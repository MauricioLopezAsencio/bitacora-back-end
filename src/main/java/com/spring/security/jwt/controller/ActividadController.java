package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ActividadRequest;
import com.spring.security.jwt.dto.ActividadResultDto;
import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.service.IActividadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/actividades")
@CrossOrigin("*")
@Slf4j
public class ActividadController {

    private final IActividadService actividadService;

    public ActividadController(IActividadService actividadService) {
        this.actividadService = actividadService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ActividadResultDto>> obtenerActividades(
            @Valid @RequestBody ActividadRequest request,
            HttpServletRequest servletRequest) {

        ActividadResultDto data = actividadService.obtenerActividades(request);
        return ResponseEntity.ok(ApiResponse.ok(data, "Actividades obtenidas exitosamente")
                .toBuilder().path(servletRequest.getRequestURI()).build());
    }
}
