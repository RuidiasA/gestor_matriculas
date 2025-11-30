package com.example.matriculas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarAlumnoDTO {

    @Size(min = 1, max = 100, message = "Los nombres no pueden estar vacíos ni exceder 100 caracteres")
    @Pattern(regexp = "^$|.*\\S.*", message = "Los nombres no pueden estar vacíos o solo contener espacios")
    private String nombres;

    @Size(min = 1, max = 100, message = "Los apellidos no pueden estar vacíos ni exceder 100 caracteres")
    @Pattern(regexp = "^$|.*\\S.*", message = "Los apellidos no pueden estar vacíos o solo contener espacios")
    private String apellidos;

    @Email(message = "Correo personal inválido")
    private String correoPersonal;

    @Pattern(regexp = "^$|^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefonoPersonal;

    @Size(max = 100, message = "El usuario de modificación no puede exceder 100 caracteres")
    private String usuarioModificacion;
}
