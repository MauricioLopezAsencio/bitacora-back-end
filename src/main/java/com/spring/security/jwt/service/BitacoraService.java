package com.spring.security.jwt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    public BitacoraService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Object obtenerProyectosPorEmpleado(Long idEmpleado, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Object> response = restTemplate.exchange(
                    BITACORA_URL,
                    HttpMethod.GET,
                    entity,
                    Object.class,
                    idEmpleado
            );
            log.info("Proyectos obtenidos idEmpleado={}", idEmpleado);
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            log.error("Error al consultar bitácora idEmpleado={} status={} body={}",
                    idEmpleado, ex.getStatusCode(), ex.getResponseBodyAsString());
            throw ex;
        }
    }
}
