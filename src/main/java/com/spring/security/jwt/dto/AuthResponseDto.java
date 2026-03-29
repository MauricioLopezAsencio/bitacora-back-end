package com.spring.security.jwt.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class AuthResponseDto {
    private String token;
    private String username;
    /** Momento exacto en que expira el token (UTC). El front usa este valor para saber cuándo renovar. */
    private Instant expiresAt;
}
