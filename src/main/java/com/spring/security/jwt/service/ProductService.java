package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.BitacoraDto;
import com.spring.security.jwt.dto.DashboardDto;
import com.spring.security.jwt.dto.HerramientaDto;
import com.spring.security.jwt.dto.HerramientaResponseDto;
import com.spring.security.jwt.dto.HerramientaUpdateRequest;
import com.spring.security.jwt.exception.NegocioException;
import com.spring.security.jwt.model.HerramientaModel;
import com.spring.security.jwt.repository.impl.IProductResository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService implements IProductService {

    @Autowired
    private IProductResository iProductResository;

    @Override
    @Transactional(readOnly = true)
    public List<HerramientaModel> findAll() {
        return iProductResository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HerramientaModel> findAllActivo() {
        return iProductResository.findAllActivo();
    }

    @Override
    @Transactional(readOnly = true)
    public HerramientaResponseDto findById(Long id) {
        HerramientaModel h = iProductResository.findById(id)
                .orElseThrow(() -> new NegocioException("No se encontró la herramienta con id: " + id));
        return toDto(h);
    }

    @Override
    @Transactional
    public HerramientaDto saveHerramienta(HerramientaDto herramienta) {
        return iProductResository.save(herramienta);
    }

    @Override
    @Transactional
    public HerramientaResponseDto update(Long id, HerramientaUpdateRequest request) {
        return iProductResository.update(id, request);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        iProductResository.delete(id);
    }

    @Override
    @Transactional
    public boolean toggleEstatus(Long id) {
        return iProductResository.toggleEstatus(id);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        return iProductResository.getDashboard();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BitacoraDto> getBitacora() {
        return iProductResository.getBitacora();
    }

    private HerramientaResponseDto toDto(HerramientaModel h) {
        return HerramientaResponseDto.builder()
                .id(h.getId())
                .nombre(h.getNombre())
                .categoria(h.getCategoria())
                .estatus(h.isEstatus())
                .cantidadTotal(h.getCantidadTotal())
                .cantidadDisponible(h.getCantidadDisponible())
                .build();
    }
}
