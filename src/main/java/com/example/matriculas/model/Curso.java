package com.example.matriculas.model;

import com.example.matriculas.model.enums.Modalidad;
import com.example.matriculas.model.enums.TipoCurso;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cursos", uniqueConstraints = {
        @UniqueConstraint(columnNames = "codigo")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Código único del curso: IA401, BD302, INF205, etc. */
    @Column(nullable = false)
    private String codigo;

    /* Nombre del curso */
    @Column(nullable = false)
    private String nombre;

    /* Descripción del curso */
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /* Créditos del curso */
    @Column(nullable = false)
    private int creditos;

    /* Horas semanales */
    @Column(name = "horas_semanales", nullable = false)
    private int horasSemanales;

    /* Ciclo sugerido / nivel del curso */
    @Column(nullable = false)
    private int ciclo;

    /* Tipo del curso: OBLIGATORIO / ELECTIVO */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCurso tipo;

    /* Modalidad: PRESENCIAL / VIRTUAL / SEMIPRESENCIAL */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    /* Carrera a la que pertenece el curso */
    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;

    /* Prerrequisitos del curso (Muchos a Muchos hacia sí mismo) */
    @ManyToMany
    @JoinTable(
            name = "curso_prerrequisitos",
            joinColumns = @JoinColumn(name = "curso_id"),
            inverseJoinColumns = @JoinColumn(name = "prerrequisito_id")
    )
    private List<Curso> prerrequisitos;

    /* Relación con las secciones del curso */
    @OneToMany(mappedBy = "curso", fetch = FetchType.LAZY)
    private List<Seccion> secciones;
}
