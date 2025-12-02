package com.example.matriculas.mapper;

import com.example.matriculas.dto.request.SeccionHorarioRequest;
import com.example.matriculas.dto.response.SeccionHorarioResponse;
import com.example.matriculas.model.SeccionHorario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeccionHorarioMapper {

    SeccionHorarioResponse toResponse(SeccionHorario horario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "seccion", ignore = true)
    SeccionHorario toEntity(SeccionHorarioRequest request);
}
