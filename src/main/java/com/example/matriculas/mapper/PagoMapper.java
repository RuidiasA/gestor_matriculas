package com.example.matriculas.mapper;

import com.example.matriculas.dto.response.PagoResponse;
import com.example.matriculas.model.Pago;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    @Mapping(target = "matriculaId", source = "matricula.id")
    @Mapping(target = "alumnoId", source = "alumno.id")
    PagoResponse toResponse(Pago pago);
}
