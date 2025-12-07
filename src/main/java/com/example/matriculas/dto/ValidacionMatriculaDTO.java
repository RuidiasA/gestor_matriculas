package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ValidacionMatriculaDTO {
    private boolean puedeMatricular;
    private boolean tieneCupos;
    private boolean sinCruceHorario;
    private boolean prerrequisitosCumplidos;
    private boolean dentroDelPeriodo;
    private boolean creditosDisponibles;
    private List<String> mensajes;
}
