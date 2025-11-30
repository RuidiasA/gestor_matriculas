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
public class AlumnoFichaDTO {
    private Long id;
    private String codigoAlumno;
    private String nombreCompleto;
    private String carrera;
    private String cicloActual;
    private Integer anioIngreso;
    private String correoInstitucional;
    private String correoPersonal;
    private String telefono;
    private String direccion;
    private String estado;
    private List<String> periodos;
}
