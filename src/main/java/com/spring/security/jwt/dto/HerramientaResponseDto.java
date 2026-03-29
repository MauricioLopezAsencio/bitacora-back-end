package com.spring.security.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HerramientaResponseDto {

    private Long id;
    private String nombre;
    private String categoria;
    private boolean estatus;
    private Integer cantidadTotal;
    private Integer cantidadDisponible;
}
