package com.example.matriculas.dto.request;

import com.example.matriculas.enums.EstadoSeccion;
import com.example.matriculas.enums.Modalidad;
import com.example.matriculas.enums.Turno;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SeccionRequest(
        @NotNull Long cursoId,
        @NotNull Long docenteId,
        @NotBlank String codigo,
        @NotBlank String periodoAcademico,
        Turno turno,
        @NotBlank String aula,
        @NotNull Integer capacidad,
        Integer matriculadosActuales,
        @NotNull Modalidad modalidad,
        @NotNull EstadoSeccion estado,
        List<SeccionHorarioRequest> horarios
) {}
