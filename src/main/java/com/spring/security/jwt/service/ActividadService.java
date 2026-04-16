package com.spring.security.jwt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.security.jwt.dto.ActividadDto;
import com.spring.security.jwt.dto.ActividadRequest;
import com.spring.security.jwt.dto.ActividadResultDto;
import com.spring.security.jwt.dto.CalendarioEventoDto;
import com.spring.security.jwt.dto.ProyectoDto;
import com.spring.security.jwt.exception.TokenExpiradoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        Long idEmpleado = bitacoraService.obtenerIdEmpleado(request.getUsername(), request.getPassword());
        List<CalendarioEventoDto> eventos = obtenerEventos(request);
        List<Map<String, Object>> proyectos = obtenerProyectos(idEmpleado,
                request.getUsername(), request.getPassword());
        Object tiposActividad = obtenerTiposActividad(request.getUsername(), request.getPassword());

        Map<String, List<Map<String, Object>>> registrosPorFecha =
                obtenerRegistrosPorFecha(idEmpleado, eventos, request.getUsername(), request.getPassword());

        List<ActividadDto> todas = eventos.stream()
                .flatMap(evento -> expandirEnFranjas(evento, idEmpleado, proyectos, registrosPorFecha).stream())
                .toList();

        Map<Boolean, List<ActividadDto>> particion = todas.stream()
                .collect(Collectors.partitioningBy(a -> !NA.equals(a.getIdProyecto())));

        return ActividadResultDto.builder()
                .actividades(particion.get(true))
                .sesionesNoPareadasAProyecto(particion.get(false))
                .proyectosDisponibles(mapearProyectos(proyectos))
                .tiposActividad(tiposActividad)
                .build();
    }

    // ─── Registros existentes agrupados por fecha ────────────────────────────

    private Map<String, List<Map<String, Object>>> obtenerRegistrosPorFecha(
            Long idEmpleado, List<CalendarioEventoDto> eventos, String username, String password) {

        Set<String> fechas = eventos.stream()
                .map(e -> convertirFecha(e.getStart().split(" ")[0]))
                .collect(Collectors.toSet());

        Map<String, List<Map<String, Object>>> resultado = new java.util.HashMap<>();
        for (String fecha : fechas) {
            try {
                resultado.put(fecha,
                        bitacoraService.obtenerRegistrosPorEmpleadoYFecha(idEmpleado, fecha, username, password));
            } catch (Exception ex) {
                log.warn("No se pudieron obtener registros SCO fecha={}: {}", fecha, ex.getMessage());
                resultado.put(fecha, Collections.emptyList());
            }
        }
        return resultado;
    }

    // ─── Expande un evento en sus franjas libres ─────────────────────────────

    private List<ActividadDto> expandirEnFranjas(CalendarioEventoDto evento, Long idEmpleado,
                                                  List<Map<String, Object>> proyectos,
                                                  Map<String, List<Map<String, Object>>> registrosPorFecha) {
        String[] startParts = evento.getStart().split(" ");
        String[] endParts   = evento.getEnd().split(" ");
        String fecha        = convertirFecha(startParts[0]);
        String horaInicio   = startParts[1];
        String horaFin      = endParts[1];

        List<Map<String, Object>> registrosDelDia = registrosPorFecha.getOrDefault(fecha, Collections.emptyList());
        List<String[]> franjas = bitacoraService.calcularFranjasLibres(horaInicio, horaFin, registrosDelDia);

        if (franjas.isEmpty()) {
            log.debug("Evento completamente cubierto, se omite subject='{}' fecha={} horario={}-{}",
                    evento.getSubject(), fecha, horaInicio, horaFin);
            return Collections.emptyList();
        }

        Object idProyecto = findProyecto(evento.getSubject(), proyectos);
        return franjas.stream()
                .map(franja -> ActividadDto.builder()
                        .idEmpleado(idEmpleado)
                        .idActividad(resolverIdActividad(evento.getModalidad()))
                        .idTipoActividad(ID_TIPO_ACTIVIDAD)
                        .idProyecto(idProyecto)
                        .descripcion(evento.getSubject())
                        .fechaRegistro(fecha)
                        .horaInicio(franja[0])
                        .horaFin(franja[1])
                        .build())
                .toList();
    }

    // ─── Tipos de actividad ──────────────────────────────────────────────────

    private Object obtenerTiposActividad(String username, String password) {
        try {
            return bitacoraService.obtenerTiposActividad(username, password);
        } catch (Exception ex) {
            log.error("Error al obtener tipos de actividad: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    // ─── Mapeo de proyectos para combo ──────────────────────────────────────

    private List<ProyectoDto> mapearProyectos(List<Map<String, Object>> proyectos) {
        if (proyectos == null) return Collections.emptyList();
        return proyectos.stream()
                .filter(p -> p.get("id") != null && p.get("descripcion") != null)
                .map(p -> ProyectoDto.builder()
                        .id(((Number) p.get("id")).longValue())
                        .descripcion(p.get("descripcion").toString())
                        .build())
                .toList();
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

    private List<Map<String, Object>> obtenerProyectos(Long idEmpleado, String username, String password) {
        try {
            Object raw = bitacoraService.obtenerProyectosPorEmpleado(idEmpleado, username, password);
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
                throw new TokenExpiradoException("bitacora");
            }
            log.error("Error al obtener proyectos de bitácora idEmpleado={}: {}", idEmpleado, ex.getMessage());
            return Collections.emptyList();
        } catch (Exception ex) {
            log.error("Error al obtener proyectos de bitácora idEmpleado={}: {}", idEmpleado, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
