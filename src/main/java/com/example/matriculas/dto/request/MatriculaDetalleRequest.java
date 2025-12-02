package com.example.matriculas.dto.request;

import com.example.matriculas.enums.Modalidad;
import jakarta.validation.constraints.NotNull;

public record MatriculaDetalleRequest(
        @NotNull Long seccionId,
        @NotNull Long docenteId,
        @NotNull Integer creditos,
        @NotNull Integer horasSemanales,
        @NotNull String aula,
        @NotNull Modalidad modalidad,
        String horarioTexto
) {}
