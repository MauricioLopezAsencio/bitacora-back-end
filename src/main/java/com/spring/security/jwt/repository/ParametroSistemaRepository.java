package com.spring.security.jwt.repository;

import com.spring.security.jwt.model.ParametroSistemaModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParametroSistemaRepository extends JpaRepository<ParametroSistemaModel, Long> {
}
