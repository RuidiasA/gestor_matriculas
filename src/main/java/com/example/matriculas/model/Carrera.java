package com.example.matriculas.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @OneToMany(mappedBy = "carrera")
    private List<Alumno> alumnos;

    @OneToMany(mappedBy = "carrera")
    private List<Curso> cursos;

    public Carrera() {}
}
