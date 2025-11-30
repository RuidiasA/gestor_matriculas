package com.example.matriculas.dto;

import com.example.matriculas.model.Matricula;
import lombok.Data;

@Data
public class ResumenMatriculaDTO {

    private String cicloAcademico;
    private String fecha;           // formateada
    private Integer totalCreditos;
    private Integer totalHoras;
    private Double montoTotal;
    private Integer totalCursos;

    public static ResumenMatriculaDTO fromEntity(Matricula m) {
        ResumenMatriculaDTO dto = new ResumenMatriculaDTO();
        dto.setCicloAcademico(m.getCicloAcademico());
        dto.setFecha(m.getFechaMatricula().toString()); // si quieres luego usamos DateTimeFormatter
        dto.setTotalCreditos(m.getTotalCreditos());
        dto.setTotalHoras(m.getTotalHoras());
        dto.setMontoTotal(m.getMontoTotal());
        dto.setTotalCursos(m.getDetalles() != null ? m.getDetalles().size() : 0);
        return dto;
    }
}
