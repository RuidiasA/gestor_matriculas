package com.example.matriculas.model;

import com.example.matriculas.model.enums.EstadoPago;
import com.example.matriculas.model.enums.TipoConcepto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "pension_cuotas", uniqueConstraints = @UniqueConstraint(columnNames = {
        "alumno_id", "periodo_academico", "tipo_concepto", "numero_cuota"
}))
@Getter
@Setter
public class PensionCuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "alumno_id")
    private Alumno alumno;

    @ManyToOne
    @JoinColumn(name = "matricula_id")
    private Matricula matricula;

    @Column(name = "periodo_academico", nullable = false)
    private String periodoAcademico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_concepto", nullable = false)
    private TipoConcepto tipoConcepto;

    @Column(name = "numero_cuota")
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "importe_original")
    private Double importeOriginal;

    private Double mora = 0.0;

    private Double descuento = 0.0;

    @Column(name = "importe_final")
    private Double importeFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago")
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @PrePersist
    @PreUpdate
    public void recalcularImporteFinal() {
        double montoBase = importeOriginal != null ? importeOriginal : 0.0;
        double montoMora = mora != null ? mora : 0.0;
        double montoDescuento = descuento != null ? descuento : 0.0;
        this.importeFinal = montoBase + montoMora - montoDescuento;
    }

    public void actualizarEstadoPorVencimiento(LocalDate hoy) {
        if (estadoPago == EstadoPago.PENDIENTE
                && fechaVencimiento != null
                && fechaVencimiento.isBefore(hoy)) {
            estadoPago = EstadoPago.ATRASADO;
        }
    }
}
