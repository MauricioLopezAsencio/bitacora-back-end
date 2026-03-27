package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ct_usuarios")
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cv_usuario", length = 50, nullable = false, unique = true)
    private String cvUsuario;

    @Column(name = "ds_password", length = 255, nullable = false)
    private String dsPassword;

    @Column(name = "ds_nombre", length = 250, nullable = false)
    private String dsNombre;

    @Column(name = "bo_activo", nullable = false)
    private boolean boActivo;
}
