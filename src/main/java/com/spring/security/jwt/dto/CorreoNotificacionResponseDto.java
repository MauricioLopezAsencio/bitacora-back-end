package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CorreoNotificacionResponseDto {

    private Long    id;
    private String  dsNombre;
    private String  dsCorreo;
    private boolean boActivo;
    private boolean boRecordatorios;
    private boolean boBitacora;
}
