package com.example.matriculas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MatriculaRequest(
        @NotNull Long alumnoId,
        @NotBlank String cicloAcademico,
        List<MatriculaDetalleRequest> detalles
) {}
