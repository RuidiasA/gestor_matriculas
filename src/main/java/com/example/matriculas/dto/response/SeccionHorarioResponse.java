package com.example.matriculas.dto.response;

import com.example.matriculas.enums.DiaSemana;

import java.time.LocalTime;

public record SeccionHorarioResponse(
        Long id,
        DiaSemana dia,
        LocalTime horaInicio,
        LocalTime horaFin
) {}
