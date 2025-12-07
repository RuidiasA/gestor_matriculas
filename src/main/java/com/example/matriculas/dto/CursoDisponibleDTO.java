package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CursoDisponibleDTO {
    private Long seccionId;
    private String codigoCurso;
    private String nombreCurso;
    private Integer creditos;
    private Integer horasSemanales;
    private String ciclo;
    private String docente;
    private String modalidad;
    private Integer cuposDisponibles;
    private Integer matriculados;
    private String turno;
}
