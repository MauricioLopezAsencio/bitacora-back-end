package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.model.UsuarioModel;

import java.util.Optional;

public interface IUsuarioRepository {
    Optional<UsuarioModel> findByCvUsuario(String cvUsuario);
}
