package com.example.matriculas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudSeccionCreateDTO {
    @NotNull
    private Long cursoId;

    @NotBlank
    @Size(max = 50)
    private String turno;

    @NotBlank
    @Size(max = 25)
    private String diaSolicitado;

    @NotBlank
    @Size(max = 10)
    private String horaInicioSolicitada;

    @NotBlank
    @Size(max = 10)
    private String horaFinSolicitada;

    @Size(max = 50)
    private String modalidad;

    @Size(max = 50)
    private String modalidadSolicitada;

    @Size(max = 50)
    private String turnoSolicitado;

    @Email
    private String correo;

    @Size(max = 20)
    private String telefono;

    @NotBlank
    @Size(max = 1000)
    private String motivo;

    private String evidenciaNombreArchivo;
    private String evidenciaContentType;
    private String evidenciaBase64;
}
