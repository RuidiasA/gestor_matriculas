package com.example.matriculas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeccionCrearDTO {

    @NotNull
    private Long idCurso;

    @NotBlank
    private String codigoSeccion;

    @NotNull
    private Long docenteId;

    @NotBlank
    private String periodoAcademico;

    @NotBlank
    private String modalidad;

    @NotBlank
    private String turno;

    @NotNull
    @Min(1)
    private Integer capacidad;

    @NotBlank
    private String aula;

    @Valid
    private List<HorarioCrearDTO> horarios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HorarioCrearDTO {
        @NotBlank
        private String dia;
        @NotBlank
        private String horaInicio;
        @NotBlank
        private String horaFin;
    }
}
