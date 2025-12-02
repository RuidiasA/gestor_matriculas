package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matriculas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Alumno que realizó esta matrícula */
    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    /* Ciclo académico: Ej: "2025-I", "2025-II" */
    @Column(nullable = false)
    private String cicloAcademico;

    /* Fecha exacta en la que se guardó la matrícula */
    @Column(nullable = false)
    private LocalDateTime fechaMatricula;

    /* Totales (calculados automáticamente) */
    @Column(nullable = false)
    private Integer totalCreditos;

    @Column(nullable = false)
    private Integer totalHoras;

    /* Monto total que debe pagar (créditos × 50 + matrícula fija) */
    @Column(nullable = false)
    private Double montoTotal;

    /* Estado: GENERADA, PAGADA o ANULADA */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatricula estado;

    /* Detalles de los cursos matriculados */
    @OneToMany(mappedBy = "matricula", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleMatricula> detalles = new ArrayList<>();
}
