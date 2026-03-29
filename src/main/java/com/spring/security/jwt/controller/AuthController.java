package com.spring.security.jwt.controller;

import com.spring.security.jwt.dto.ApiResponse;
import com.spring.security.jwt.dto.AuthRequestDto;
import com.spring.security.jwt.dto.AuthResponseDto;
import com.spring.security.jwt.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin("*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(
            @Valid @RequestBody AuthRequestDto request,
            HttpServletRequest httpRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUser(), request.getPassword()));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Credenciales incorrectas", "INVALID_CREDENTIALS")
                            .toBuilder().path(httpRequest.getRequestURI()).build());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUser());
        String token = jwtService.generateToken(userDetails.getUsername());

        AuthResponseDto data = new AuthResponseDto();
        data.setToken(token);
        data.setUsername(userDetails.getUsername());
        data.setExpiresAt(jwtService.extractExpiration(token));

        log.info("Login exitoso user={}", request.getUser());
        return ResponseEntity.ok(ApiResponse.ok(data, "Login exitoso")
                .toBuilder().path(httpRequest.getRequestURI()).build());
    }

    /**
     * Renueva el token si el actual aún es válido.
     * El front debe llamar este endpoint antes de que expire (ej. cuando quedan 5 min).
     * Si el token ya expiró, devuelve 401 y el front redirige al login.
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refresh(HttpServletRequest httpRequest) {
        final String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Token no proporcionado", "MISSING_TOKEN")
                            .toBuilder().path(httpRequest.getRequestURI()).build());
        }

        final String oldToken = authHeader.substring(7);
        final String username;
        try {
            username = jwtService.extractUsername(oldToken);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Token inválido", "INVALID_TOKEN")
                            .toBuilder().path(httpRequest.getRequestURI()).build());
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(oldToken, userDetails)) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(401, "Token expirado o inválido", "TOKEN_EXPIRED")
                            .toBuilder().path(httpRequest.getRequestURI()).build());
        }

        String newToken = jwtService.generateToken(username);
        AuthResponseDto data = new AuthResponseDto();
        data.setToken(newToken);
        data.setUsername(username);
        data.setExpiresAt(jwtService.extractExpiration(newToken));

        log.info("Token renovado user={}", username);
        return ResponseEntity.ok(ApiResponse.ok(data, "Token renovado")
                .toBuilder().path(httpRequest.getRequestURI()).build());
    }
}
