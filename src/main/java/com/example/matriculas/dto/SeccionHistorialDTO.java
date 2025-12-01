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
public class SeccionHistorialDTO {
    private Long matriculaId;
    private String alumnoCodigo;
    private String alumnoNombre;
    private String estadoMatricula;
    private String periodo;
    private LocalDateTime fechaMatricula;
    private String observacion;
}

