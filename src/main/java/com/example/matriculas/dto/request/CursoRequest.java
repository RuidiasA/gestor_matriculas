package com.example.matriculas.dto.request;

import com.example.matriculas.enums.Modalidad;
import com.example.matriculas.enums.TipoCurso;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CursoRequest(
        @NotNull Long carreraId,
        @NotBlank String codigo,
        @NotBlank String nombre,
        String descripcion,
        @NotNull Integer ciclo,
        @NotNull Integer creditos,
        @NotNull Integer horasSemanales,
        @NotNull TipoCurso tipo,
        @NotNull Modalidad modalidad,
        List<Long> prerrequisitos
) {}
