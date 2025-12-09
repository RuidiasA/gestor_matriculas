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
    private String estado;
    private String mensajeAdmin;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaActualizacion;
    private String evidenciaNombreArchivo;
    private String evidenciaContentType;
    private String evidenciaBase64;
    private Long solicitantes;
}
