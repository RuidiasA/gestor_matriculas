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
public class SeccionDetalleDTO {

    private Long idSeccion;
    private String curso;
    private String codigoSeccion;
    private Long docenteId;
    private String docente;
    private String periodo;
    private String modalidad;
    private String horario;
    private String aula;
    private Integer cupos;
    private Integer matriculados;
    private String estado;
    private List<HorarioDTO> horarios;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HorarioDTO {
        private String dia;
        private String horaInicio;
        private String horaFin;
    }
}
