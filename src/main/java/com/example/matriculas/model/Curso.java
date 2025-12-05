package com.example.matriculas.model;

import com.example.matriculas.model.enums.Modalidad;
import com.example.matriculas.model.enums.TipoCurso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "cursos")
@Getter
@Setter
@NoArgsConstructor
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

    private String descripcion;
    private Integer ciclo;
    private Integer creditos;

    @Column(name = "horas_semanales")
    private Integer horasSemanales;

    @Enumerated(EnumType.STRING)
    private TipoCurso tipo;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;

    @ManyToMany(mappedBy = "cursosDictados")
    private Set<Docente> docentes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "curso_prerrequisitos",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "prerrequisito_id")
    )
    private List<Curso> prerrequisitos;
}
