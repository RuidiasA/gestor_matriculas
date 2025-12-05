package com.example.matriculas.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteCursoDictableDTO {

    private Long idDocente;
    private Long idCurso;

    private String codigoDocente;
    private String nombreCompleto;
    private String dni;

    private Integer creditosCurso;
    private Integer cicloCurso;
}

