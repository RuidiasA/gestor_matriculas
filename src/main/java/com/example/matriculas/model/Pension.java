package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoPension;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "pension",
        uniqueConstraints = @UniqueConstraint(columnNames = {"alumno_id", "periodo", "concepto"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* Alumno al que pertenece esta obligación/pago */
    @ManyToOne
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    /* Ejemplo: "2025-I" */
    @Column(nullable = false, length = 10)
    private String periodo;

    /* Ej: "MATRICULA", "PENSION 1", "PENSION 2", etc. */
    @Column(nullable = false, length = 50)
    private String concepto;

    /* Monto de este pago */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    /* Fecha límite de pago */
    private LocalDate vencimiento;

    /* Estado del pago: PAGADO / PENDIENTE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPension estado;

    /* Fecha en que se realizó el pago (si aplica) */
    private LocalDate fechaPago;
}
