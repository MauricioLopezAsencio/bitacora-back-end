package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.HerramientaResponseDto;
import com.spring.security.jwt.dto.HerramientaUpdateRequest;
import com.spring.security.jwt.service.IProductService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/herramientas")
@CrossOrigin("*")
@Slf4j
public class HerramientaController {

    private final IProductService herramientaService;

    public HerramientaController(IProductService herramientaService) {
        this.herramientaService = herramientaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HerramientaResponseDto>>> findAll(HttpServletRequest request) {
        log.info("GET /herramientas txId={}", MDC.get("transactionId"));
        List<HerramientaResponseDto> data = herramientaService.findAll().stream()
                .map(h -> HerramientaResponseDto.builder()
                        .id(h.getId())
                        .nombre(h.getNombre())
                        .categoria(h.getCategoria())
                        .estatus(h.isEstatus())
                        .cantidadTotal(h.getCantidadTotal())
                        .cantidadDisponible(h.getCantidadDisponible())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(data, "Herramientas obtenidas exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HerramientaResponseDto>> findById(
            @PathVariable("id") Long id, HttpServletRequest request) {
        log.info("GET /herramientas/{} txId={}", id, MDC.get("transactionId"));
        HerramientaResponseDto data = herramientaService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(data, "Herramienta encontrada")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HerramientaDto>> create(
            @Valid @RequestBody HerramientaDto req, HttpServletRequest request) {
        log.info("POST /herramientas nombre={} txId={}", req.getNombre(), MDC.get("transactionId"));
        HerramientaDto data = herramientaService.saveHerramienta(req);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(data, "Herramienta creada exitosamente")
                        .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HerramientaResponseDto>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody HerramientaUpdateRequest req,
            HttpServletRequest request) {
        log.info("PUT /herramientas/{} txId={}", id, MDC.get("transactionId"));
        HerramientaResponseDto data = herramientaService.update(id, req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Herramienta actualizada exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @PathVariable("id") Long id, HttpServletRequest request) {
        log.info("DELETE /herramientas/{} txId={}", id, MDC.get("transactionId"));
        herramientaService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Herramienta eliminada exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping("/{id}/toggle-estatus")
    public ResponseEntity<ApiResponse<Void>> toggleEstatus(
            @PathVariable("id") Long id, HttpServletRequest request) {
        log.info("PUT /herramientas/{}/toggle-estatus txId={}", id, MDC.get("transactionId"));
        boolean nuevoEstatus = herramientaService.toggleEstatus(id);
        String mensaje = nuevoEstatus ? "Herramienta activada exitosamente." : "Herramienta desactivada exitosamente.";
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, mensaje)
                .toBuilder().path(request.getRequestURI()).build());
    }
}
