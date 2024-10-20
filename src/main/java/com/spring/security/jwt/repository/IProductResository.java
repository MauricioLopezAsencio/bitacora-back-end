package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;

public interface IProductResository {
    public List<HerramientaModel> findAll();
}
