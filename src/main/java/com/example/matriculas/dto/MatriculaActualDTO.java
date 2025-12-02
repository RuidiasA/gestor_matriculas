package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatriculaActualDTO {
    private Integer totalCreditos;
    private Integer totalHoras;
    private Double montoMatricula;
    private Double montoPensionMensual;
    private Double montoTotal;
}
