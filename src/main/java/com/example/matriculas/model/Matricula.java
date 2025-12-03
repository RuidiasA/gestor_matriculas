package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matriculas")
@Getter
@Setter
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    private String cicloAcademico;
    private LocalDateTime fechaMatricula;

    @Enumerated(EnumType.STRING)
    private EstadoMatricula estado;

    private Integer totalCreditos;
    private Integer totalHoras;

    private Double montoMatricula;
    private Double montoPension;
    private Double montoTotal;

    @OneToMany(mappedBy = "matricula")
    private List<DetalleMatricula> detalles;

    @OneToMany(mappedBy = "matricula")
    private List<Pago> pagos;

    public Matricula() {}
}
