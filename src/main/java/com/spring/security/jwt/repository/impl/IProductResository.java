package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.HerramientaResponseDto;
import com.spring.security.jwt.dto.HerramientaUpdateRequest;
import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;
import java.util.Optional;

public interface IProductResository {
    List<HerramientaModel> findAll();
    List<HerramientaModel> findAllActivo();
    Optional<HerramientaModel> findById(Long id);
    HerramientaDto save(HerramientaDto entity);
    HerramientaResponseDto update(Long id, HerramientaUpdateRequest request);
    void delete(Long id);
    boolean toggleEstatus(Long id);
    DashboardDto getDashboard();
    List<BitacoraDto> getBitacora();
}
