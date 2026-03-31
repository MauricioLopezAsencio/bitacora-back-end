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
@Table(name = "ct_parametro_sistema")
public class ParametroSistemaModel {

    /** Siempre id=1. Tabla de fila única. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Minutos antes del fin de turno para enviar el recordatorio (1-120) */
    @Column(name = "minutos_recordatorio", nullable = false)
    private int minutosRecordatorio;

    @Column(name = "ds_creado_por", length = 255)
    private String dsCreadoPor;

    @Column(name = "ds_actualizado_por", length = 255)
    private String dsActualizadoPor;

    @Column(name = "fc_creacion")
    private Instant fcCreacion;

    @Column(name = "fc_ultima_actualizacion")
    private Instant fcUltimaActualizacion;
}
