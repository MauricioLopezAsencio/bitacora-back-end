package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.RegistrarActividadRequest;
import com.spring.security.jwt.exception.NegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BitacoraServiceFaseTest {

    private static final int ID_TIPO_SERVICIO = 4;
    private static final int ID_TIPO_SESION   = 3;

    private RestTemplate         restTemplate;
    private BitacoraTokenManager tokenManager;
    private BitacoraService      bitacoraService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        tokenManager = mock(BitacoraTokenManager.class);
        bitacoraService = new BitacoraService(restTemplate, tokenManager);
        ReflectionTestUtils.setField(bitacoraService, "idTipoActividadServicio", ID_TIPO_SERVICIO);

        lenient().when(tokenManager.obtenerIdEmpleado(anyString(), anyString())).thenReturn(123L);
        lenient().when(tokenManager.obtenerToken(anyString(), anyString())).thenReturn("tok");

        // Sin registros existentes para cada prueba (la fecha real depende del request)
        lenient().when(restTemplate.exchange(
                anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Object.class), any(Object[].class)
        )).thenReturn(ResponseEntity.ok(Collections.emptyList()));

        lenient().when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(ResponseEntity.ok((Object) Map.of("ok", true)));
    }

    @Test
    void registrarConTipoServicioYFaseEnviaFaseEnElBody() {
        RegistrarActividadRequest request = baseRequest(ID_TIPO_SERVICIO);
        request.setFase("DESARROLLO_CONSTRUCCION");

        bitacoraService.registrarActividadConParticion(request);

        Map<String, Object> body = capturarBodyDelPost();
        assertThat(body).containsEntry("fase", "DESARROLLO_CONSTRUCCION");
    }

    @Test
    void registrarConTipoServicioSinFaseLanzaNegocioException() {
        RegistrarActividadRequest request = baseRequest(ID_TIPO_SERVICIO);
        request.setFase(null);

        assertThatThrownBy(() -> bitacoraService.registrarActividadConParticion(request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("fase");
    }

    @Test
    void registrarConTipoServicioYFaseBlankLanzaNegocioException() {
        RegistrarActividadRequest request = baseRequest(ID_TIPO_SERVICIO);
        request.setFase("   ");

        assertThatThrownBy(() -> bitacoraService.registrarActividadConParticion(request))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("fase");
    }

    @Test
    void registrarConTipoNoServicioNoEnviaFaseAunqueVengaEnElRequest() {
        RegistrarActividadRequest request = baseRequest(ID_TIPO_SESION);
        request.setFase("DESARROLLO_CONSTRUCCION");

        bitacoraService.registrarActividadConParticion(request);

        Map<String, Object> body = capturarBodyDelPost();
        assertThat(body).doesNotContainKey("fase");
    }

    @Test
    void registrarConTipoNoServicioYSinFaseNoLanzaError() {
        RegistrarActividadRequest request = baseRequest(ID_TIPO_SESION);
        request.setFase(null);

        bitacoraService.registrarActividadConParticion(request);

        Map<String, Object> body = capturarBodyDelPost();
        assertThat(body).doesNotContainKey("fase");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> capturarBodyDelPost() {
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Object.class));
        return captor.getValue().getBody();
    }

    private RegistrarActividadRequest baseRequest(int idTipoActividad) {
        RegistrarActividadRequest r = new RegistrarActividadRequest();
        r.setUsername("u");
        r.setPassword("p");
        r.setIdActividad(8L);
        r.setIdTipoActividad(idTipoActividad);
        r.setIdProyecto(1L);
        r.setDescripcion("desc");
        r.setFechaRegistro(LocalDate.of(2026, 4, 29));
        r.setHoraInicio("09:00");
        r.setHoraFin("10:00");
        return r;
    }
}
