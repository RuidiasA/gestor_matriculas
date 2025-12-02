package com.example.matriculas.mapper;

import com.example.matriculas.dto.request.DocenteRequest;
import com.example.matriculas.dto.response.DocenteResponse;
import com.example.matriculas.model.Docente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DocenteMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "cursosDictables", expression = "java(docente.getCursosDictables() == null ? java.util.List.<Long>of() : docente.getCursosDictables().stream().map(c -> c.getId()).toList())")
    DocenteResponse toResponse(Docente docente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cursosDictables", ignore = true)
    @Mapping(target = "secciones", ignore = true)
    Docente toEntity(DocenteRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "cursosDictables", ignore = true)
    @Mapping(target = "secciones", ignore = true)
    void update(@MappingTarget Docente docente, DocenteRequest request);
}
