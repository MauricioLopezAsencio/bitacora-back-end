package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.EmpleadoCreateRequest;
import com.spring.security.jwt.dto.EmpleadoResponseDto;
import com.spring.security.jwt.dto.EmpleadoUpdateRequest;

import java.util.List;

public interface IEmpeladoService {
    List<EmpleadoResponseDto> findAll();
    EmpleadoResponseDto findById(Long id);
    EmpleadoResponseDto create(EmpleadoCreateRequest request);
    EmpleadoResponseDto update(Long id, EmpleadoUpdateRequest request);
    void deleteById(Long id);
}
