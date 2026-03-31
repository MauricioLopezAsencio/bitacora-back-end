package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ct_turno_config")
public class TurnoConfigModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MATUTINO | VESPERTINO | NOCTURNO */
    @Column(name = "cv_turno", length = 20, nullable = false, unique = true)
    private String cvTurno;

    /** Hora de inicio del turno (0-23) */
    @Column(name = "hora_inicio", nullable = false)
    private int horaInicio;

    /** Hora de fin del turno (0-23). NOCTURNO: fin < inicio (cruza medianoche) */
    @Column(name = "hora_fin", nullable = false)
    private int horaFin;

    @Column(name = "ds_creado_por", length = 255)
    private String dsCreadoPor;

    @Column(name = "ds_actualizado_por", length = 255)
    private String dsActualizadoPor;

    @Column(name = "fc_creacion")
    private Instant fcCreacion;

    @Column(name = "fc_ultima_actualizacion")
    private Instant fcUltimaActualizacion;
}
