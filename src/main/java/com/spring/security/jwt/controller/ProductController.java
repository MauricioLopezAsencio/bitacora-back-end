package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.EmpleadoHerramientaDTO;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.model.EmpleadoHerramientaModel;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.service.EmpleadoHerramientaService;
import com.spring.security.jwt.service.IProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin("*")
@RequestMapping("api/v1/")
@Slf4j
public class ProductController {

    @Autowired
    IProductService iProductService;
    @Autowired
    EmpleadoHerramientaService empleadoHerramientaService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardDto>> dashboard() {
        log.info("GET /dashboard txId={}", MDC.get("transactionId"));
        DashboardDto data = iProductService.getDashboard();
        return ResponseEntity.ok(ApiResponse.ok(data, "Estadísticas del dashboard"));
    }

    @GetMapping("/products")
    public ResponseEntity<?> listProducts() {
        log.info("GET /products txId={}", MDC.get("transactionId"));
        List<HerramientaModel> products = this.iProductService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/productsActivos")
    public ResponseEntity<?> listProductsActivos() {
        log.info("GET /productsActivos txId={}", MDC.get("transactionId"));
        List<HerramientaModel> products = this.iProductService.findAllActivo();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/bitacora")
    public ResponseEntity<ApiResponse<List<BitacoraDto>>> bitacora() {
        log.info("GET /bitacora txId={}", MDC.get("transactionId"));
        List<BitacoraDto> bitacoraDto = iProductService.getBitacora();
        return ResponseEntity.ok(ApiResponse.ok(bitacoraDto, "Bitácora de asignaciones"));
    }

    @PostMapping("/saveHerramienta")
    public ResponseEntity<?> saveHerramienta(@RequestBody HerramientaDto name) {
        log.info("POST /saveHerramienta herramienta={} txId={}", name.getNombre(), MDC.get("transactionId"));
        try {
            HerramientaDto herramienta = iProductService.saveHerramienta(name);
            return ResponseEntity.ok(herramienta);
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la herramienta: " + e.getMessage());
        }
    }

    @PostMapping("/asignar")
    public ResponseEntity<ApiResponse<EmpleadoHerramientaModel>> asignarHerramientaAEmpleado(@RequestBody EmpleadoHerramientaDTO dto) {
        log.info("POST /asignar empleadoId={} herramientaId={} turno={} txId={}",
                dto.getEmpleadoId(), dto.getHerramientaId(), dto.getTurno(), MDC.get("transactionId"));
        EmpleadoHerramientaModel guardado = empleadoHerramientaService.asignarHerramientaAEmpleado(dto);
        return ResponseEntity.ok(ApiResponse.ok(guardado, "Herramienta asignada exitosamente"));
    }

    @PutMapping("/actualizar")
    public ResponseEntity<ApiResponse<EmpleadoHerramientaModel>> actualizarEstatus(@RequestBody BitacoraDto dto) {
        log.info("PUT /actualizar asignacionId={} estatus={} txId={}", dto.getId(), dto.isEstatus(), MDC.get("transactionId"));
        EmpleadoHerramientaModel actualizado = empleadoHerramientaService.actualizarEstatus(dto);
        String mensaje = actualizado.isEstatus() ? "Herramienta devuelta exitosamente" : "Herramienta desactivada exitosamente";
        return ResponseEntity.ok(ApiResponse.ok(actualizado, mensaje));
    }

    @PutMapping("/inactivarHerramienta/{id}")
    public ResponseEntity<ApiResponse<Void>> toggleHerramienta(@PathVariable("id") Long id) {
        log.info("PUT /inactivarHerramienta id={} txId={}", id, MDC.get("transactionId"));
        boolean nuevoEstatus = iProductService.toggleEstatus(id);
        String mensaje = nuevoEstatus ? "Herramienta activada exitosamente." : "Herramienta desactivada exitosamente.";
        return ResponseEntity.ok(ApiResponse.ok(null, mensaje));
    }

}
