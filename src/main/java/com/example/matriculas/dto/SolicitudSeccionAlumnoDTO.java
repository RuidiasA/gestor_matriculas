package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudSeccionAlumnoDTO {
    private Long id;
    private String curso;
    private String codigoCurso;
    private String ciclo;
    private String modalidad;
    private String turno;
    private String estado;
    private String respuesta;
    private LocalDateTime fechaSolicitud;
}
