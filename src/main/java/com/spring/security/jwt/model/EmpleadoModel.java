package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    @Column(name = "nomina", length = 250)
    private Long nomina;

    @OneToMany(mappedBy = "empleado")
    private List<EmpleadoHerramientaModel> herramientas; // Cambia a una lista de la tabla pivote
}
