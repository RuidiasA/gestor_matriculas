package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteHistorialSeccionDTO {

    private String periodo;
    private String curso;
    private String seccion;
    private String modalidad;
    private Integer creditos;
    private String turno;
    private String horario;
    private Integer estudiantesFinalizados;
    private Double notaPromedio;
    private Double porcentajeAprobacion;
    private String observaciones;
}
