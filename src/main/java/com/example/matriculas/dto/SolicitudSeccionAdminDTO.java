package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SolicitudSeccionAdminDTO {
    private Long id;
    private String alumno;
    private String codigoAlumno;
    private String curso;
    private String codigoCurso;
    private String motivo;
    private String modalidad;
    private String turno;
    private String diaSolicitado;
    private String horaInicioSolicitada;
    private String horaFinSolicitada;
    private String modalidadSolicitada;
    private String turnoSolicitado;
    private String telefono;
    private String correo;
    private String estado;
    private String mensajeAdmin;
    private String ciclo;
    private LocalDateTime fechaSolicitud;
}
