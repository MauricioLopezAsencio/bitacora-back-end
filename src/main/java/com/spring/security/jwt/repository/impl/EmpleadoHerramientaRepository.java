package com.spring.security.jwt.repository.impl;

import com.spring.security.jwt.model.EmpleadoHerramientaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EmpleadoHerramientaRepository extends JpaRepository<EmpleadoHerramientaModel, Long> {

    /**
     * Devuelve todas las asignaciones aún no devueltas que tienen timestamp de asignación.
     * JOIN FETCH evita N+1 al acceder a empleado y herramienta en el loop de recuperación.
     */
    @Query("SELECT eh FROM EmpleadoHerramientaModel eh " +
           "JOIN FETCH eh.empleado " +
           "JOIN FETCH eh.herramienta " +
           "WHERE eh.estatus = false AND eh.fcAsignacion IS NOT NULL")
    List<EmpleadoHerramientaModel> findActivasPendientesRecordatorio();
}
