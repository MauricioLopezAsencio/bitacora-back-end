package com.spring.security.jwt.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String token;
    private String username;
}
