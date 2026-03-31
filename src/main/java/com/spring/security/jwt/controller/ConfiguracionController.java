package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.ParametroSistemaResponseDto;
import com.spring.security.jwt.dto.ParametroSistemaUpdateRequest;
import com.spring.security.jwt.dto.TurnoConfigResponseDto;
import com.spring.security.jwt.dto.TurnoConfigUpdateRequest;
import com.spring.security.jwt.service.IConfiguracionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionController {

    private final IConfiguracionService configuracionService;

    // ── Turnos ────────────────────────────────────────────────────────────────
    // Acepta: /api/v1/configuracion/turnos  Y  /api/v1/turnos

    @GetMapping({"/api/v1/configuracion/turnos", "/api/v1/turnos"})
    public ResponseEntity<ApiResponse<List<TurnoConfigResponseDto>>> findAllTurnos(
            HttpServletRequest request) {
        log.info("GET {} txId={}", request.getRequestURI(), MDC.get("transactionId"));
        List<TurnoConfigResponseDto> data = configuracionService.findAllTurnos();
        return ResponseEntity.ok(ApiResponse.ok(data, "Configuración de turnos obtenida exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping({"/api/v1/configuracion/turnos/{id}", "/api/v1/turnos/{id}"})
    public ResponseEntity<ApiResponse<TurnoConfigResponseDto>> updateTurno(
            @PathVariable Long id,
            @Valid @RequestBody TurnoConfigUpdateRequest req,
            HttpServletRequest request) {
        log.info("PUT {} txId={}", request.getRequestURI(), MDC.get("transactionId"));
        TurnoConfigResponseDto data = configuracionService.updateTurno(id, req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Horario de turno actualizado exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    // ── Parámetros / Recordatorio ──────────────────────────────────────────────
    // Acepta: /api/v1/configuracion/parametros  Y  /api/v1/configuracion/recordatorio

    @GetMapping({"/api/v1/configuracion/parametros", "/api/v1/configuracion/recordatorio"})
    public ResponseEntity<ApiResponse<ParametroSistemaResponseDto>> getParametros(
            HttpServletRequest request) {
        log.info("GET {} txId={}", request.getRequestURI(), MDC.get("transactionId"));
        ParametroSistemaResponseDto data = configuracionService.getParametros();
        return ResponseEntity.ok(ApiResponse.ok(data, "Parámetros del sistema obtenidos exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping({"/api/v1/configuracion/parametros", "/api/v1/configuracion/recordatorio"})
    public ResponseEntity<ApiResponse<ParametroSistemaResponseDto>> updateParametros(
            @Valid @RequestBody ParametroSistemaUpdateRequest req,
            HttpServletRequest request) {
        log.info("PUT {} minutosRecordatorio={} txId={}",
                request.getRequestURI(), req.getMinutosRecordatorio(), MDC.get("transactionId"));
        ParametroSistemaResponseDto data = configuracionService.updateParametros(req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Parámetros del sistema actualizados exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }
}
