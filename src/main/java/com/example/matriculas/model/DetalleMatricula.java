package com.example.matriculas.model;

import com.example.matriculas.enums.EstadoDetalleMatricula;
import com.example.matriculas.enums.Modalidad;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_matricula")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    @ManyToOne
    @JoinColumn(name = "seccion_id", nullable = false)
    private Seccion seccion;

    @ManyToOne
    @JoinColumn(name = "docente_id", nullable = false)
    private Docente docente;

    @Column(nullable = false)
    private Integer creditos;

    @Column(name = "horas_semanales", nullable = false)
    private Integer horasSemanales;

    @Column(nullable = false)
    private String aula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Modalidad modalidad;

    @Column(name = "horario_texto")
    private String horarioTexto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_detalle")
    private EstadoDetalleMatricula estadoDetalle;

    @Column(name = "nota_final", precision = 4, scale = 2)
    private BigDecimal notaFinal;

    @Column(columnDefinition = "TEXT")
    private String observacion;
}
