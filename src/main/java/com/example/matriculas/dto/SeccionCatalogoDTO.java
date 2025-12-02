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
public class SeccionCatalogoDTO {

    private List<CursoCatalogoDTO> cursos;
    private List<String> periodos;
    private List<DocenteCatalogoDTO> docentes;
    private List<String> modalidades;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CursoCatalogoDTO {
        private Long idCurso;
        private String codigo;
        private String nombre;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DocenteCatalogoDTO {
        private Long idDocente;
        private String nombres;
        private String apellidos;
        private String nombreCompleto;
    }
}
