package com.example.matriculas.dto;

import com.example.matriculas.model.DetalleMatricula;
import lombok.Data;

@Data
public class CursoMatriculadoDTO {

    private String seccion;
    private String curso;
    private String docente;
    private String aula;
    private int cicloCurso;
    private int creditos;
    private int horas;
    private String tipo;
    private String modalidad;

    public static CursoMatriculadoDTO fromDetalle(DetalleMatricula d) {
        CursoMatriculadoDTO dto = new CursoMatriculadoDTO();

        dto.setSeccion(d.getSeccion().getCodigo());
        dto.setCurso(d.getSeccion().getCurso().getNombre());
        dto.setDocente(d.getSeccion().getDocente().getNombres()
                + " " + d.getSeccion().getDocente().getApellidos());
        dto.setAula(d.getAula());
        dto.setCicloCurso(d.getSeccion().getCurso().getCiclo());
        dto.setCreditos(d.getCreditos());
        dto.setHoras(d.getHorasSemanales());
        dto.setTipo(d.getSeccion().getCurso().getTipo().name());
        dto.setModalidad(d.getModalidad().name());

        return dto;
    }
}
