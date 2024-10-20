package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.model.EmpleadoModel;

import java.util.List;
import java.util.Optional;

public interface IEmpleadoRepository {
 public List<EmpleadoModel>  findAll();

 Optional<EmpleadoModel> findById(Long empleadoId);
}
