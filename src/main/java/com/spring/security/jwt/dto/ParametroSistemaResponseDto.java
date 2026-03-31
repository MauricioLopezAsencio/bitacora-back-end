package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParametroSistemaResponseDto {

    private Long   id;
    private int    minutosRecordatorio;
    /** Representación legible: "30 minutos antes del fin de turno" */
    private String dsDescripcion;
}
