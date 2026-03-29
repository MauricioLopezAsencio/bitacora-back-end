package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.repository.impl.IEmpleadoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class EmpleadoRepository implements IEmpleadoRepository {

    private static final RowMapper<EmpleadoModel> EMPLEADO_MAPPER = (rs, rowNum) -> {
        EmpleadoModel m = new EmpleadoModel();
        m.setId(rs.getLong("id"));
        m.setNombre(rs.getString("nombre"));
        m.setNomina(rs.getLong("nomina"));
        return m;
    };

    private final JdbcTemplate jdbcTemplate;

    public EmpleadoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<EmpleadoModel> findAll() {
        String sql = "SELECT id, nombre, nomina FROM cat_empleados ORDER BY nombre";
        return jdbcTemplate.query(sql, EMPLEADO_MAPPER);
    }

    @Override
    public Optional<EmpleadoModel> findById(Long id) {
        String sql = "SELECT id, nombre, nomina FROM cat_empleados WHERE id = ?";
        try {
            EmpleadoModel empleado = jdbcTemplate.queryForObject(sql, EMPLEADO_MAPPER, id);
            return Optional.ofNullable(empleado);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public EmpleadoModel save(EmpleadoModel empleado) {
        String sql = "INSERT INTO cat_empleados (nombre, nomina, ds_creado_por, ds_actualizado_por) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, empleado.getNombre());
            ps.setLong(2, empleado.getNomina());
            ps.setString(3, empleado.getDsCreadoPor());
            ps.setString(4, empleado.getDsActualizadoPor());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            empleado.setId(key.longValue());
        }
        return empleado;
    }

    @Override
    public EmpleadoModel update(Long id, EmpleadoModel empleado) {
        findById(id).orElseThrow(() ->
                new EntityNotFoundException("Empleado no encontrado con id: " + id));

        String sql = "UPDATE cat_empleados SET nombre = ?, nomina = ?, ds_actualizado_por = ? WHERE id = ?";
        jdbcTemplate.update(sql, empleado.getNombre(), empleado.getNomina(), empleado.getDsActualizadoPor(), id);
        empleado.setId(id);
        return empleado;
    }

    @Override
    public void deleteById(Long id) {
        findById(id).orElseThrow(() ->
                new EntityNotFoundException("Empleado no encontrado con id: " + id));
        jdbcTemplate.update("DELETE FROM cat_empleados WHERE id = ?", id);
    }
}
