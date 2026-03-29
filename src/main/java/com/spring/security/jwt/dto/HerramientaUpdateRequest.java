package com.spring.security.jwt.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HerramientaUpdateRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria")
    private String categoria;

    @NotNull(message = "La cantidad total es obligatoria")
    @Min(value = 1, message = "La cantidad total debe ser al menos 1")
    private Integer cantidadTotal;

    private boolean estatus;
}
