package com.example.matriculas.model;

import com.example.matriculas.enums.EstadoMatricula;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matriculas")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @Column(name = "ciclo_academico", nullable = false)
    private String cicloAcademico;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDateTime fechaMatricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatricula estado;

    @Column(name = "total_creditos")
    private Integer totalCreditos;

    @Column(name = "total_horas")
    private Integer totalHoras;

    @Column(name = "monto_matricula")
    private BigDecimal montoMatricula;

    @Column(name = "monto_pension")
    private BigDecimal montoPension;

    @Column(name = "monto_total")
    private BigDecimal montoTotal;

    @OneToMany(mappedBy = "matricula")
    private List<DetalleMatricula> detalles;

    @OneToMany(mappedBy = "matricula")
    private List<Pago> pagos;
}
