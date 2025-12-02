package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenMatriculaDTO {
    private Integer totalCursos;
    private Integer totalCreditos;
    private Integer totalHoras;
    private Double matricula;
    private Double pension;
    private Double mora;
    private Double descuentos;
    private Double montoTotal;
}
