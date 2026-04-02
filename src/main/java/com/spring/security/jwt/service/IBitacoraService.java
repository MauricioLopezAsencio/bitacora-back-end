package com.spring.security.jwt.service;

import com.spring.security.jwt.dto.RegistrarActividadRequest;

public interface IBitacoraService {

    Object obtenerProyectosPorEmpleado(Long idEmpleado, String username, String password);

    Long obtenerIdEmpleado(String username, String password);

    Object registrarActividad(RegistrarActividadRequest request);
}
