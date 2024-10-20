package com.spring.security.jwt.service;


import com.spring.security.jwt.model.HerramientaModel;

import java.util.List;

public interface IProductService {
    public List<HerramientaModel> findAll();
}
