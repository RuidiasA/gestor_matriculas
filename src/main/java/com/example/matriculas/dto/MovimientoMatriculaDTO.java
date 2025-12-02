package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoMatriculaDTO {
    private String codigo;
    private String nombre;
    private String accion;
    private LocalDateTime fecha;
    private String usuario;
    private String notaFinal;
    private String observacion;
}
