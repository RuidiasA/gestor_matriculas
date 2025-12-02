package com.example.matriculas.dto.response;

import com.example.matriculas.enums.EstadoPago;
import com.example.matriculas.enums.TipoPago;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoResponse(
        Long id,
        Long matriculaId,
        Long alumnoId,
        String periodo,
        String concepto,
        TipoPago tipo,
        BigDecimal monto,
        EstadoPago estado,
        LocalDate fechaPago,
        LocalDate fechaVencimiento
) {}
