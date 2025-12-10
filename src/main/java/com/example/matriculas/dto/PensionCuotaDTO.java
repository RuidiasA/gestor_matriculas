package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PensionCuotaDTO {
    private Long id;
    private String concepto;
    private LocalDate vencimiento;
    private Double monto;
    private Double mora;
    private Double descuento;
    private Double importeFinal;
    private String estado;
    private LocalDate fechaPago;
    private Integer numeroCuota;
    private String tipoConcepto;
}
