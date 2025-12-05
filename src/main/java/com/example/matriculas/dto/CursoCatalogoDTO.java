package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CursoCatalogoDTO {

    private List<CarreraDTO> carreras;
    private List<DocenteBusquedaDTO> docentes;
    private List<CursoListadoDTO> cursos;
}
