package com.example.matriculas.dto.response;

import com.example.matriculas.enums.EstadoMatricula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MatriculaResponse(
        Long id,
        Long alumnoId,
        String cicloAcademico,
        LocalDateTime fechaMatricula,
        EstadoMatricula estado,
        Integer totalCreditos,
        Integer totalHoras,
        BigDecimal montoMatricula,
        BigDecimal montoPension,
        BigDecimal montoTotal,
        List<MatriculaDetalleResponse> detalles,
        List<PagoResponse> pagos
) {}
