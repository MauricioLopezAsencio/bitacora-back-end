package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "empleado_herramienta")
public class EmpleadoHerramientaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoModel empleado;

    @ManyToOne
    @JoinColumn(name = "herramienta_id", nullable = false)
    private HerramientaModel herramienta;


}
