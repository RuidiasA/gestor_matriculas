package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoPeriodoAcademico;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "periodos_academicos")
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING)
    private EstadoPeriodoAcademico estado;

    public PeriodoAcademico() {}
}
