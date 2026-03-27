package com.spring.security.jwt.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardDto {

    private final int totalTipos;
    private final int totalUnidades;
    private final int totalPrestadas;
    private final int totalDisponibles;
    private final List<CategoriaStatsDto> porCategoria;
    /** Lista de préstamos activos ordenada de mayor a menor antigüedad, con semáforo de alerta. */
    private final List<PrestamoActivoDto> prestamosActivos;
}
