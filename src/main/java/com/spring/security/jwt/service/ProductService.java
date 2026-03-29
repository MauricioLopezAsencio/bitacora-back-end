package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.PageResponse;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IProductResository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService implements  IProductService{
    @Autowired
    private IProductResository iProductResository;

    @Override
    public List<HerramientaModel> findAll() {
        List<HerramientaModel> list;
        try{
            list = iProductResository.findAll();
        }catch (Exception ex){
            throw ex;
        }
        return list;
    }

    @Override
    public List<HerramientaModel> findAllActivo() {
        List<HerramientaModel> list;
        try{
            list = iProductResository.findAllActivo();
        }catch (Exception ex){
            throw ex;
        }
        return list;
    }

    @Override
    public HerramientaDto saveHerramienta(HerramientaDto herramienta) {
       // Asegúrate de que esta propiedad exista en tu modelo
        return iProductResository.save(herramienta);
    }

    @Override
    public boolean toggleEstatus(Long id) {
        return iProductResository.toggleEstatus(id);
    }

    @Override
    public DashboardDto getDashboard() {
        return iProductResository.getDashboard();
    }

    @Override
    public List<BitacoraDto> getBitacora() {
        return iProductResository.getBitacora();
    }

    @Override
    public PageResponse<BitacoraDto> getBitacoraPaginado(int page, int size) {
        return iProductResository.getBitacoraPaginado(page, size);
    }
}
