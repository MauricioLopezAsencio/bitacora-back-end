package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TurnoConfigResponseDto {

    private Long   id;
    private String cvTurno;
    private int    horaInicio;
    private int    horaFin;
    /** Representación legible: "06:00 → 14:00" */
    private String dsHorario;
}
