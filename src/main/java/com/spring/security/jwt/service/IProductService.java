package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.HerramientaResponseDto;
import com.spring.security.jwt.dto.HerramientaUpdateRequest;
import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;

public interface IProductService {
    List<HerramientaModel> findAll();
    List<HerramientaModel> findAllActivo();
    HerramientaResponseDto findById(Long id);
    HerramientaDto saveHerramienta(HerramientaDto herramienta);
    HerramientaResponseDto update(Long id, HerramientaUpdateRequest request);
    void deleteById(Long id);
    boolean toggleEstatus(Long id);
    DashboardDto getDashboard();
    List<BitacoraDto> getBitacora();
}
