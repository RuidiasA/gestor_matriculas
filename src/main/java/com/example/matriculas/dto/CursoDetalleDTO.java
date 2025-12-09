package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private String carreraNombre;
    private List<CursoPrerequisitoDTO> prerrequisitos;
    private List<DocenteCursoDictableDTO> docentesDictables;
}
