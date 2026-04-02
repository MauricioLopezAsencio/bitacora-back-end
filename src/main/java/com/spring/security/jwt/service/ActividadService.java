package com.spring.security.jwt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.security.jwt.dto.ActividadDto;
import com.spring.security.jwt.dto.ActividadRequest;
import com.spring.security.jwt.dto.ActividadResultDto;
import com.spring.security.jwt.dto.CalendarioEventoDto;
import com.spring.security.jwt.exception.TokenExpiradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ActividadService implements IActividadService {

    // ─── IDs de actividad — ajustar según catálogo de bitácora ──────────────
    private static final long ID_SESION_INTERNA = 1L;
    private static final long ID_SESION_EXTERNA  = 2L;

    private static final int  ID_TIPO_ACTIVIDAD  = 3;
    private static final String NA               = "N/A";

private final ICalendarioService calendarioService;
    private final IBitacoraService   bitacoraService;
    private final ObjectMapper       objectMapper;

    public ActividadService(ICalendarioService calendarioService,
                            IBitacoraService bitacoraService,
                            ObjectMapper objectMapper) {
        this.calendarioService = calendarioService;
        this.bitacoraService   = bitacoraService;
        this.objectMapper      = objectMapper;
    }

    @Override
    public ActividadResultDto obtenerActividades(ActividadRequest request) {
        List<CalendarioEventoDto> eventos = obtenerEventos(request);
        List<Map<String, Object>> proyectos = obtenerProyectos(request.getIdEmpleado(),
                request.getTokenBitacora());

        Map<Boolean, List<ActividadDto>> particion = eventos.stream()
                .map(evento -> mapearActividad(evento, request.getIdEmpleado(), proyectos))
                .collect(Collectors.partitioningBy(a -> !NA.equals(a.getIdProyecto())));

        return ActividadResultDto.builder()
                .actividades(particion.get(true))
                .sesionesNoPareadasAProyecto(particion.get(false))
                .build();
    }

    // ─── Mapeo principal ─────────────────────────────────────────────────────

    private ActividadDto mapearActividad(CalendarioEventoDto evento,
                                         Long idEmpleado,
                                         List<Map<String, Object>> proyectos) {
        String[] startParts = evento.getStart().split(" ");
        String[] endParts   = evento.getEnd().split(" ");

        return ActividadDto.builder()
                .idEmpleado(idEmpleado)
                .idActividad(resolverIdActividad(evento.getModalidad()))
                .idTipoActividad(ID_TIPO_ACTIVIDAD)
                .idProyecto(findProyecto(evento.getSubject(), proyectos))
                .descripcion(evento.getSubject())
                .fechaRegistro(convertirFecha(startParts[0]))
                .horaInicio(startParts[1])
                .horaFin(endParts[1])
                .build();
    }

    // ─── idActividad según modalidad ─────────────────────────────────────────

    private long resolverIdActividad(String modalidad) {
        return "externa".equalsIgnoreCase(modalidad) ? ID_SESION_EXTERNA : ID_SESION_INTERNA;
    }

    // ─── Match de proyecto ───────────────────────────────────────────────────

    private Object findProyecto(String subject, List<Map<String, Object>> proyectos) {
        if (proyectos == null || proyectos.isEmpty() || subject == null) return NA;

        List<String> keywords = extraerKeywords(subject);
        if (keywords.isEmpty()) return NA;

        return proyectos.stream()
                .filter(p -> keywordsMatchenProyecto(keywords, p))
                .map(p -> {
                    Object id = p.get("id");
                    return id != null ? ((Number) id).longValue() : (Object) NA;
                })
                .findFirst()
                .orElse(NA);
    }

    /**
     * Divide el subject por delimitadores y espacios para obtener palabras individuales.
     * "Sesión Capacitación Moneki- Spot" → ["sesión", "capacitación", "moneki", "spot"]
     * "Valia | Daily"                    → ["valia", "daily"]
     */
    private List<String> extraerKeywords(String subject) {
        return List.of(subject.split("[|\\-:\\s]+")).stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(k -> k.length() > 2)
                .toList();
    }

    private boolean keywordsMatchenProyecto(List<String> keywords, Map<String, Object> proyecto) {
        Object descripcionRaw = proyecto.get("descripcion");
        if (descripcionRaw == null) return false;

        String descripcion = descripcionRaw.toString();
        int primerGuion = descripcion.indexOf('-');
        if (primerGuion < 0) return false;

        String fragmento = descripcion.substring(primerGuion + 1).toLowerCase();

        return keywords.stream().anyMatch(fragmento::contains);
    }

    // ─── Fecha "dd/MM/yyyy" → "yyyy-MM-dd" ──────────────────────────────────

    private String convertirFecha(String fecha) {
        String[] partes = fecha.split("/");
        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }

    // ─── Llamadas a servicios ────────────────────────────────────────────────

    private List<CalendarioEventoDto> obtenerEventos(ActividadRequest request) {
        try {
            return calendarioService.obtenerEventos(
                    request.getTokenMicrosoft(), request.getFechaInicio(), request.getFechaFin());
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new TokenExpiradoException("tokenMicrosoft");
            }
            log.error("Error al obtener eventos de calendario: {}", ex.getMessage());
            return Collections.emptyList();
        } catch (Exception ex) {
            log.error("Error al obtener eventos de calendario: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> obtenerProyectos(Long idEmpleado, String tokenBitacora) {
        try {
            Object raw = bitacoraService.obtenerProyectosPorEmpleado(idEmpleado, tokenBitacora);
            if (raw == null) return Collections.emptyList();

            // El API devuelve { "status": "OK", "data": [...] }
            Map<String, Object> wrapper = objectMapper.convertValue(raw,
                    new TypeReference<Map<String, Object>>() {});
            Object data = wrapper.get("data");
            if (data == null) return Collections.emptyList();

            return objectMapper.convertValue(data,
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode().value() == 401) {
                throw new TokenExpiradoException("tokenBitacora");
            }
            log.error("Error al obtener proyectos de bitácora idEmpleado={}: {}", idEmpleado, ex.getMessage());
            return Collections.emptyList();
        } catch (Exception ex) {
            log.error("Error al obtener proyectos de bitácora idEmpleado={}: {}", idEmpleado, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
