package com.example.matriculas.model;

import com.example.matriculas.model.enums.Modalidad;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "detalle_matricula")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Relación con la matrícula */
    @ManyToOne(optional = false)
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    /* Sección que eligió el alumno */
    @ManyToOne(optional = false)
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    /* Datos directos para evitar JOINs innecesarios (snapshot) */
    @Column(nullable = false)
    private Integer creditos;

    @Column(nullable = false)
    private Integer horasSemanales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    /* Aula (o "Zoom" si es virtual) */
    @Column(nullable = false)
    private String aula;

    /* Horario final (por ejemplo "Lun 08:00-10:00") */
    @Column(nullable = false)
    private String horarioTexto;
}
