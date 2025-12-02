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
public class SeccionCambioDTO {
    private LocalDateTime fecha;
    private String usuario;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private String observacion;
}
