package com.spring.security.jwt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class BitacoraService implements IBitacoraService {

    private static final String BITACORA_URL =
            "https://scoca.casystem.com.mx/api/bitacora/proyectos/byEmpleado/{idEmpleado}";

    private final RestTemplate restTemplate;
    private final BitacoraTokenManager tokenManager;

    public BitacoraService(RestTemplate restTemplate, BitacoraTokenManager tokenManager) {
        this.restTemplate = restTemplate;
        this.tokenManager = tokenManager;
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

    private Object ejecutarConsulta(Long idEmpleado, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Object> response = restTemplate.exchange(
                BITACORA_URL, HttpMethod.GET, new HttpEntity<>(headers), Object.class, idEmpleado);
        log.info("Proyectos obtenidos idEmpleado={}", idEmpleado);
        return response.getBody();
    }
}
