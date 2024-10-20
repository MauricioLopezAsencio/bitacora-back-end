package com.spring.security.jwt.controller;

import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.IEmpleadoRepository;
import com.spring.security.jwt.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
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


}
