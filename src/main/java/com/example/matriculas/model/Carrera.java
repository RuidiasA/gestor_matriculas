package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carreras", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Código corto de la carrera: Ej: "SIS", "IND", "ADM" */
    @Column(nullable = false)
    private String codigo;

    /* Nombre completo: Ej: "Ingeniería de Sistemas" */
    @Column(nullable = false)
    private String nombre;

    /* Descripción opcional */
    private String descripcion;

    /* Relación con alumnos (una carrera tiene muchos alumnos) */
    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<Alumno> alumnos = new ArrayList<>();

    /* Relación con cursos (una carrera tiene muchos cursos) */
    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<Curso> cursos = new ArrayList<>();
}
