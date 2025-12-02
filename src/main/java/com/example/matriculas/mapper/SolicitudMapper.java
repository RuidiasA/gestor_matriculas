package com.example.matriculas.mapper;

import com.example.matriculas.dto.response.SolicitudSeccionResponse;
import com.example.matriculas.model.SolicitudSeccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudMapper {

    @Mapping(target = "alumnoId", source = "alumno.id")
    @Mapping(target = "cursoId", source = "curso.id")
    SolicitudSeccionResponse toResponse(SolicitudSeccion solicitud);
}
