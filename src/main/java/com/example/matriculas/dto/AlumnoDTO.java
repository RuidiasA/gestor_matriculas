package com.example.matriculas.dto;

import com.example.matriculas.model.Alumno;
import lombok.Data;

@Data
public class AlumnoDTO {

    private Long id;
    private String codigoAlumno;

    private String apellidos;
    private String nombres;
    private String dni;

    private String correoInstitucional;
    private String correoPersonal;
    private String telefonoPersonal;

    private String direccion;
    private String estado;

    private Integer anioIngreso;
    private Integer cicloActual;

    private String carreraNombre;

    public static AlumnoDTO fromEntity(Alumno a) {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setId(a.getId());
        dto.setCodigoAlumno(a.getCodigoAlumno());
        dto.setApellidos(a.getApellidos());
        dto.setNombres(a.getNombres());
        dto.setDni(a.getDni());

        dto.setCorreoInstitucional(a.getCorreoInstitucional());
        dto.setCorreoPersonal(a.getCorreoPersonal());
        dto.setTelefonoPersonal(a.getTelefonoPersonal());

        dto.setDireccion(a.getDireccion());
        dto.setEstado(a.getEstado());

        dto.setAnioIngreso(a.getAnioIngreso());
        dto.setCicloActual(a.getCicloActual());

        dto.setCarreraNombre(a.getCarrera().getNombre());
        return dto;
    }
}
