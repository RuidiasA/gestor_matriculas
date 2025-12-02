package com.example.matriculas.model;

import com.example.matriculas.enums.Modalidad;
import com.example.matriculas.enums.TipoCurso;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cursos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private Integer ciclo;

    @Column(nullable = false)
    private Integer creditos;

    @Column(name = "horas_semanales", nullable = false)
    private Integer horasSemanales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCurso tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    @ManyToMany
    @JoinTable(
            name = "curso_prerrequisitos",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "prerrequisito_id")
    )
    private List<Curso> prerrequisitos;

    @ManyToMany(mappedBy = "cursosDictables")
    private List<Docente> docentes;

    @OneToMany(mappedBy = "curso")
    private List<Seccion> secciones;

    @OneToMany(mappedBy = "curso")
    private List<SolicitudSeccion> solicitudes;
}
