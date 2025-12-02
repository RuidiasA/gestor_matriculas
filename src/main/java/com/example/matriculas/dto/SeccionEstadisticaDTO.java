package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionEstadisticaDTO {
    private int matriculadosActuales;
    private int cuposLibres;
    private int aprobadosUltimoPeriodo;
    private double porcentajeAprobacion;
    private int retiros;
    private int horariosProgramados;
    private int creditos;
    private String ciclo;
    private String turno;
    private int horasSemanales;
    private int cuposTotales;
    private int cuposDisponibles;
    private int cantidadHorarios;
    private String estadoAcademico;
    private List<SeccionPeriodoResumenDTO> resumenPeriodos;
}
