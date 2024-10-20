package com.spring.security.jwt.service;

import com.spring.security.jwt.model.EmpleadoModel;
import com.spring.security.jwt.repository.impl.IEmpleadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService implements IEmpeladoService {

    @Autowired
    private IEmpleadoRepository iEmpleadosRepository;

    @Override
    public List<EmpleadoModel> findAll() {
        List<EmpleadoModel> list;
        try{
            list = iEmpleadosRepository.findAll();
        }catch (Exception ex){
            throw ex;
        }
        return list;
    }
}
