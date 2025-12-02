package com.example.matriculas.mapper;

import com.example.matriculas.dto.request.AlumnoRequest;
import com.example.matriculas.dto.response.AlumnoResponse;
import com.example.matriculas.model.Alumno;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AlumnoMapper {

    @Mapping(target = "carreraId", source = "carrera.id")
    @Mapping(target = "usuarioId", source = "usuario.id")
    AlumnoResponse toResponse(Alumno alumno);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrera", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "matriculas", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    @Mapping(target = "solicitudes", ignore = true)
    Alumno toEntity(AlumnoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrera", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "matriculas", ignore = true)
    @Mapping(target = "pagos", ignore = true)
    @Mapping(target = "solicitudes", ignore = true)
    void update(@MappingTarget Alumno alumno, AlumnoRequest request);
}
