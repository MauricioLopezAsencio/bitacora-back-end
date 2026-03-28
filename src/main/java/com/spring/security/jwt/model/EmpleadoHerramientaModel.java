package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "turno", length = 10, nullable = false)
    private String turno;

    /** Timestamp exacto de la asignación — usado para re-agendar recordatorios tras reinicio. */
    @Column(name = "fc_asignacion")
    private LocalDateTime fcAsignacion;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private EmpleadoModel empleado;

    @ManyToOne
    @JoinColumn(name = "herramienta_id", nullable = false)
    private HerramientaModel herramienta;


}
