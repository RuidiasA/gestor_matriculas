package com.example.matriculas.model;

import com.example.matriculas.model.enums.Modalidad;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "secciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"codigo"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Código de sección: A1, B2, C3, 101, etc. (luego podemos combinar con curso.codigo si quieres IA401-A1) */
    @Column(nullable = false)
    private String codigo;

    /* Cupo máximo de alumnos */
    @Column(nullable = false)
    private int capacidad;

    /* Aula: Ejemplo D0204 (Torre D, piso 2, aula 04). Si es virtual = "Zoom". */
    @Column(nullable = false)
    private String aula;

    /* Modalidad de la sección, puede coincidir o diferir de la del curso */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    /* Relación con el curso */
    @ManyToOne
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    /* Docente asignado a la sección */
    @ManyToOne
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    /* Horarios de esta sección (1 o varios) */
    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeccionHorario> horarios = new ArrayList<>();

    /* Relación con matrícula detalle */
    @OneToMany(mappedBy = "seccion", fetch = FetchType.LAZY)
    private List<DetalleMatricula> matriculas = new ArrayList<>();
}
