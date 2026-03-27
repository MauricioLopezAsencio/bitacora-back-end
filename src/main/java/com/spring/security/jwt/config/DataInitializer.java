package com.spring.security.jwt.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ct_usuarios WHERE cv_usuario = 'admin'", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update(
                    "INSERT INTO ct_usuarios (cv_usuario, ds_password, ds_nombre, bo_activo) VALUES (?, ?, ?, ?)",
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "Administrador",
                    true
            );
            log.info("Usuario admin creado con password por defecto");
        }
    }
}
