package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IHerramientaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class HerramientaRepository implements IHerramientaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<HerramientaModel> findAll() {
        String SQL = "SELECT * FROM cat_herramientas";
        return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
    }

    @Override
    public Optional<HerramientaModel> findById(Long id) {
        String SQL = "SELECT * FROM cat_herramientas WHERE id = ?";
        try {
            HerramientaModel herramienta = jdbcTemplate.queryForObject(SQL, new Object[]{id}, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
            return Optional.ofNullable(herramienta); // Devuelve el empleado envuelto en un Optional
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // Si no se encuentra, devuelve Optional vacío
        }
    }
}
