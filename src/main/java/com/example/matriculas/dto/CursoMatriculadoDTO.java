package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoMatriculadoDTO {
    private String codigoSeccion;
    private String nombreCurso;
    private String docente;
    private Integer creditos;
    private Integer horasSemanales;
    private String modalidad;
    private String aula;
}
