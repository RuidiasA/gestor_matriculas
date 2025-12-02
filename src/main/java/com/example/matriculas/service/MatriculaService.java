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

                    dto.setCodigoSeccion(det.getSeccion().getCodigo());
                    dto.setNombreCurso(det.getSeccion().getCurso().getNombre());

                    dto.setDocente(det.getSeccion().getDocente().getNombres()
                            + " " + det.getSeccion().getDocente().getApellidos());

                    dto.setCreditos(det.getCreditos());
                    dto.setHorasSemanales(det.getHorasSemanales());
                    dto.setModalidad(det.getModalidad().name());
                    dto.setAula(det.getAula());

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

                    // Convertir detalles a lista de Cursos DTO
                    List<CursoMatriculadoDTO> cursosDTO = m.getDetalles()
                            .stream()
                            .map(det -> CursoMatriculadoDTO.builder()
                                    .codigoSeccion(det.getSeccion().getCodigo())
                                    .nombreCurso(det.getSeccion().getCurso().getNombre())
                                    .docente(det.getSeccion().getDocente().getNombres()
                                            + " " + det.getSeccion().getDocente().getApellidos())
                                    .creditos(det.getCreditos())
                                    .horasSemanales(det.getHorasSemanales())
                                    .modalidad(det.getModalidad().name())
                                    .aula(det.getAula())
                                    .build())
                            .collect(Collectors.toList());

                    return HistorialMatriculaDTO.builder()
                            .ciclo(m.getCicloAcademico())
                            .estado(m.getEstado() != null ? m.getEstado().name() : null)
                            .totalCursos(m.getDetalles().size())
                            .totalCreditos(m.getTotalCreditos())
                            .totalHoras(m.getTotalHoras())
                            .matricula(null)      // No existe este dato en Matricula
                            .pension(null)        // No existe este dato en Matricula
                            .mora(null)           // No existe este dato en Matricula
                            .descuentos(null)     // No existe este dato en Matricula
                            .montoTotal(m.getMontoTotal())
                            .cursos(cursosDTO)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
