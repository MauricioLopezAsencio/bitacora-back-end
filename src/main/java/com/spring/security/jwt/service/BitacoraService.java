package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.RegistrarActividadRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BitacoraService implements IBitacoraService {

    private static final String BITACORA_URL =
            "https://scoca.casystem.com.mx/api/bitacora/proyectos/byEmpleado/{idEmpleado}";

    private static final String REGISTRAR_ACTIVIDAD_URL =
            "https://scoca.casystem.com.mx/api/bitacora";

    private static final String TIPO_ACTIVIDAD_URL =
            "https://scoca.casystem.com.mx/api/bitacora/tipoActividad";

    private static final String ACTIVIDADES_POR_TIPO_URL =
            "https://scoca.casystem.com.mx/api/bitacora/actividades/{idTipoActividad}";

    private static final String REGISTROS_POR_EMPLEADO_FECHA_URL =
            "https://scoca.casystem.com.mx/api/bitacora/registrosByEmpleadoAndFechaRegistro/{idEmpleado}/{fecha}";

    private static final DateTimeFormatter TIME_FMT    = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TIME_PARSE  = DateTimeFormatter.ofPattern("HH:mm[:ss]");

    private final RestTemplate restTemplate;
    private final BitacoraTokenManager tokenManager;

    public BitacoraService(RestTemplate restTemplate, BitacoraTokenManager tokenManager) {
        this.restTemplate = restTemplate;
        this.tokenManager = tokenManager;
    }

    @Override
    public Long obtenerIdEmpleado(String username, String password) {
        return tokenManager.obtenerIdEmpleado(username, password);
    }

    @Override
    public Object obtenerProyectosPorEmpleado(Long idEmpleado, String username, String password) {
        try {
            return ejecutarConsulta(idEmpleado, tokenManager.obtenerToken(username, password));
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Token expirado, renovando y reintentando idEmpleado={}", idEmpleado);
                return ejecutarConsulta(idEmpleado, tokenManager.renovarToken(username, password));
            }
            log.error("Error al consultar bitácora idEmpleado={} status={}", idEmpleado, ex.getStatusCode());
            throw ex;
        }
    }

    @Override
    public Object registrarActividad(RegistrarActividadRequest request) {
        Long idEmpleado = tokenManager.obtenerIdEmpleado(request.getUsername(), request.getPassword());
        String token    = tokenManager.obtenerToken(request.getUsername(), request.getPassword());
        try {
            return ejecutarRegistro(request, idEmpleado, token);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Token expirado al registrar actividad, renovando...");
                String nuevoToken = tokenManager.renovarToken(request.getUsername(), request.getPassword());
                return ejecutarRegistro(request, idEmpleado, nuevoToken);
            }
            log.error("Error al registrar actividad en bitácora status={}", ex.getStatusCode());
            throw ex;
        }
    }

    @Override
    public Object obtenerTiposActividad(String username, String password) {
        try {
            return ejecutarGetSimple(TIPO_ACTIVIDAD_URL, tokenManager.obtenerToken(username, password));
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Token expirado al obtener tipos de actividad, renovando...");
                return ejecutarGetSimple(TIPO_ACTIVIDAD_URL, tokenManager.renovarToken(username, password));
            }
            log.error("Error al consultar tipos de actividad status={}", ex.getStatusCode());
            throw ex;
        }
    }

    @Override
    public Object obtenerActividadesPorTipo(Integer idTipoActividad, String username, String password) {
        try {
            return ejecutarGetConVariable(ACTIVIDADES_POR_TIPO_URL,
                    tokenManager.obtenerToken(username, password), idTipoActividad);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Token expirado al obtener actividades idTipoActividad={}, renovando...", idTipoActividad);
                return ejecutarGetConVariable(ACTIVIDADES_POR_TIPO_URL,
                        tokenManager.renovarToken(username, password), idTipoActividad);
            }
            log.error("Error al consultar actividades idTipoActividad={} status={}", idTipoActividad, ex.getStatusCode());
            throw ex;
        }
    }

    // ─── Registro con partición anti-traslape ────────────────────────────────

    @Override
    public List<Object> registrarActividadConParticion(RegistrarActividadRequest request) {
        Long idEmpleado = tokenManager.obtenerIdEmpleado(request.getUsername(), request.getPassword());
        String token    = tokenManager.obtenerToken(request.getUsername(), request.getPassword());

        List<Map<String, Object>> existentes = obtenerRegistrosExistentes(
                idEmpleado, request.getFechaRegistro().toString(), token, request);

        return registrarConSeparacion(request, idEmpleado, token, existentes);
    }

    // ─── Consulta de registros existentes ───────────────────────────────────

    @Override
    public List<Map<String, Object>> obtenerRegistrosPorEmpleadoYFecha(
            Long idEmpleado, String fecha, String username, String password) {
        String token = tokenManager.obtenerToken(username, password);
        try {
            Object raw = ejecutarGetConVariable(REGISTROS_POR_EMPLEADO_FECHA_URL, token, idEmpleado, fecha);
            return parsearListaDeRegistros(raw);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                String nuevoToken = tokenManager.renovarToken(username, password);
                Object raw = ejecutarGetConVariable(REGISTROS_POR_EMPLEADO_FECHA_URL, nuevoToken, idEmpleado, fecha);
                return parsearListaDeRegistros(raw);
            }
            log.warn("No se pudieron obtener registros idEmpleado={} fecha={}: {}", idEmpleado, fecha, ex.getMessage());
            return Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Error inesperado al obtener registros idEmpleado={} fecha={}: {}", idEmpleado, fecha, ex.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> obtenerRegistrosExistentes(
            Long idEmpleado, String fecha, String token, RegistrarActividadRequest request) {
        try {
            Object raw = ejecutarGetConVariable(
                    REGISTROS_POR_EMPLEADO_FECHA_URL, token, idEmpleado, fecha);
            return parsearListaDeRegistros(raw);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                String nuevoToken = tokenManager.renovarToken(request.getUsername(), request.getPassword());
                Object raw = ejecutarGetConVariable(
                        REGISTROS_POR_EMPLEADO_FECHA_URL, nuevoToken, idEmpleado, fecha);
                return parsearListaDeRegistros(raw);
            }
            log.warn("No se pudieron obtener registros existentes idEmpleado={} fecha={}: {}",
                    idEmpleado, fecha, ex.getMessage());
            return Collections.emptyList();
        } catch (Exception ex) {
            log.warn("Error inesperado al obtener registros existentes: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parsearListaDeRegistros(Object raw) {
        if (raw == null) return Collections.emptyList();
        if (raw instanceof List<?> list) return (List<Map<String, Object>>) list;
        if (raw instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof List<?> list) return (List<Map<String, Object>>) list;
        }
        return Collections.emptyList();
    }

    // ─── Mover registros superpuestos para dar espacio al nuevo ─────────────

    /**
     * Para cada registro de Scoca que se traslape con [newStart, newEnd]:
     *   - Si el registro termina después de newEnd → moverlo para que empiece en newEnd
     *   - Si el registro termina antes o en newEnd  → no hace falta editarlo (queda dentro)
     * Luego registra la nueva actividad en [newStart, newEnd].
     */
    private List<Object> registrarConSeparacion(RegistrarActividadRequest request,
                                                 Long idEmpleado, String token,
                                                 List<Map<String, Object>> existentes) {
        LocalTime newStart = LocalTime.parse(request.getHoraInicio(), TIME_FMT);
        LocalTime newEnd   = LocalTime.parse(request.getHoraFin(),    TIME_FMT);

        List<Map<String, Object>> superpuestos = existentes.stream()
                .filter(r -> r.get("horaInicio") != null && r.get("horaFin") != null && r.get("id") != null)
                .filter(r -> {
                    LocalTime rS = LocalTime.parse(r.get("horaInicio").toString(), TIME_PARSE);
                    LocalTime rE = LocalTime.parse(r.get("horaFin").toString(),    TIME_PARSE);
                    return rS.isBefore(newEnd) && rE.isAfter(newStart);
                })
                .collect(Collectors.toList());

        log.info("Registros superpuestos encontrados: {} para horario={}-{}",
                superpuestos.size(), request.getHoraInicio(), request.getHoraFin());

        List<Object> resultados = new ArrayList<>();

        for (Map<String, Object> reg : superpuestos) {
            Long      idReg = ((Number) reg.get("id")).longValue();
            LocalTime rE    = LocalTime.parse(reg.get("horaFin").toString(), TIME_PARSE);

            if (rE.isAfter(newEnd)) {
                // El registro termina después del nuevo → moverlo para que empiece en newEnd
                log.info("Moviendo registro id={} para que empiece en {}", idReg, newEnd.format(TIME_FMT));
                resultados.add(actualizarRegistro(idReg,
                        buildUpdateBody(reg, idEmpleado, newEnd.format(TIME_FMT), rE.format(TIME_FMT)),
                        token, request));
            } else {
                // El registro queda completamente dentro del nuevo intervalo — no se edita
                log.info("Registro id={} queda dentro del nuevo intervalo, se omite edición", idReg);
            }
        }

        resultados.add(ejecutarRegistroConHorario(request, idEmpleado, token,
                request.getHoraInicio(), request.getHoraFin()));

        log.info("Separación completada: {} operación(es) idEmpleado={} fecha={} horario={}-{}",
                resultados.size(), idEmpleado, request.getFechaRegistro(),
                request.getHoraInicio(), request.getHoraFin());

        return resultados;
    }

    private Object actualizarRegistro(Long idRegistro, Map<String, Object> body,
                                       String token, RegistrarActividadRequest request) {
        try {
            return doActualizarRegistro(idRegistro, body, token);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.warn("Token expirado al actualizar registro id={}, renovando...", idRegistro);
                return doActualizarRegistro(idRegistro, body,
                        tokenManager.renovarToken(request.getUsername(), request.getPassword()));
            }
            log.error("Error 4xx al actualizar registro id={} body={} response={}",
                    idRegistro, body, ex.getResponseBodyAsString());
            throw ex;
        } catch (HttpServerErrorException ex) {
            log.error("Error 5xx al actualizar registro id={} body={} response={}",
                    idRegistro, body, ex.getResponseBodyAsString());
            throw ex;
        }
    }

    private Object doActualizarRegistro(Long idRegistro, Map<String, Object> body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> bodyConId = new LinkedHashMap<>(body);
        bodyConId.put("id", idRegistro);

        log.info("PUT {} id={} horaInicio={} horaFin={}",
                REGISTRAR_ACTIVIDAD_URL, idRegistro, body.get("horaInicio"), body.get("horaFin"));

        ResponseEntity<Object> response = restTemplate.exchange(
                REGISTRAR_ACTIVIDAD_URL, HttpMethod.PUT,
                new HttpEntity<>(bodyConId, headers), Object.class);

        log.info("Registro actualizado id={}", idRegistro);
        return response.getBody();
    }

    private Map<String, Object> buildUpdateBody(Map<String, Object> reg, Long idEmpleado,
                                                 String horaInicio, String horaFin) {
        log.info("Campos del registro existente en Scoca: {}", reg.keySet());
        log.info("Valores del registro existente: {}", reg);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("idEmpleado",      idEmpleado);
        body.put("idActividad",     reg.get("idActividad"));
        body.put("idTipoActividad", reg.get("idTipoActividad"));
        body.put("idProyecto",      reg.get("idProyecto"));
        body.put("descripcion",     reg.get("descripcion"));
        body.put("fechaRegistro",   reg.get("fechaRegistro"));
        body.put("horaInicio",      horaInicio);
        body.put("horaFin",         horaFin);
        return body;
    }

    private Object ejecutarRegistro(RegistrarActividadRequest request, Long idEmpleado, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("idEmpleado",      idEmpleado);
        body.put("idActividad",     request.getIdActividad());
        body.put("idTipoActividad", request.getIdTipoActividad());
        body.put("idProyecto",      request.getIdProyecto());
        body.put("descripcion",     request.getDescripcion());
        body.put("fechaRegistro",   request.getFechaRegistro().toString());
        body.put("horaInicio",      request.getHoraInicio());
        body.put("horaFin",         request.getHoraFin());

        ResponseEntity<Object> response = restTemplate.postForEntity(
                REGISTRAR_ACTIVIDAD_URL, new HttpEntity<>(body, headers), Object.class);
        log.info("Actividad registrada en bitácora idEmpleado={} idProyecto={}", idEmpleado, request.getIdProyecto());
        return response.getBody();
    }

    private Object ejecutarRegistroConHorario(RegistrarActividadRequest request,
                                               Long idEmpleado, String token,
                                               String horaInicio, String horaFin) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("idEmpleado",      idEmpleado);
        body.put("idActividad",     request.getIdActividad());
        body.put("idTipoActividad", request.getIdTipoActividad());
        body.put("idProyecto",      request.getIdProyecto());
        body.put("descripcion",     request.getDescripcion());
        body.put("fechaRegistro",   request.getFechaRegistro().toString());
        body.put("horaInicio",      horaInicio);
        body.put("horaFin",         horaFin);

        ResponseEntity<Object> response = restTemplate.postForEntity(
                REGISTRAR_ACTIVIDAD_URL, new HttpEntity<>(body, headers), Object.class);
        log.info("Franja registrada idEmpleado={} idProyecto={} horario={}-{}",
                idEmpleado, request.getIdProyecto(), horaInicio, horaFin);
        return response.getBody();
    }

    private Object ejecutarConsulta(Long idEmpleado, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> response = restTemplate.exchange(
                BITACORA_URL, HttpMethod.GET, new HttpEntity<>(headers), Object.class, idEmpleado);
        log.info("Proyectos obtenidos idEmpleado={}", idEmpleado);
        return response.getBody();
    }

    private Object ejecutarGetSimple(String url, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
        log.info("GET {} completado", url);
        return response.getBody();
    }

    private Object ejecutarGetConVariable(String url, String token, Object... uriVars) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), Object.class, uriVars);
        log.info("GET {} vars={} completado", url, uriVars);
        return response.getBody();
    }

    private Object ejecutarGetPublico(String url) {
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY, Object.class);
        log.info("GET publico {} completado", url);
        return response.getBody();
    }

    private Object ejecutarGetPublicoConVariable(String url, Object... uriVars) {
        ResponseEntity<Object> response = restTemplate.exchange(
                url, HttpMethod.GET, HttpEntity.EMPTY, Object.class, uriVars);
        log.info("GET publico {} vars={} completado", url, uriVars);
        return response.getBody();
    }
}
