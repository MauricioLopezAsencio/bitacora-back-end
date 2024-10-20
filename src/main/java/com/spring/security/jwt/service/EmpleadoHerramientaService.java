package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.EmpleadoHerramientaDTO;
import com.spring.security.jwt.model.EmpleadoHerramientaModel;
import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.EmpleadoRepository;
import com.spring.security.jwt.repository.HerramientaRepository;
import com.spring.security.jwt.repository.impl.EmpleadoHerramientaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmpleadoHerramientaService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private HerramientaRepository herramientaRepository;

    @Autowired
    private EmpleadoHerramientaRepository empleadoHerramientaRepository;

    // Método para asignar una herramienta a un empleado
    @Transactional
    public EmpleadoHerramientaModel asignarHerramientaAEmpleado(EmpleadoHerramientaDTO dto) {
        // Buscar el empleado por ID
        EmpleadoModel empleado = empleadoRepository.findById(dto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Buscar la herramienta por ID
        HerramientaModel herramienta = herramientaRepository.findById(dto.getHerramientaId())
                .orElseThrow(() -> new RuntimeException("Herramienta no encontrada"));

        // Crear la relación empleado-herramienta
        EmpleadoHerramientaModel empleadoHerramienta = new EmpleadoHerramientaModel();
        empleadoHerramienta.setEmpleado(empleado);
        empleadoHerramienta.setHerramienta(herramienta);
        empleadoHerramienta.setFecha(LocalDate.now());
        empleadoHerramienta.setEstatus(false);

        // Guardar la relación en la tabla pivote
        return empleadoHerramientaRepository.save(empleadoHerramienta);
    }

    // Método para obtener todas las relaciones de empleado y herramienta
    public List<BitacoraDto> findAll() {

        List<EmpleadoHerramientaModel> empleadoHerramientaModels = empleadoHerramientaRepository.findAll();

        // Obtener todos los empleados en un solo llamado
        List<EmpleadoModel> empleados = empleadoRepository.findAll();
        // Obtener todas las herramientas en un solo llamado
        List<HerramientaModel> herramientas = herramientaRepository.findAll();

        // Crear un mapa de empleados y herramientas para facilitar el acceso
        Map<Long, String> empleadoMap = empleados.stream()
                .collect(Collectors.toMap(
                        EmpleadoModel::getId,
                        empleado -> empleado.getNombre() + " " + empleado.getPrimerApellido() + (empleado.getSegundoApellido() != null ? " " + empleado.getSegundoApellido() : "")
                ));

        Map<Long, String> herramientaMap = herramientas.stream()
                .collect(Collectors.toMap(HerramientaModel::getId, HerramientaModel::getNombre));

        List<BitacoraDto> response = new ArrayList<>();

        // Iterar sobre las relaciones de empleado y herramienta
        for (EmpleadoHerramientaModel ehModel : empleadoHerramientaModels) {
            // Asegúrate de obtener el ID del empleado y de la herramienta
            Long empleadoId = ehModel.getEmpleado().getId(); // Cambiado para obtener el ID
            Long herramientaId = ehModel.getHerramienta().getId(); // Cambiado para obtener el ID

            String nombreEmpleado = empleadoMap.get(empleadoId); // Usar el ID del empleado
            String nombreHerramienta = herramientaMap.get(herramientaId); // Usar el ID de la herramienta

            // Crear un nuevo BitacoraDto y añadirlo a la respuesta
            BitacoraDto bitacoraDto = new BitacoraDto();
            bitacoraDto.setId(ehModel.getId());
            bitacoraDto.setNombreEmpleado(nombreEmpleado);
            bitacoraDto.setNombreHerramienta(nombreHerramienta);
            bitacoraDto.setEstatus(ehModel.isEstatus()); // Asume que tienes un método isEstatus en tu modelo
            bitacoraDto.setFecha(ehModel.getFecha()); // Establecer la fecha actual

            response.add(bitacoraDto);
        }

        return response;
    }

   /* // Método para eliminar la relación empleado-herramienta
    @Transactional
    public void eliminarHerramientaDeEmpleado(Long empleadoId, Long herramientaId) {
        EmpleadoHerramientaModel empleadoHerramienta = empleadoHerramientaRepository.findByEmpleadoIdAndHerramientaId(empleadoId, herramientaId)
                .orElseThrow(() -> new RuntimeException("Relación no encontrada"));
        empleadoHerramientaRepository.delete(empleadoHerramienta);
    }*/
}
