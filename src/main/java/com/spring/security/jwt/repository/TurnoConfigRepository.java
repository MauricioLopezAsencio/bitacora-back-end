package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.TurnoConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TurnoConfigRepository extends JpaRepository<TurnoConfigModel, Long> {

    Optional<TurnoConfigModel> findByCvTurno(String cvTurno);
}
