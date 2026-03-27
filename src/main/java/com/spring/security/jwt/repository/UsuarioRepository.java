package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.UsuarioModel;
import com.spring.security.jwt.repository.impl.IUsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepository implements IUsuarioRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Optional<UsuarioModel> findByCvUsuario(String cvUsuario) {
        String sql = "SELECT * FROM ct_usuarios WHERE cv_usuario = ? AND bo_activo = true";
        try {
            UsuarioModel usuario = jdbcTemplate.queryForObject(
                    sql,
                    new Object[]{cvUsuario},
                    BeanPropertyRowMapper.newInstance(UsuarioModel.class)
            );
            return Optional.ofNullable(usuario);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
