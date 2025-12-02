package com.example.matriculas.dto.response;

import com.example.matriculas.enums.EstadoDocente;

import java.util.List;

public record DocenteResponse(
        Long id,
        String codigoDocente,
        String dni,
        String nombres,
        String apellidos,
        String correoInstitucional,
        String correoPersonal,
        String telefonoPersonal,
        String direccion,
        String especialidad,
        EstadoDocente estado,
        Integer anioIngreso,
        Long usuarioId,
        List<Long> cursosDictables
) {}
