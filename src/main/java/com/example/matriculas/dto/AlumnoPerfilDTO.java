package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AlumnoPerfilDTO {
    private String codigo;
    private String nombres;
    private String apellidos;
    private String correoInstitucional;
    private Integer cicloActual;
    private String carrera;
    private Integer ordenMerito;
    private Integer totalCreditosActuales;
    private ResumenMatriculaDTO resumenMatricula;
}
