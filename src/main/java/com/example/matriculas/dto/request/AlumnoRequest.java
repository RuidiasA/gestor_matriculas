package com.example.matriculas.dto.request;

import com.example.matriculas.enums.EstadoAlumno;
import com.example.matriculas.enums.Turno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlumnoRequest(
        @NotBlank String codigoAlumno,
        @NotBlank @Size(min = 8, max = 8) String dni,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank @Email String correoInstitucional,
        @Email String correoPersonal,
        @Size(min = 9, max = 9) String telefonoPersonal,
        String direccion,
        @NotNull Integer anioIngreso,
        Integer cicloActual,
        Turno turno,
        @NotNull EstadoAlumno estado,
        Integer ordenMerito,
        @NotNull Long carreraId,
        Long usuarioId
) {}
