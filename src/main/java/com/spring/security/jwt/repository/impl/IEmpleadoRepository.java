package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.model.EmpleadoModel;

import java.util.List;
import java.util.Optional;

public interface IEmpleadoRepository {
    List<EmpleadoModel> findAll();
    Optional<EmpleadoModel> findById(Long id);
    EmpleadoModel save(EmpleadoModel empleado);
    EmpleadoModel update(Long id, EmpleadoModel empleado);
    void deleteById(Long id);
}
