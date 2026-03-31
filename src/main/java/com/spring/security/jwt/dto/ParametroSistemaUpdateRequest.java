package com.spring.security.jwt.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParametroSistemaUpdateRequest {

    @Min(value = 1,   message = "El tiempo de recordatorio mínimo es 1 minuto")
    @Max(value = 120, message = "El tiempo de recordatorio máximo es 120 minutos")
    private int minutosRecordatorio;
}
