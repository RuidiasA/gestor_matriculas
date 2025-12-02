package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoSeccion;
import com.example.matriculas.model.enums.Modalidad;
import com.example.matriculas.model.enums.Turno;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "secciones")
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToOne
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(name = "periodo_academico", nullable = false)
    private String periodoAcademico;

    @Enumerated(EnumType.STRING)
    private Turno turno;

    private String aula;
    private Integer capacidad;
    private Integer matriculadosActuales;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;

    @Enumerated(EnumType.STRING)
    private EstadoSeccion estado;

    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "seccion")
    private List<SeccionHorario> horarios;

    @OneToMany(mappedBy = "seccion")
    private List<SeccionCambio> cambios;

    public Seccion() {}
}
