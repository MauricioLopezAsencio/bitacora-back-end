package com.spring.security.jwt.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cat_herramientas")
public class HerramientaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", length = 250)
    private String nombre;

    @Column(name = "categoria", length = 250)
    private String categoria;

    @OneToMany(mappedBy = "herramienta")
    private List<EmpleadoHerramientaModel> empleados; // Cambia a una lista de la tabla pivote
}
