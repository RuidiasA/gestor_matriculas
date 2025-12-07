package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HistorialAlumnoDTO {
    private String ciclo;
    private String estado;
    private Integer totalCreditos;
    private Integer totalHoras;
    private List<CursoMatriculadoDTO> cursos;
}
