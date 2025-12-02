package com.example.matriculas.dto;

import com.example.matriculas.model.Matricula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialMatriculaDTO {
    private String ciclo;
    private String estado;
    private Integer totalCursos;
    private Integer totalCreditos;
    private Integer totalHoras;
    private Double matricula;
    private Double pension;
    private Double mora;
    private Double descuentos;
    private Double montoTotal;
    private List<CursoMatriculadoDTO> cursos;

    public static HistorialMatriculaDTO fromEntity(Matricula m) {

        List<CursoMatriculadoDTO> cursosDTO = m.getDetalles()
                .stream()
                .map(CursoMatriculadoDTO::fromDetalle)
                .toList();

        return HistorialMatriculaDTO.builder()
                .ciclo(m.getCicloAcademico())
                .estado(m.getEstado() != null ? m.getEstado().name() : null)
                .totalCursos(cursosDTO.size())
                .totalCreditos(m.getTotalCreditos())
                .totalHoras(m.getTotalHoras())
                .matricula(null)
                .pension(null)
                .mora(null)
                .descuentos(null)
                .montoTotal(m.getMontoTotal())
                .cursos(cursosDTO)
                .build();
    }
}

