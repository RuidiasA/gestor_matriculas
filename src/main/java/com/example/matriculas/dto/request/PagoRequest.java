package com.example.matriculas.dto.request;

import com.example.matriculas.enums.TipoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PagoRequest(
        Long matriculaId,
        @NotNull Long alumnoId,
        @NotBlank String periodo,
        @NotBlank String concepto,
        @NotNull TipoPago tipo,
        @NotNull BigDecimal monto,
        LocalDate fechaVencimiento
) {}
