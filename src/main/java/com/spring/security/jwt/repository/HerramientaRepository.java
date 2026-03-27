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
            return Optional.ofNullable(herramienta);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void decrementarDisponible(Long herramientaId) {
        String sql = "UPDATE cat_herramientas SET cantidad_disponible = cantidad_disponible - 1 WHERE id = ? AND cantidad_disponible > 0";
        int rows = jdbcTemplate.update(sql, herramientaId);
        if (rows == 0) {
            throw new RuntimeException("No hay unidades disponibles de esta herramienta para préstamo");
        }
    }

    @Override
    public void incrementarDisponible(Long herramientaId) {
        String sql = "UPDATE cat_herramientas SET cantidad_disponible = LEAST(cantidad_disponible + 1, cantidad_total) WHERE id = ?";
        jdbcTemplate.update(sql, herramientaId);
    }
}
