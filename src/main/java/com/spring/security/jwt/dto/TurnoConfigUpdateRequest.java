package com.spring.security.jwt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TurnoConfigUpdateRequest {

    @Min(value = 0, message = "La hora de inicio debe estar entre 0 y 23")
    @Max(value = 23, message = "La hora de inicio debe estar entre 0 y 23")
    private int horaInicio;

    @Min(value = 0, message = "La hora de fin debe estar entre 0 y 23")
    @Max(value = 23, message = "La hora de fin debe estar entre 0 y 23")
    private int horaFin;
}
