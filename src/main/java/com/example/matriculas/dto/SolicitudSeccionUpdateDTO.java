package com.example.matriculas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SolicitudSeccionUpdateDTO {
    @NotNull
    private Long id;

    @NotBlank
    private String estado;

    @Size(max = 300)
    private String mensajeAdmin;
}
