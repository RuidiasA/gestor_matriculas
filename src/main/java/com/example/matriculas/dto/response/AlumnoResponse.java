package com.example.matriculas.dto.response;

import com.example.matriculas.enums.EstadoAlumno;
import com.example.matriculas.enums.Turno;

public record AlumnoResponse(
        Long id,
        String codigoAlumno,
        String dni,
        String nombres,
        String apellidos,
        String correoInstitucional,
        String correoPersonal,
        String telefonoPersonal,
        String direccion,
        Integer anioIngreso,
        Integer cicloActual,
        Turno turno,
        EstadoAlumno estado,
        Integer ordenMerito,
        Long carreraId,
        Long usuarioId
) {}
