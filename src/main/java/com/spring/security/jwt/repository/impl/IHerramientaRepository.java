package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;
import java.util.Optional;

public interface IHerramientaRepository {
    Optional<HerramientaModel> findById(Long herramientaId);
    List<HerramientaModel> findAll();
    void decrementarDisponible(Long herramientaId);
    void incrementarDisponible(Long herramientaId);
}
