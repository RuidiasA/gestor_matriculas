package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SolicitudSeccionDetalleDTO {
    private Long id;
    private String alumno;
    private String codigoAlumno;
    private String curso;
    private String codigoCurso;
    private String carrera;
    private String ciclo;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private String mensajeAdmin;
    private String motivo;
    private List<SolicitudSeccionListadoDTO> relacionados;
}
