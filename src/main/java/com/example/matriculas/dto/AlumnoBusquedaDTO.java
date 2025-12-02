package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlumnoBusquedaDTO {

    private Long id;
    private String codigo;
    private String nombreCompleto;
    private String dni;
    private String correoInstitucional;
    private String correoPersonal;
    private String telefono;
    private String anioIngreso;
    private String cicloActual;
    private String turno;
    private Integer ordenMerito;
    private String carrera;
    private String direccion;
    private String estado;
    private List<String> periodos;
}
