package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CursoDTO {

    private Long idCurso;
    private String codigo;
    private String nombre;
    private Integer creditos;
    private Integer ciclo;
}

