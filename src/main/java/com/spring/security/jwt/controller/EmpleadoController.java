package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.EmpleadoCreateRequest;
import com.spring.security.jwt.dto.EmpleadoResponseDto;
import com.spring.security.jwt.dto.EmpleadoUpdateRequest;
import com.spring.security.jwt.service.IEmpeladoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/empleados")
@CrossOrigin("*")
@Slf4j
public class EmpleadoController {

    private final IEmpeladoService empleadoService;

    public EmpleadoController(IEmpeladoService empleadoService) {
        this.empleadoService = empleadoService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmpleadoResponseDto>>> findAll(HttpServletRequest request) {
        List<EmpleadoResponseDto> data = empleadoService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(data, "Empleados obtenidos exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpleadoResponseDto>> findById(
            @PathVariable("id") Long id, HttpServletRequest request) {
        EmpleadoResponseDto data = empleadoService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(data, "Empleado encontrado")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EmpleadoResponseDto>> create(
            @Valid @RequestBody EmpleadoCreateRequest req, HttpServletRequest request) {
        EmpleadoResponseDto data = empleadoService.create(req);
        return ResponseEntity.status(201)
                .body(ApiResponse.created(data, "Empleado creado exitosamente")
                        .toBuilder().path(request.getRequestURI()).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmpleadoResponseDto>> update(
            @PathVariable("id") Long id, @Valid @RequestBody EmpleadoUpdateRequest req,
            HttpServletRequest request) {
        EmpleadoResponseDto data = empleadoService.update(id, req);
        return ResponseEntity.ok(ApiResponse.ok(data, "Empleado actualizado exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteById(
            @PathVariable("id") Long id, HttpServletRequest request) {
        empleadoService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.<Void>ok(null, "Empleado eliminado exitosamente")
                .toBuilder().path(request.getRequestURI()).build());
    }
}
