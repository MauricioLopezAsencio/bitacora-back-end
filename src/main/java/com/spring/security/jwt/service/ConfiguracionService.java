package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.ParametroSistemaResponseDto;
import com.spring.security.jwt.dto.ParametroSistemaUpdateRequest;
import com.spring.security.jwt.dto.TurnoConfigResponseDto;
import com.spring.security.jwt.dto.TurnoConfigUpdateRequest;
import com.spring.security.jwt.exception.NegocioException;
import com.spring.security.jwt.model.ParametroSistemaModel;
import com.spring.security.jwt.model.TurnoConfigModel;
import com.spring.security.jwt.repository.ParametroSistemaRepository;
import com.spring.security.jwt.repository.TurnoConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionService implements IConfiguracionService {

    private static final long PARAMETRO_ID = 1L;

    private final TurnoConfigRepository    turnoConfigRepository;
    private final ParametroSistemaRepository parametroRepository;

    // ── Turnos ────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TurnoConfigResponseDto> findAllTurnos() {
        return turnoConfigRepository.findAll().stream()
                .map(this::toTurnoDto)
                .toList();
    }

    @Override
    @Transactional
    public TurnoConfigResponseDto updateTurno(Long id, TurnoConfigUpdateRequest request) {
        TurnoConfigModel turno = turnoConfigRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Turno no encontrado con id: " + id));

        boolean esNocturno = "NOCTURNO".equalsIgnoreCase(turno.getCvTurno());
        boolean cruzaMedianoche = request.getHoraFin() < request.getHoraInicio();

        // Para NOCTURNO el fin debe ser menor que el inicio (cruza medianoche).
        // Para MATUTINO y VESPERTINO el fin debe ser mayor que el inicio.
        if (!esNocturno && cruzaMedianoche) {
            throw new NegocioException(
                "Para " + turno.getCvTurno() + " la hora de fin debe ser mayor que la hora de inicio.");
        }
        if (esNocturno && !cruzaMedianoche) {
            throw new NegocioException(
                "Para NOCTURNO la hora de fin debe ser menor que la hora de inicio (el turno cruza la medianoche).");
        }

        turno.setHoraInicio(request.getHoraInicio());
        turno.setHoraFin(request.getHoraFin());
        turno.setDsActualizadoPor("system");

        log.info("Turno actualizado cvTurno={} horaInicio={} horaFin={}",
                turno.getCvTurno(), request.getHoraInicio(), request.getHoraFin());
        return toTurnoDto(turnoConfigRepository.save(turno));
    }

    // ── Parámetros ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ParametroSistemaResponseDto getParametros() {
        ParametroSistemaModel params = parametroRepository.findById(PARAMETRO_ID)
                .orElseThrow(() -> new EntityNotFoundException("Parámetros del sistema no encontrados"));
        return toParametroDto(params);
    }

    @Override
    @Transactional
    public ParametroSistemaResponseDto updateParametros(ParametroSistemaUpdateRequest request) {
        ParametroSistemaModel params = parametroRepository.findById(PARAMETRO_ID)
                .orElseThrow(() -> new EntityNotFoundException("Parámetros del sistema no encontrados"));
        int anteriorMinutos = params.getMinutosRecordatorio();
        params.setMinutosRecordatorio(request.getMinutosRecordatorio());
        params.setDsActualizadoPor("system");
        ParametroSistemaModel guardado = parametroRepository.save(params);
        log.info("Parámetros actualizados minutosRecordatorio={} -> {}",
                anteriorMinutos, guardado.getMinutosRecordatorio());
        return toParametroDto(guardado);
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private TurnoConfigResponseDto toTurnoDto(TurnoConfigModel model) {
        String horario = String.format("%02d:00 → %02d:00", model.getHoraInicio(), model.getHoraFin());
        return TurnoConfigResponseDto.builder()
                .id(model.getId())
                .cvTurno(model.getCvTurno())
                .horaInicio(model.getHoraInicio())
                .horaFin(model.getHoraFin())
                .dsHorario(horario)
                .build();
    }

    private ParametroSistemaResponseDto toParametroDto(ParametroSistemaModel model) {
        String descripcion = model.getMinutosRecordatorio() + " minutos antes del fin de turno";
        return ParametroSistemaResponseDto.builder()
                .id(model.getId())
                .minutosRecordatorio(model.getMinutosRecordatorio())
                .dsDescripcion(descripcion)
                .build();
    }
}
