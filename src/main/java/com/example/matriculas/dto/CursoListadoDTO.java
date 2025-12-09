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
    private Long carreraId;
    private String carrera;
    private Integer ciclo;
    private String tipo;
    private String modalidad;
    private Integer creditos;
    private Integer horasSemanales;
}

