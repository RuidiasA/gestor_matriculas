package com.example.matriculas.dto.response;

import com.example.matriculas.enums.EstadoDetalleMatricula;
import com.example.matriculas.enums.Modalidad;

import java.math.BigDecimal;

public record MatriculaDetalleResponse(
        Long id,
        Long seccionId,
        Long docenteId,
        Integer creditos,
        Integer horasSemanales,
        String aula,
        Modalidad modalidad,
        String horarioTexto,
        EstadoDetalleMatricula estadoDetalle,
        BigDecimal notaFinal,
        String observacion
) {}
