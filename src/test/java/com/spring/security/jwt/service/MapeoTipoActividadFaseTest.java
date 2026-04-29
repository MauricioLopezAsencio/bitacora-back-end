package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.Fase;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MapeoTipoActividadFaseTest {

    private final MapeoTipoActividadFase mapeo = new MapeoTipoActividadFase();

    @Test
    void resolverDevuelveFaseCorrectaParaCadaActividadDelCatalogo() {
        Map<String, Fase> esperados = Map.ofEntries(
                Map.entry("Análisis",                    Fase.ANALISIS_DISENO),
                Map.entry("Arquitectura",                Fase.DESARROLLO_CONSTRUCCION),
                Map.entry("Atención de defecto",         Fase.PRUEBAS),
                Map.entry("Bases de datos",              Fase.DESARROLLO_CONSTRUCCION),
                Map.entry("Capacitación",                Fase.DESPLIEGUE),
                Map.entry("Capacitación al usuario",     Fase.DESPLIEGUE),
                Map.entry("Codificación",                Fase.DESARROLLO_CONSTRUCCION),
                Map.entry("Desarrollo",                  Fase.DESARROLLO_CONSTRUCCION),
                Map.entry("Despliegue",                  Fase.DESPLIEGUE),
                Map.entry("Diseño",                      Fase.ANALISIS_DISENO)
        );
        esperados.forEach((nombre, fase) ->
                assertThat(mapeo.resolver(nombre))
                        .as("nombre=%s", nombre)
                        .isEqualTo(fase));
    }

    @Test
    void resolverDevuelveFaseCorrectaParaActividadesRestantes() {
        Map<String, Fase> esperados = Map.ofEntries(
                Map.entry("Diversos",                    Fase.GARANTIA),
                Map.entry("Elaboración de documentos",   Fase.ANALISIS_DISENO),
                Map.entry("Entregables",                 Fase.DESPLIEGUE),
                Map.entry("Implementación",              Fase.DESPLIEGUE),
                Map.entry("Investigación",               Fase.ANALISIS_DISENO),
                Map.entry("Legales y trámites",          Fase.GARANTIA),
                Map.entry("Plan de trabajo",             Fase.ANALISIS_DISENO),
                Map.entry("Pruebas",                     Fase.PRUEBAS),
                Map.entry("Reportes",                    Fase.GARANTIA),
                Map.entry("Seguimiento a cumplimiento",  Fase.GARANTIA),
                Map.entry("Seguridad de la información", Fase.DESARROLLO_CONSTRUCCION),
                Map.entry("Sesión externa",              Fase.GARANTIA),
                Map.entry("Sesión interna",              Fase.GARANTIA),
                Map.entry("Soporte",                     Fase.GARANTIA),
                Map.entry("Tableros",                    Fase.DESARROLLO_CONSTRUCCION),
                Map.entry("Ventas/comercial",            Fase.GARANTIA)
        );
        esperados.forEach((nombre, fase) ->
                assertThat(mapeo.resolver(nombre))
                        .as("nombre=%s", nombre)
                        .isEqualTo(fase));
    }

    @Test
    void resolverEsCaseInsensitive() {
        assertThat(mapeo.resolver("ANÁLISIS")).isEqualTo(Fase.ANALISIS_DISENO);
        assertThat(mapeo.resolver("desarrollo")).isEqualTo(Fase.DESARROLLO_CONSTRUCCION);
        assertThat(mapeo.resolver("PrUeBaS")).isEqualTo(Fase.PRUEBAS);
    }

    @Test
    void resolverIgnoraAcentos() {
        assertThat(mapeo.resolver("Analisis")).isEqualTo(Fase.ANALISIS_DISENO);
        assertThat(mapeo.resolver("Codificacion")).isEqualTo(Fase.DESARROLLO_CONSTRUCCION);
        assertThat(mapeo.resolver("Sesion interna")).isEqualTo(Fase.GARANTIA);
        assertThat(mapeo.resolver("Capacitacion al usuario")).isEqualTo(Fase.DESPLIEGUE);
    }

    @Test
    void resolverIgnoraEspaciosExtremos() {
        assertThat(mapeo.resolver("  Análisis  ")).isEqualTo(Fase.ANALISIS_DISENO);
    }

    @Test
    void resolverDevuelveNoAplicaParaActividadDesconocida() {
        assertThat(mapeo.resolver("Actividad inexistente")).isEqualTo(Fase.NO_APLICA);
        assertThat(mapeo.resolver("Foo bar baz")).isEqualTo(Fase.NO_APLICA);
    }

    @Test
    void resolverDevuelveNoAplicaParaNullOBlank() {
        assertThat(mapeo.resolver(null)).isEqualTo(Fase.NO_APLICA);
        assertThat(mapeo.resolver("")).isEqualTo(Fase.NO_APLICA);
        assertThat(mapeo.resolver("   ")).isEqualTo(Fase.NO_APLICA);
    }

    @Test
    void mapeoExpuestoTieneVeintiseisEntradas() {
        assertThat(mapeo.obtenerMapeo()).hasSize(26);
    }
}
