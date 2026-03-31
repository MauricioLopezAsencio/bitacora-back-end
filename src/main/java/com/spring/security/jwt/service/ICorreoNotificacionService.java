package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.CorreoNotificacionCreateRequest;
import com.spring.security.jwt.dto.CorreoNotificacionResponseDto;
import com.spring.security.jwt.dto.CorreoNotificacionUpdateRequest;
import com.spring.security.jwt.model.CorreoNotificacionModel;

import java.util.List;

public interface ICorreoNotificacionService {

    List<CorreoNotificacionResponseDto> findAll();

    CorreoNotificacionResponseDto findById(Long id);

    CorreoNotificacionResponseDto create(CorreoNotificacionCreateRequest request);

    CorreoNotificacionResponseDto update(Long id, CorreoNotificacionUpdateRequest request);

    void deleteById(Long id);

    boolean toggleActivo(Long id);

    List<CorreoNotificacionModel> findActivosParaRecordatorios();

    List<CorreoNotificacionModel> findActivosParaBitacora();
}
