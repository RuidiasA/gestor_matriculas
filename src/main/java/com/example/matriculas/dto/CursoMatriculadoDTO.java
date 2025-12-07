package com.example.matriculas.dto;

import com.example.matriculas.model.DetalleMatricula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursoMatriculadoDTO {
    private Long seccionId;
    private String codigoSeccion;
    private String nombreCurso;
    private String docente;
    private Integer creditos;
    private Integer horasSemanales;
    private String modalidad;
    private String aula;

    public static CursoMatriculadoDTO fromDetalle(DetalleMatricula det) {
        return CursoMatriculadoDTO.builder()
                .seccionId(det.getSeccion() != null ? det.getSeccion().getId() : null)
                .codigoSeccion(det.getSeccion().getCodigo())
                .nombreCurso(det.getSeccion().getCurso().getNombre())
                .docente(det.getSeccion().getDocente().getNombres()
                        + " " + det.getSeccion().getDocente().getApellidos())
                .creditos(det.getCreditos())
                .horasSemanales(det.getHorasSemanales())
                .modalidad(det.getModalidad().name())
                .aula(det.getAula())
                .build();
    }
}
