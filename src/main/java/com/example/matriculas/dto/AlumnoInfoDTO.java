package com.example.matriculas.dto;

import com.example.matriculas.model.Alumno;
import lombok.Data;

@Data
public class AlumnoInfoDTO {

    private Long id;
    private String codigoAlumno;
    private String nombres;
    private String apellidos;
    private String dni;
    private String correoInstitucional;
    private String carreraNombre;
    private Integer anioIngreso;
    private Integer cicloActual;
    private String estado;

    public static AlumnoInfoDTO fromEntity(Alumno a) {
        AlumnoInfoDTO dto = new AlumnoInfoDTO();
        dto.setId(a.getId());
        dto.setCodigoAlumno(a.getCodigoAlumno());
        dto.setNombres(a.getNombres());
        dto.setApellidos(a.getApellidos());
        dto.setDni(a.getDni());
        dto.setCorreoInstitucional(a.getCorreoInstitucional());
        dto.setCarreraNombre(a.getCarrera().getNombre());
        dto.setAnioIngreso(a.getAnioIngreso());
        dto.setCicloActual(a.getCicloActual());
        dto.setEstado(a.getEstado());
        return dto;
    }
}
