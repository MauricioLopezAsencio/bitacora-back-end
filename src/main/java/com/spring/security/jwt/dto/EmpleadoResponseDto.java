package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmpleadoResponseDto {
    private final Long id;
    private final String nombre;
    private final Long nomina;
}
