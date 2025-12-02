package com.example.matriculas.dto.response;

import com.example.matriculas.enums.Modalidad;
import com.example.matriculas.enums.TipoCurso;

import java.util.List;

public record CursoResponse(
        Long id,
        Long carreraId,
        String codigo,
        String nombre,
        String descripcion,
        Integer ciclo,
        Integer creditos,
        Integer horasSemanales,
        TipoCurso tipo,
        Modalidad modalidad,
        List<Long> prerrequisitos
) {}
