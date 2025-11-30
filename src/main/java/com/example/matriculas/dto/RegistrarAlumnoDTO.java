package com.example.matriculas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrarAlumnoDTO {

    @NotBlank(message = "El código institucional es obligatorio")
    @Pattern(regexp = "^S\\d+$", message = "El código institucional debe iniciar con 'S' seguido de dígitos")
    @Size(max = 20, message = "El código institucional no puede exceder 20 caracteres")
    private String codigoAlumno;

    @NotBlank(message = "Los nombres son obligatorios")
    @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener exactamente 8 dígitos")
    private String dni;

    @NotBlank(message = "El correo institucional es obligatorio")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@universidad\\.com\\.pe$", message = "El correo institucional debe terminar en @universidad.com.pe")
    private String correoInstitucional;

    @Email(message = "Correo personal inválido")
    private String correoPersonal;

    @Pattern(regexp = "^$|^\\d{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefonoPersonal;

    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String direccion;

    @NotNull(message = "El año de ingreso es obligatorio")
    @Min(value = 2000, message = "El año de ingreso no puede ser anterior al 2000")
    private Integer anioIngreso;

    @NotNull(message = "El ciclo actual es obligatorio")
    @Min(value = 1, message = "El ciclo actual debe ser mayor o igual a 1")
    private Integer cicloActual;

    @NotNull(message = "La carrera es obligatoria")
    private Long carreraId;

    @Size(max = 100, message = "El usuario de registro no puede exceder 100 caracteres")
    private String usuarioRegistro;
}
