package com.example.matriculas.service;

import com.example.matriculas.dto.CursoMatriculadoDTO;
import com.example.matriculas.dto.HistorialMatriculaDTO;
import com.example.matriculas.model.DetalleMatricula;
import com.example.matriculas.model.Matricula;
import com.example.matriculas.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;

    // LISTA DE CICLOS DEL ALUMNO
    public List<String> obtenerCiclosAlumno(Long alumnoId) {
        return matriculaRepository.findByAlumnoId(alumnoId)
                .stream()
                .map(Matricula::getCicloAcademico)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    // CURSOS MATRICULADOS EN UN CICLO
    public List<CursoMatriculadoDTO> obtenerCursosPorCiclo(Long alumnoId, String ciclo) {

        Matricula matricula = matriculaRepository
                .findByAlumnoIdAndCicloAcademico(alumnoId, ciclo)
                .stream()
                .findFirst()
                .orElse(null);

        if (matricula == null) return List.of();

        return matricula.getDetalles()
                .stream()
                .map(det -> {
                    CursoMatriculadoDTO dto = new CursoMatriculadoDTO();
                    dto.setSeccion(det.getSeccion().getCodigo());
                    dto.setCurso(det.getSeccion().getCurso().getNombre());
                    dto.setDocente(det.getSeccion().getDocente().getNombres()
                            + " " + det.getSeccion().getDocente().getApellidos());
                    dto.setAula(det.getAula());
                    dto.setCicloCurso(det.getSeccion().getCurso().getCiclo());
                    dto.setCreditos(det.getCreditos());
                    dto.setHoras(det.getHorasSemanales());
                    dto.setTipo(det.getSeccion().getCurso().getTipo().name());
                    dto.setModalidad(det.getModalidad().name());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // HISTORIAL COMPLETO
    public List<HistorialMatriculaDTO> obtenerHistorial(Long alumnoId) {

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return matriculaRepository.findByAlumnoId(alumnoId)
                .stream()
                .sorted(Comparator.comparing(Matricula::getFechaMatricula).reversed())
                .map(m -> {
                    HistorialMatriculaDTO dto = new HistorialMatriculaDTO();

                    dto.setCiclo(m.getCicloAcademico());
                    dto.setCursos(m.getDetalles().size());
                    dto.setCreditos(m.getTotalCreditos());
                    dto.setHoras(m.getTotalHoras());
                    dto.setMonto(m.getMontoTotal());
                    dto.setFecha(m.getFechaMatricula().format(fmt));

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
