package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.ParametroSistemaResponseDto;
import com.spring.security.jwt.dto.ParametroSistemaUpdateRequest;
import com.spring.security.jwt.dto.TurnoConfigResponseDto;
import com.spring.security.jwt.dto.TurnoConfigUpdateRequest;

import java.util.List;

public interface IConfiguracionService {

    List<TurnoConfigResponseDto> findAllTurnos();

    TurnoConfigResponseDto updateTurno(Long id, TurnoConfigUpdateRequest request);

    ParametroSistemaResponseDto getParametros();

    ParametroSistemaResponseDto updateParametros(ParametroSistemaUpdateRequest request);
}
