package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IProductResository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProductRepository implements IProductResository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<HerramientaModel> findAll() {
       String SQL = "SELECT * FROM cat_herramientas";
       return jdbcTemplate.query(SQL, BeanPropertyRowMapper.newInstance(HerramientaModel.class));
    }

}
