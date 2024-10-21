package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.EmpleadoHerramientaDTO;
import com.spring.security.jwt.model.EmpleadoHerramientaModel;
import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IEmpleadoRepository;
import com.spring.security.jwt.service.EmpleadoHerramientaService;
import com.spring.security.jwt.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@CrossOrigin("*")
@RequestMapping("api/v1/")
public class ProductController {

    @Autowired
    IProductService iProductService;
    @Autowired
    EmpleadoHerramientaService empleadoHerramientaService;

    @Autowired
    IEmpleadoRepository iEmpleadosRepository;

    @GetMapping("/products")
    public ResponseEntity<?> listProducts() {
        List<HerramientaModel> products = this.iProductService.findAll();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/empleados")
    public ResponseEntity<?> listempleados() {
        List<EmpleadoModel> empleados = this.iEmpleadosRepository.findAll();
        return new ResponseEntity<>(empleados, HttpStatus.OK);
    }
    @GetMapping("/bitacora")
    public ResponseEntity<List<BitacoraDto>> bitacora() {
        List<BitacoraDto> bitacoraDto = empleadoHerramientaService.findAll();
        return new ResponseEntity<>(bitacoraDto, HttpStatus.OK);
    }

    @PostMapping("/asignar")
    public ResponseEntity<EmpleadoHerramientaModel> asignarHerramientaAEmpleado(@RequestBody EmpleadoHerramientaDTO dto) {
        EmpleadoHerramientaModel empleadoHerramienta = empleadoHerramientaService.asignarHerramientaAEmpleado(dto);
        return ResponseEntity.ok(empleadoHerramienta);
    }

    @PutMapping("/actualizar")
    public ResponseEntity<EmpleadoHerramientaModel> actualizarEstatus(@RequestBody BitacoraDto dto) {
        EmpleadoHerramientaModel actualizado = null;
        try {
            actualizado = empleadoHerramientaService.actualizarEstatus(dto);
        } catch (RuntimeException e) {
            // Aquí podrías loguear el error si lo deseas
            // Log.error("Error al actualizar el estatus: " + e.getMessage());
        }

        return ResponseEntity.ok(actualizado != null ? actualizado : new EmpleadoHerramientaModel());
    }

}
