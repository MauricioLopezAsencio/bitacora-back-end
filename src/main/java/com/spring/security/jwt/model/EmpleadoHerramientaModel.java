package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "empleado_herramienta")
public class EmpleadoHerramientaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha", length = 250)
    private LocalDate fecha;

    @Column(name = "estatus", length = 250)
    private boolean estatus;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoModel empleado;

    @ManyToOne
    @JoinColumn(name = "herramienta_id", nullable = false)
    private HerramientaModel herramienta;


}
