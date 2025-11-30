package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialMatriculaDTO {
    private String ciclo;
    private String estado;
    private Integer totalCursos;
    private Integer totalCreditos;
    private Integer totalHoras;
    private Double montoTotal;
}
