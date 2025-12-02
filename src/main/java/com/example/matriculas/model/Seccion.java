package com.example.matriculas.model;

import com.example.matriculas.enums.EstadoSeccion;
import com.example.matriculas.enums.Modalidad;
import com.example.matriculas.enums.Turno;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "secciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(nullable = false)
    private String aula;

    @Column(nullable = false)
    private Integer capacidad;

    @Column(name = "matriculados_actuales", nullable = false)
    private Integer matriculadosActuales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSeccion estado;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "seccion")
    private List<SeccionHorario> horarios;

    @OneToMany(mappedBy = "seccion")
    private List<SeccionCambio> cambios;

    @OneToMany(mappedBy = "seccion")
    private List<DetalleMatricula> detalles;
}
