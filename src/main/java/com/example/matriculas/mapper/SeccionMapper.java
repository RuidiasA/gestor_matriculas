package com.example.matriculas.mapper;

import com.example.matriculas.dto.request.SeccionRequest;
import com.example.matriculas.dto.response.SeccionResponse;
import com.example.matriculas.model.Seccion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = SeccionHorarioMapper.class)
public interface SeccionMapper {

    @Mapping(target = "cursoId", source = "curso.id")
    @Mapping(target = "docenteId", source = "docente.id")
    SeccionResponse toResponse(Seccion seccion);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "curso", ignore = true)
    @Mapping(target = "docente", ignore = true)
    @Mapping(target = "horarios", ignore = true)
    @Mapping(target = "cambios", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    Seccion toEntity(SeccionRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "curso", ignore = true)
    @Mapping(target = "docente", ignore = true)
    @Mapping(target = "horarios", ignore = true)
    @Mapping(target = "cambios", ignore = true)
    @Mapping(target = "detalles", ignore = true)
    void update(@MappingTarget Seccion seccion, SeccionRequest request);
}
