package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SeccionDisponibleDTO {
    private Long id;
    private String codigo;
    private String docente;
    private String aula;
    private String turno;
    private String modalidad;
    private Integer cuposDisponibles;
    private Integer matriculados;
}
