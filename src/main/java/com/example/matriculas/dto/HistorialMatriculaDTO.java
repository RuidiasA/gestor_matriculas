package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private Double matricula;
    private Double pension;
    private Double mora;
    private Double descuentos;
    private Double montoTotal;
    private List<CursoMatriculadoDTO> cursos;
}
