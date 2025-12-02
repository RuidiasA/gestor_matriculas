package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoPago;
import com.example.matriculas.model.enums.TipoPago;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    private String periodo;
    private String concepto;

    @Enumerated(EnumType.STRING)
    private TipoPago tipo;

    private Double monto;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado;

    private LocalDate fechaPago;
    private LocalDate fechaVencimiento;

    public Pago() {}
}
