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
        EmpleadoModel empleado = empleadoRepository.findById(dto.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        HerramientaModel herramienta = herramientaRepository.findById(dto.getHerramientaId())
                .orElseThrow(() -> new RuntimeException("Herramienta no encontrada"));

        if (herramienta.getCantidadDisponible() == null || herramienta.getCantidadDisponible() <= 0) {
            throw new RuntimeException("No hay unidades disponibles de '" + herramienta.getNombre() + "' para préstamo");
        }

        EmpleadoHerramientaModel empleadoHerramienta = new EmpleadoHerramientaModel();
        empleadoHerramienta.setEmpleado(empleado);
        empleadoHerramienta.setHerramienta(herramienta);
        empleadoHerramienta.setFecha(LocalDate.now());
        empleadoHerramienta.setEstatus(false);

        EmpleadoHerramientaModel guardado = empleadoHerramientaRepository.save(empleadoHerramienta);
        herramientaRepository.decrementarDisponible(herramienta.getId());
        return guardado;
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
                        empleado -> empleado.getNombre()
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

    @Transactional
    public EmpleadoHerramientaModel actualizarEstatus(BitacoraDto dto) {
        if (!dto.isEstatus()) {
            throw new RuntimeException(
                "No se puede desactivar una asignación. Si el empleado tomó la herramienta nuevamente, registre una nueva asignación."
            );
        }

        EmpleadoHerramientaModel empleadoHerramienta = empleadoHerramientaRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Relación empleado-herramienta no encontrada"));

        if (empleadoHerramienta.isEstatus()) {
            throw new RuntimeException(
                "Esta asignación ya está activa. Si el empleado tomó la herramienta nuevamente, registre una nueva asignación."
            );
        }

        empleadoHerramienta.setEstatus(true);
        EmpleadoHerramientaModel devuelta = empleadoHerramientaRepository.save(empleadoHerramienta);
        herramientaRepository.incrementarDisponible(empleadoHerramienta.getHerramienta().getId());
        return devuelta;
    }

}
