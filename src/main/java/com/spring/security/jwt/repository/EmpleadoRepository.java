package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.repository.impl.IEmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EmpleadoRepository implements IEmpleadoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<EmpleadoModel> findAll() {
        String SQL = "SELECT * FROM cat_empleados";
        return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(EmpleadoModel.class));
    }

    @Override
    public Optional<EmpleadoModel> findById(Long id) {
        String SQL = "SELECT * FROM cat_empleados WHERE id = ?";
        try {
            EmpleadoModel empleado = jdbcTemplate.queryForObject(SQL, new Object[]{id}, BeanPropertyRowMapper.newInstance(EmpleadoModel.class));
            return Optional.ofNullable(empleado); // Devuelve el empleado envuelto en un Optional
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // Si no se encuentra, devuelve Optional vacío
        }
    }

}
