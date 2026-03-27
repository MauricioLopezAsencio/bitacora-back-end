package com.spring.security.jwt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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

    @Column(name = "nomina")
    private Long nomina;

    @Column(name = "ds_creado_por", length = 255)
    private String dsCreadoPor;

    @Column(name = "ds_actualizado_por", length = 255)
    private String dsActualizadoPor;

    @Column(name = "fc_creacion")
    private Instant fcCreacion;

    @Column(name = "fc_ultima_actualizacion")
    private Instant fcUltimaActualizacion;

    @JsonIgnore
    @OneToMany(mappedBy = "empleado")
    private List<EmpleadoHerramientaModel> herramientas;
}
