package com.example.matriculas.dto;

import com.example.matriculas.model.Matricula;
import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class HistorialMatriculaDTO {

    private String ciclo;
    private int cursos;
    private int creditos;
    private int horas;
    private double monto;
    private String fecha;

    public static HistorialMatriculaDTO fromEntity(Matricula m) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        HistorialMatriculaDTO dto = new HistorialMatriculaDTO();

        dto.setCiclo(m.getCicloAcademico());
        dto.setCursos(m.getDetalles().size());
        dto.setCreditos(m.getTotalCreditos());
        dto.setHoras(m.getTotalHoras());
        dto.setMonto(m.getMontoTotal());
        dto.setFecha(m.getFechaMatricula().format(fmt));

        return dto;
    }
}
