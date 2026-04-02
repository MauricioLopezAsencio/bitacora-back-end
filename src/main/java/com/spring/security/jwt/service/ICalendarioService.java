package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.CalendarioEventoDto;

import java.util.List;

public interface ICalendarioService {

    List<CalendarioEventoDto> obtenerEventos(String bearerToken);
}
