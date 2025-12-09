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
    private boolean pendiente;
}
