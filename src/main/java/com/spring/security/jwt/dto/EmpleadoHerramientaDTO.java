package com.spring.security.jwt.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EmpleadoHerramientaDTO {
    private Long empleadoId;
    private Long herramientaId;
}
