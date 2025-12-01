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
public class SeccionActualizarDTO {

    @NotNull
    private Long docenteId;

    @NotBlank
    private String aula;

    @NotNull
    @Min(1)
    private Integer cupos;

    @NotBlank
    private String modalidad;

    @Valid
    private List<HorarioEdicionDTO> horarios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HorarioEdicionDTO {
        @NotBlank
        private String dia;

        @NotBlank
        private String horaInicio;

        @NotBlank
        private String horaFin;
    }
}
