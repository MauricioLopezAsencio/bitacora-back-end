package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.EmpleadoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EmpleadoRepository implements IEmpleadoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Override
    public List<EmpleadoModel>  findAll() {
        String SQL = "SELECT * FROM cat_empleados";
        return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(EmpleadoModel.class));
    }
}
