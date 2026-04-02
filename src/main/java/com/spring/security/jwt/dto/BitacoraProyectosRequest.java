package com.spring.security.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BitacoraProyectosRequest {

    @NotNull(message = "idEmpleado es requerido")
    private Long idEmpleado;

    @NotBlank(message = "token es requerido")
    private String token;
}
