package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteCursoDictableDTO {

    private Long idCurso;
    private String nombre;
    private String codigo;
    private Integer creditos;
    private Integer ciclo;
}
