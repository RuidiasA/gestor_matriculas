package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudSeccionAlumnoDTO {
    private Long id;
    private Long cursoId;
    private String curso;
    private String codigoCurso;
    private String ciclo;
    private String motivo;
    private String modalidad;
    private String turno;
    private String diaSolicitado;
    private String horaInicioSolicitada;
    private String horaFinSolicitada;
    private String modalidadSolicitada;
    private String turnoSolicitado;
    private String estado;
    private String mensajeAdmin;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaActualizacion;
    private String evidenciaNombreArchivo;
    private String evidenciaContentType;
    private String evidenciaUrl;
    private Long solicitantes;
}
