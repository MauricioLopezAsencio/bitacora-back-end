package com.spring.security.jwt.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class PrestamoActivoDto {

    private final Long id;
    private final String nombreEmpleado;
    private final String nombreHerramienta;
    private final LocalDate fecha;
    private final int diasPrestado;
    /** Turno del préstamo: MATUTINO (06–14), VESPERTINO (14–22) o NOCTURNO (22–06). */
    private final String turno;
    /**
     * Indica si el turno de 8 horas sigue vigente al momento de consultar.
     * false = el turno ya terminó y la herramienta aún no fue devuelta.
     */
    private final boolean turnoActivo;
    /**
     * Semáforo de urgencia:
     * VERDE    → turno activo, primera mitad (> 240 min restantes)
     * AMARILLO → turno activo, segunda mitad (≤ 240 min restantes)
     * ROJO     → turno terminado, aún no inició, o préstamo de día anterior
     */
    private final String alerta;

    /**
     * Reglas en orden:
     * 1. diasPrestado > 0            → ROJO (día anterior, sin importar turno)
     * 2. turno no activo (hoy)       → ROJO (terminó o aún no inicia)
     * 3. turno activo, > 240 min     → VERDE (primera mitad del turno)
     * 4. turno activo, ≤ 240 min     → AMARILLO (segunda mitad del turno)
     */
    public static String calcularAlerta(int diasPrestado, String turno, LocalDate fecha) {
        if (diasPrestado > 0) return "ROJO";
        if (!calcularTurnoActivo(turno, fecha)) return "ROJO";
        long minutosRestantes = Duration.between(
                LocalDateTime.now(), calcularFinTurnoDt(turno, fecha)).toMinutes();
        return minutosRestantes > 240 ? "VERDE" : "AMARILLO";
    }

    private static LocalDateTime calcularFinTurnoDt(String turno, LocalDate fecha) {
        return switch (turno.toUpperCase()) {
            case "VESPERTINO" -> fecha.atTime(LocalTime.of(22, 0));
            case "NOCTURNO"   -> {
                // Si ahora es antes de las 06:00, el turno activo termina HOY
                // (la asignación fue entre medianoche y las 05:59).
                // En cualquier otro momento, termina al día siguiente.
                LocalTime ahora = LocalTime.now();
                if (ahora.isBefore(LocalTime.of(6, 0))) {
                    yield fecha.atTime(LocalTime.of(6, 0));
                } else {
                    yield fecha.plusDays(1).atTime(LocalTime.of(6, 0));
                }
            }
            default           -> fecha.atTime(LocalTime.of(14, 0)); // MATUTINO
        };
    }

    /**
     * Calcula si el turno de 8 horas sigue activo según la convención:
     * <ul>
     *   <li>MATUTINO   → 06:00–14:00 del mismo día de la asignación</li>
     *   <li>VESPERTINO → 14:00–22:00 del mismo día de la asignación</li>
     *   <li>NOCTURNO   → 22:00 del día de asignación – 06:00 del día siguiente</li>
     * </ul>
     */
    public static boolean calcularTurnoActivo(String turno, LocalDate fecha) {
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        if ("MATUTINO".equalsIgnoreCase(turno)) {
            return fecha.equals(hoy)
                    && !ahora.isBefore(LocalTime.of(6, 0))
                    && ahora.isBefore(LocalTime.of(14, 0));
        }
        if ("VESPERTINO".equalsIgnoreCase(turno)) {
            return fecha.equals(hoy)
                    && !ahora.isBefore(LocalTime.of(14, 0))
                    && ahora.isBefore(LocalTime.of(22, 0));
        }
        // NOCTURNO: 22:00 del día de asignación → 06:00 del día siguiente
        // También cubre asignaciones post-medianoche (00:00–05:59) donde fecha == hoy
        if (fecha.equals(hoy)) {
            return !ahora.isBefore(LocalTime.of(22, 0)) || ahora.isBefore(LocalTime.of(6, 0));
        }
        if (fecha.equals(hoy.minusDays(1))) {
            return ahora.isBefore(LocalTime.of(6, 0));
        }
        return false;
    }
}
