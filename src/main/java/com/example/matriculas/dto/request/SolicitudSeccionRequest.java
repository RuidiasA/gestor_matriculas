package com.example.matriculas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudSeccionRequest(
        @NotNull Long alumnoId,
        @NotNull Long cursoId,
        @NotBlank String modalidad,
        String turno,
        String telefono,
        @Email String correo,
        String motivo,
        String evidenciaNombreArchivo
) {}
