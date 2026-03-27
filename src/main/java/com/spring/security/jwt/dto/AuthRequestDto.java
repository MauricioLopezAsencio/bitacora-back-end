package com.spring.security.jwt.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDto {

    @NotBlank(message = "El usuario es obligatorio")
    private String user;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
