package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActividadResultDto {

    private List<ActividadDto> actividades;
    private List<ActividadDto> sesionesNoPareadasAProyecto;
}
