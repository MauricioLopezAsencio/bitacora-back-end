package com.spring.security.jwt.service;


import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;

public interface IProductService {
    List<HerramientaModel> findAll();
    List<HerramientaModel> findAllActivo();
    HerramientaDto saveHerramienta(HerramientaDto herramienta);
    void inactivarHerramienta(Long id);
}
