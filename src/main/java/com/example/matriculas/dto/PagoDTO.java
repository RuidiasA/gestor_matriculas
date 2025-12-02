package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PagoDTO {
    private Long id;
    private String concepto;
    private String periodo;
    private Double monto;
    private LocalDate vencimiento;
    private String estado;
    private LocalDate fechaPago;
}
