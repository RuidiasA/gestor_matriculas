package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HorarioDTO {
    private String curso;
    private String dia;
    private String horaInicio;
    private String horaFin;
    private String aula;
    private String docente;
}
