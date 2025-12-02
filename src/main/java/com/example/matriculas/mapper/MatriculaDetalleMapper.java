package com.example.matriculas.mapper;

import com.example.matriculas.dto.response.MatriculaDetalleResponse;
import com.example.matriculas.model.DetalleMatricula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MatriculaDetalleMapper {

    @Mapping(target = "seccionId", source = "seccion.id")
    @Mapping(target = "docenteId", source = "docente.id")
    MatriculaDetalleResponse toResponse(DetalleMatricula detalle);
}
