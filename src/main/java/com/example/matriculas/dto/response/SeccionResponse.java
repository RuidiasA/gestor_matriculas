package com.example.matriculas.dto.response;

import com.example.matriculas.enums.EstadoSeccion;
import com.example.matriculas.enums.Modalidad;
import com.example.matriculas.enums.Turno;

import java.time.LocalDateTime;
import java.util.List;

public record SeccionResponse(
        Long id,
        Long cursoId,
        Long docenteId,
        String codigo,
        String periodoAcademico,
        Turno turno,
        String aula,
        Integer capacidad,
        Integer matriculadosActuales,
        Modalidad modalidad,
        EstadoSeccion estado,
        LocalDateTime fechaCreacion,
        List<SeccionHorarioResponse> horarios
) {}
