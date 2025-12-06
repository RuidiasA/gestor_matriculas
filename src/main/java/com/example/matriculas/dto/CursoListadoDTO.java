package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CursoListadoDTO {
    private Long id;
    private Long idCurso;
    private String codigo;
    private String nombre;
    private Integer ciclo;
    private String tipo;
    private Integer creditos;
    private String carrera;
    private Integer horasSemanales;
    private String modalidad;
}

