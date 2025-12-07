package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CursoDetalleAlumnoDTO {
    private Long seccionId;
    private String codigoSeccion;
    private String codigoCurso;
    private String nombreCurso;
    private String descripcion;
    private Integer creditos;
    private Integer horasSemanales;
    private String docente;
    private String aula;
    private String turno;
    private String modalidad;
    private Integer cuposDisponibles;
    private Integer matriculados;
    private List<String> prerrequisitos;
    private List<HorarioDTO> horarios;
}
