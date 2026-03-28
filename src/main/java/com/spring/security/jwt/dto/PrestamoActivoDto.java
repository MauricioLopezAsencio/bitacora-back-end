package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
public class PrestamoActivoDto {

    private final Long id;
    private final String nombreEmpleado;
    private final String nombreHerramienta;
    private final LocalDate fecha;
    private final int diasPrestado;
    /** Turno del préstamo: DIA (07:00–19:00) o NOCHE (19:00–07:00). */
    private final String turno;
    /**
     * Indica si el turno de 12 horas sigue vigente al momento de consultar.
     * false = el turno ya terminó y la herramienta aún no fue devuelta.
     */
    private final boolean turnoActivo;
    /**
     * Semáforo de urgencia calculado en backend:
     * VERDE    → 0–2 días  (normal)
     * AMARILLO → 3–4 días  (atención)
     * ROJO     → 5+ días   (urgente, recuperar)
     */
    private final String alerta;

    public static String calcularAlerta(int dias) {
        if (dias <= 2) return "VERDE";
        if (dias <= 4) return "AMARILLO";
        return "ROJO";
    }

    /**
     * Calcula si el turno de 12 horas sigue activo según la convención:
     * <ul>
     *   <li>DIA   → 07:00–19:00 del mismo día de la asignación</li>
     *   <li>NOCHE → 19:00 del día de asignación – 07:00 del día siguiente</li>
     * </ul>
     */
    public static boolean calcularTurnoActivo(String turno, LocalDate fecha) {
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if ("DIA".equalsIgnoreCase(turno)) {
            return fecha.equals(hoy) && ahora.isBefore(LocalTime.of(19, 0));
        }
        // NOCHE
        if (fecha.equals(hoy)) {
            return ahora.isAfter(LocalTime.of(18, 59));
        }
        if (fecha.equals(hoy.minusDays(1))) {
            return ahora.isBefore(LocalTime.of(7, 0));
        }
        return false;
    }
}
