package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoDetalleMatricula;
import com.example.matriculas.model.enums.Modalidad;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalle_matricula")
@Getter
@Setter
public class DetalleMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @ManyToOne
    @JoinColumn(name = "seccion_id")
    private Seccion seccion;

    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    private Integer creditos;
    private Integer horasSemanales;
    private String aula;

    @Enumerated(EnumType.STRING)
    private Modalidad modalidad;

    private String horarioTexto;

    @Enumerated(EnumType.STRING)
    private EstadoDetalleMatricula estadoDetalle;

    private Double notaFinal;

    private String observacion;

    public DetalleMatricula() {}
}
