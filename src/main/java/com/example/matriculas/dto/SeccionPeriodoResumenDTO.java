package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionPeriodoResumenDTO {
    private String periodo;
    private int matriculados;
    private int retirados;
    private int aprobados;
    private int desaprobados;
    private double porcentajeAprobacion;
    private double promedioNotas;
    private double promedioAsistencia;
}
