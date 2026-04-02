package com.spring.security.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ActividadRequest {

    @NotNull(message = "idEmpleado es requerido")
    private Long idEmpleado;

    @NotBlank(message = "tokenMicrosoft es requerido")
    private String tokenMicrosoft;

    @NotBlank(message = "tokenBitacora es requerido")
    private String tokenBitacora;

    @NotNull(message = "fechaInicio es requerida")
    private LocalDate fechaInicio;

    @NotNull(message = "fechaFin es requerida")
    private LocalDate fechaFin;
}
