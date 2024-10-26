package com.spring.security.jwt.repository;

import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IProductResository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository implements IProductResository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<HerramientaModel> findAll() {
       String SQL = "SELECT * FROM cat_herramientas";
       return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
    }

    @Override
    public List<HerramientaModel> findAllActivo() {
        String SQL = "SELECT * FROM cat_herramientas WHERE estatus = true";
        return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
    }


    @Override
    public HerramientaDto save(HerramientaDto entity) {
        String sql = "INSERT INTO cat_herramientas (nombre, categoria, estatus) VALUES (?, ?, ?)"; // Asegúrate de incluir todos los campos necesarios
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Inserta la herramienta y obtiene el ID generado
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, entity.getNombre());
            ps.setString(2, entity.getCategoria());
            ps.setBoolean(3, entity.isEstatus());
            return ps;
        }, keyHolder);


        return entity; // Devuelve la entidad guardada con el ID
    }

    @Override
    public void inactivarHerramienta(Long id) {
        String sql = "UPDATE cat_herramientas SET estatus = false WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);

        if (rowsAffected == 0) {
            throw new RuntimeException("No se encontró ninguna herramienta con el id: " + id);
        }
    }
}
