package com.spring.security.jwt.service;

import com.spring.security.jwt.model.EmpleadoModel;

import java.util.List;

public interface IEmpeladoService {
    public List<EmpleadoModel> findAll();
}
