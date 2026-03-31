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
@Table(name = "ct_correo_notificacion")
public class CorreoNotificacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ds_nombre", length = 255, nullable = false)
    private String dsNombre;

    @Column(name = "ds_correo", length = 255, nullable = false, unique = true)
    private String dsCorreo;

    @Column(name = "bo_activo", nullable = false)
    private boolean boActivo = true;

    @Column(name = "bo_recordatorios", nullable = false)
    private boolean boRecordatorios = true;

    @Column(name = "bo_bitacora", nullable = false)
    private boolean boBitacora = true;

    @Column(name = "ds_creado_por", length = 255)
    private String dsCreadoPor;

    @Column(name = "ds_actualizado_por", length = 255)
    private String dsActualizadoPor;

    @Column(name = "fc_creacion")
    private Instant fcCreacion;

    @Column(name = "fc_ultima_actualizacion")
    private Instant fcUltimaActualizacion;
}
