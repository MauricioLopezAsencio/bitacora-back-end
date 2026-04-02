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
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class BitacoraService implements IBitacoraService {

    private static final String BITACORA_URL =
            "https://scoca.casystem.com.mx/api/bitacora/proyectos/byEmpleado/{idEmpleado}";

    private static final String REGISTRAR_ACTIVIDAD_URL =
            "https://scoca.casystem.com.mx/api/bitacora";

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

    private Object ejecutarConsulta(Long idEmpleado, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> response = restTemplate.exchange(
                BITACORA_URL, HttpMethod.GET, new HttpEntity<>(headers), Object.class, idEmpleado);
        log.info("Proyectos obtenidos idEmpleado={}", idEmpleado);
        return response.getBody();
    }
}
