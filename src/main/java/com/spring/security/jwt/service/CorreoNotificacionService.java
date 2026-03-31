package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.CorreoNotificacionCreateRequest;
import com.spring.security.jwt.dto.CorreoNotificacionResponseDto;
import com.spring.security.jwt.dto.CorreoNotificacionUpdateRequest;
import com.spring.security.jwt.exception.NegocioException;
import com.spring.security.jwt.model.CorreoNotificacionModel;
import com.spring.security.jwt.repository.CorreoNotificacionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CorreoNotificacionService implements ICorreoNotificacionService {

    private final CorreoNotificacionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<CorreoNotificacionResponseDto> findAll() {
        return repository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CorreoNotificacionResponseDto findById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Correo no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public CorreoNotificacionResponseDto create(CorreoNotificacionCreateRequest request) {
        if (repository.existsByDsCorreo(request.getDsCorreo())) {
            throw new NegocioException("Ya existe un destinatario con el correo: " + request.getDsCorreo());
        }
        CorreoNotificacionModel correo = new CorreoNotificacionModel();
        correo.setDsNombre(request.getDsNombre());
        correo.setDsCorreo(request.getDsCorreo().toLowerCase());
        correo.setBoActivo(true);
        correo.setBoRecordatorios(request.isBoRecordatorios());
        correo.setBoBitacora(request.isBoBitacora());
        correo.setDsCreadoPor("system");
        correo.setDsActualizadoPor("system");
        log.info("Creando destinatario correo={}", correo.getDsCorreo());
        return toDto(repository.save(correo));
    }

    @Override
    @Transactional
    public CorreoNotificacionResponseDto update(Long id, CorreoNotificacionUpdateRequest request) {
        CorreoNotificacionModel correo = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Correo no encontrado con id: " + id));
        if (repository.existsByDsCorreoAndIdNot(request.getDsCorreo(), id)) {
            throw new NegocioException("Ya existe otro destinatario con el correo: " + request.getDsCorreo());
        }
        correo.setDsNombre(request.getDsNombre());
        correo.setDsCorreo(request.getDsCorreo().toLowerCase());
        correo.setBoRecordatorios(request.isBoRecordatorios());
        correo.setBoBitacora(request.isBoBitacora());
        correo.setDsActualizadoPor("system");
        log.info("Actualizando destinatario id={} correo={}", id, correo.getDsCorreo());
        return toDto(repository.save(correo));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Correo no encontrado con id: " + id);
        }
        log.info("Eliminando destinatario id={}", id);
        repository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean toggleActivo(Long id) {
        CorreoNotificacionModel correo = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Correo no encontrado con id: " + id));
        correo.setBoActivo(!correo.isBoActivo());
        correo.setDsActualizadoPor("system");
        boolean nuevoEstatus = repository.save(correo).isBoActivo();
        log.info("Toggle activo id={} nuevoEstatus={}", id, nuevoEstatus);
        return nuevoEstatus;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorreoNotificacionModel> findActivosParaRecordatorios() {
        return repository.findByBoActivoTrueAndBoRecordatoriosTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CorreoNotificacionModel> findActivosParaBitacora() {
        return repository.findByBoActivoTrueAndBoBitacoraTrue();
    }

    private CorreoNotificacionResponseDto toDto(CorreoNotificacionModel model) {
        return CorreoNotificacionResponseDto.builder()
                .id(model.getId())
                .dsNombre(model.getDsNombre())
                .dsCorreo(model.getDsCorreo())
                .boActivo(model.isBoActivo())
                .boRecordatorios(model.isBoRecordatorios())
                .boBitacora(model.isBoBitacora())
                .build();
    }
}
