package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cat_empleados")
public class EmpleadoModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre", length = 250)
    private String nombre;
    @Column(name = "primer_apellido", length = 250)
    private String primerApellido;
    @Column(name = "segundo_apellido", length = 250)
    private String segundoApellido;
    @Column(name = "telefono", length = 250)
    private Long telefono;
}
