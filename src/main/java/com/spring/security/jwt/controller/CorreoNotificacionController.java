package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.CorreoNotificacionCreateRequest;
import com.spring.security.jwt.dto.CorreoNotificacionResponseDto;
import com.spring.security.jwt.dto.CorreoNotificacionUpdateRequest;
import com.spring.security.jwt.service.ICorreoNotificacionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/correos")
@RequiredArgsConstructor
@Slf4j
public class CorreoNotificacionController {

    private final ICorreoNotificacionService correoService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CorreoNotificacionResponseDto>>> findAll(HttpServletRequest request) {
        log.info("GET /correos txId={}", MDC.get("transactionId"));
        List<CorreoNotificacionResponseDto> data = correoService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(data, "Destinatarios obtenidos exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CorreoNotificacionResponseDto>> findById(
            @PathVariable Long id, HttpServletRequest request) {
        log.info("GET /correos/{} txId={}", id, MDC.get("transactionId"));
        CorreoNotificacionResponseDto data = correoService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(data, "Destinatario encontrado")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CorreoNotificacionResponseDto>> create(
            @Valid @RequestBody CorreoNotificacionCreateRequest req, HttpServletRequest request) {
        log.info("POST /correos correo={} txId={}", req.getDsCorreo(), MDC.get("transactionId"));
        CorreoNotificacionResponseDto data = correoService.create(req);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(data, "Destinatario creado exitosamente")
                        .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CorreoNotificacionResponseDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody CorreoNotificacionUpdateRequest req,
            HttpServletRequest request) {
        log.info("PUT /correos/{} txId={}", id, MDC.get("transactionId"));
        CorreoNotificacionResponseDto data = correoService.update(id, req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Destinatario actualizado exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @PathVariable Long id, HttpServletRequest request) {
        log.info("DELETE /correos/{} txId={}", id, MDC.get("transactionId"));
        correoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Destinatario eliminado exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping("/{id}/toggle-activo")
    public ResponseEntity<ApiResponse<Void>> toggleActivo(
            @PathVariable Long id, HttpServletRequest request) {
        log.info("PUT /correos/{}/toggle-activo txId={}", id, MDC.get("transactionId"));
        boolean nuevoEstatus = correoService.toggleActivo(id);
        String mensaje = nuevoEstatus ? "Destinatario activado exitosamente." : "Destinatario desactivado exitosamente.";
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, mensaje)
                .toBuilder().path(request.getRequestURI()).build());
    }
}
