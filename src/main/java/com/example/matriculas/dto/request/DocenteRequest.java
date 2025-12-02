package com.example.matriculas.dto.request;

import com.example.matriculas.enums.EstadoDocente;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DocenteRequest(
        @NotBlank String codigoDocente,
        @NotBlank @Size(min = 8, max = 8) String dni,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank @Email String correoInstitucional,
        @Email String correoPersonal,
        @Size(min = 9, max = 9) String telefonoPersonal,
        String direccion,
        String especialidad,
        @NotNull EstadoDocente estado,
        Integer anioIngreso,
        Long usuarioId,
        List<Long> cursosDictables
) {}
