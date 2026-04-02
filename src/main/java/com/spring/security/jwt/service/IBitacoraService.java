package com.spring.security.jwt.service;

public interface IBitacoraService {

    Object obtenerProyectosPorEmpleado(Long idEmpleado, String token);
}
