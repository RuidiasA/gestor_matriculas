package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteBusquedaDTO {

    private Long id;
    private String codigo;
    private String nombreCompleto;
    private String dni;
    private String estado;
}
