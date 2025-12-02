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
public class DocenteDetalleDTO {

    private Long id;
    private String codigo;
    private String apellidos;
    private String nombres;
    private String dni;
    private String estado;
    private String correoInstitucional;
    private String correoPersonal;
    private String telefono;
    private String direccion;
    private String especialidad;
    private Integer anioIngreso;

    private List<DocenteCursoDictableDTO> cursosDictables;
    private List<DocenteSeccionActualDTO> seccionesActuales;
    private Integer totalSeccionesActuales;
    private Integer totalCreditosActuales;
    private Integer totalHorasSemanalesActuales;
    private Integer totalCursosActuales;

    private List<DocenteHistorialSeccionDTO> historial;
}
