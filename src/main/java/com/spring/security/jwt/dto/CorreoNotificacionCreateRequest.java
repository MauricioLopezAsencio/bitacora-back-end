package com.spring.security.jwt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CorreoNotificacionCreateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String dsNombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 255, message = "El correo no puede exceder 255 caracteres")
    private String dsCorreo;

    private boolean boRecordatorios = true;
    private boolean boBitacora      = true;
}
