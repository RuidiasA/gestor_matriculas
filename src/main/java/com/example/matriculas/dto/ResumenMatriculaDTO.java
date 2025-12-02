package com.example.matriculas.dto;

import com.example.matriculas.model.Matricula;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenMatriculaDTO {

    private Integer totalCursos;
    private Integer totalCreditos;
    private Integer totalHoras;
    private Double matricula;
    private Double pension;
    private Double mora;
    private Double descuentos;
    private Double montoTotal;

    public static ResumenMatriculaDTO fromEntity(Matricula m) {
        return ResumenMatriculaDTO.builder()
                .totalCursos(m.getDetalles() != null ? m.getDetalles().size() : 0)
                .totalCreditos(m.getTotalCreditos())
                .totalHoras(m.getTotalHoras())
                .matricula(null)   // puedes reemplazar si deseas implementar cálculo
                .pension(null)
                .mora(null)
                .descuentos(null)
                .montoTotal(m.getMontoTotal())
                .build();
    }
}
