package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudSeccionListadoDTO {
    private Long id;
    private String curso;
    private String codigoCurso;
    private String carrera;
    private String ciclo;
    private Long solicitantes;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaActualizacion;
    private String mensajeAdmin;
    private String alumno;
    private String diaSolicitado;
    private String horaInicioSolicitada;
    private String horaFinSolicitada;
    private String modalidadSolicitada;
    private String turnoSolicitado;
}
