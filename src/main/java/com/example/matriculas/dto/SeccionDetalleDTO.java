package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeccionDetalleDTO {

    private Long idSeccion;
    private String curso;
    private String codigoSeccion;
    private String docente;
    private String periodo;
    private String modalidad;
    private String horario;
    private String aula;
    private Integer cupos;
    private Integer matriculados;
    private String estado;
}
