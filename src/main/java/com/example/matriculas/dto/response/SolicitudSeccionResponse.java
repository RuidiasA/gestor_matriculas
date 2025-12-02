package com.example.matriculas.dto.response;

import java.time.LocalDateTime;

public record SolicitudSeccionResponse(
        Long id,
        Long alumnoId,
        Long cursoId,
        LocalDateTime fechaSolicitud,
        String modalidad,
        String turno,
        String telefono,
        String correo,
        String motivo,
        String evidenciaNombreArchivo
) {}
