package com.example.matriculas.dto.request;

import com.example.matriculas.enums.DiaSemana;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record SeccionHorarioRequest(
        @NotNull DiaSemana dia,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {}
