package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.EmpleadoModel;

import java.util.List;

public interface IEmpleadoRepository {
 public List<EmpleadoModel>  findAll();
}
