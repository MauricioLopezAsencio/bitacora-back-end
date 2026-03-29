package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.EmpleadoCreateRequest;
import com.spring.security.jwt.dto.EmpleadoResponseDto;
import com.spring.security.jwt.dto.EmpleadoUpdateRequest;
import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.repository.impl.IEmpleadoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpleadoService implements IEmpeladoService {

    private final IEmpleadoRepository empleadoRepository;

    public EmpleadoService(IEmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoResponseDto> findAll() {
        return empleadoRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EmpleadoResponseDto findById(Long id) {
        return empleadoRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public EmpleadoResponseDto create(EmpleadoCreateRequest request) {
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setNombre(request.getNombre().toUpperCase());
        empleado.setNomina(request.getNomina());
        empleado.setDsCreadoPor("system");
        empleado.setDsActualizadoPor("system");
        return toDto(empleadoRepository.save(empleado));
    }

    @Override
    @Transactional
    public EmpleadoResponseDto update(Long id, EmpleadoUpdateRequest request) {
        EmpleadoModel empleado = new EmpleadoModel();
        empleado.setNombre(request.getNombre().toUpperCase());
        empleado.setNomina(request.getNomina());
        empleado.setDsActualizadoPor("system");
        return toDto(empleadoRepository.update(id, empleado));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        empleadoRepository.deleteById(id);
    }

    private EmpleadoResponseDto toDto(EmpleadoModel model) {
        return EmpleadoResponseDto.builder()
                .id(model.getId())
                .nombre(model.getNombre())
                .nomina(model.getNomina())
                .build();
    }
}
