package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;

public interface IProductResository {
    List<HerramientaModel> findAll();
    List<HerramientaModel> findAllActivo();
    HerramientaDto save(HerramientaDto entity);
    void inactivarHerramienta(Long id);
}
