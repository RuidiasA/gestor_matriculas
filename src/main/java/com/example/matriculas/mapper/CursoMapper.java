package com.example.matriculas.mapper;

import com.example.matriculas.dto.request.CursoRequest;
import com.example.matriculas.dto.response.CursoResponse;
import com.example.matriculas.model.Curso;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CursoMapper {

    @Mapping(target = "carreraId", source = "carrera.id")
    @Mapping(target = "prerrequisitos", expression = "java(curso.getPrerrequisitos() == null ? java.util.List.<Long>of() : curso.getPrerrequisitos().stream().map(p -> p.getId()).toList())")
    CursoResponse toResponse(Curso curso);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrera", ignore = true)
    @Mapping(target = "prerrequisitos", ignore = true)
    @Mapping(target = "docentes", ignore = true)
    @Mapping(target = "secciones", ignore = true)
    @Mapping(target = "solicitudes", ignore = true)
    Curso toEntity(CursoRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "carrera", ignore = true)
    @Mapping(target = "prerrequisitos", ignore = true)
    @Mapping(target = "docentes", ignore = true)
    @Mapping(target = "secciones", ignore = true)
    @Mapping(target = "solicitudes", ignore = true)
    void update(@MappingTarget Curso curso, CursoRequest request);
}
