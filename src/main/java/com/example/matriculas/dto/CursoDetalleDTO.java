package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CursoDetalleDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer ciclo;
    private Integer creditos;
    private Integer horasSemanales;
    private String modalidad;
    private String tipo;

    private Long carreraId;
    private List<CursoPrerequisitoDTO> prerrequisitos;
    private List<DocenteCursoDictableDTO> docentesDictables;
}
