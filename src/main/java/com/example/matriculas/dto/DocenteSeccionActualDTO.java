package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteSeccionActualDTO {

    private String curso;
    private String codigoSeccion;
    private String periodo;
    private String modalidad;
    private Integer creditos;
    private String turno;
    private String horario;
    private String aula;
    private Integer estudiantesInscritos;
}
