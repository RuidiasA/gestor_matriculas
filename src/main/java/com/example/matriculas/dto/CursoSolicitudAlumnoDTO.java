package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CursoSolicitudAlumnoDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String ciclo;
    private String carrera;
    private Long carreraId;
    private String modalidad;
    private boolean pendiente;
}
