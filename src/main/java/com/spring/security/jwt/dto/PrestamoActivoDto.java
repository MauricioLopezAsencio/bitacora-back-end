package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PrestamoActivoDto {

    private final Long id;
    private final String nombreEmpleado;
    private final String nombreHerramienta;
    private final LocalDate fecha;
    private final int diasPrestado;
    /**
     * Semáforo de urgencia calculado en backend:
     * VERDE   → 0–2 días (normal)
     * AMARILLO → 3–4 días (atención)
     * ROJO    → 5+ días  (urgente, recuperar)
     */
    private final String alerta;

    public static String calcularAlerta(int dias) {
        if (dias <= 2) return "VERDE";
        if (dias <= 4) return "AMARILLO";
        return "ROJO";
    }
}
