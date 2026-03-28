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
    /** Turno del empleado al momento del préstamo. Valores: "DIA" o "NOCHE" */
    private String turno;
}
