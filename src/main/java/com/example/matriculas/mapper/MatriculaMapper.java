package com.example.matriculas.mapper;

import com.example.matriculas.dto.response.MatriculaResponse;
import com.example.matriculas.model.Matricula;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {MatriculaDetalleMapper.class, PagoMapper.class})
public interface MatriculaMapper {

    @Mapping(target = "alumnoId", source = "alumno.id")
    MatriculaResponse toResponse(Matricula matricula);
}
