package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.BitacoraProyectosRequest;
import com.spring.security.jwt.dto.RegistrarActividadRequest;
import com.spring.security.jwt.service.IBitacoraService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api/v1/bitacora")
@CrossOrigin("*")
@Slf4j
public class BitacoraController {

    private final IBitacoraService bitacoraService;

    public BitacoraController(IBitacoraService bitacoraService) {
        this.bitacoraService = bitacoraService;
    }

    @PostMapping("/actividades")
    public ResponseEntity<ApiResponse<Object>> registrarActividad(
            @Valid @RequestBody RegistrarActividadRequest request,
            HttpServletRequest servletRequest) {

        try {
            Object data = bitacoraService.registrarActividad(request);
            return ResponseEntity.ok(ApiResponse.ok(data, "Actividad registrada exitosamente")
                    .toBuilder().path(servletRequest.getRequestURI()).build());

        } catch (HttpClientErrorException ex) {
            HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode().value());
            String reason = httpStatus != null ? httpStatus.getReasonPhrase() : "Error desconocido";
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ApiResponse.<Object>builder()
                            .status(ex.getStatusCode().value())
                            .message("Error al registrar actividad en bitácora: " + reason)
                            .errorCode("BITACORA_API_ERROR")
                            .path(servletRequest.getRequestURI())
                            .build());
        }
    }

    @PostMapping("/proyectos/byEmpleado")
    public ResponseEntity<ApiResponse<Object>> obtenerProyectosPorEmpleado(
            @Valid @RequestBody BitacoraProyectosRequest request,
            HttpServletRequest servletRequest) {

        try {
            Long idEmpleado = bitacoraService.obtenerIdEmpleado(request.getUsername(), request.getPassword());
            Object data = bitacoraService.obtenerProyectosPorEmpleado(
                    idEmpleado,
                    request.getUsername(),
                    request.getPassword()
            );

            return ResponseEntity.ok(ApiResponse.ok(data, "Proyectos obtenidos exitosamente")
                    .toBuilder().path(servletRequest.getRequestURI()).build());

        } catch (HttpClientErrorException ex) {
            HttpStatus httpStatus = HttpStatus.resolve(ex.getStatusCode().value());
            String reason = httpStatus != null ? httpStatus.getReasonPhrase() : "Error desconocido";
            return ResponseEntity.status(ex.getStatusCode())
                    .body(ApiResponse.<Object>builder()
                            .status(ex.getStatusCode().value())
                            .message("Error al consultar bitácora: " + reason)
                            .errorCode("BITACORA_API_ERROR")
                            .path(servletRequest.getRequestURI())
                            .build());
        }
    }
}
