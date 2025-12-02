package com.example.matriculas.service;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.DetalleMatricula;
import com.example.matriculas.model.Matricula;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.DetalleMatriculaRepository;
import com.example.matriculas.repository.MatriculaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMatriculaService {

    private final AlumnoRepository alumnoRepository;
    private final MatriculaRepository matriculaRepository;
    private final DetalleMatriculaRepository detalleMatriculaRepository;

    // ==============================
    // 1. Info de alumno (tarjeta)
    // ==============================
    public AlumnoInfoDTO obtenerInfoAlumno(Long alumnoId) {
        Alumno alumno = alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));

        return AlumnoInfoDTO.fromEntity(alumno);
    }

    // ==============================
    // 2. Matricula actual por ciclo
    // ==============================
    public ResumenMatriculaDTO obtenerResumenMatricula(Long alumnoId, String ciclo) {

        Matricula matricula = matriculaRepository
                .findByAlumnoIdAndCicloAcademico(alumnoId, ciclo)
                .orElseThrow(() -> new RuntimeException("El alumno no tiene matrícula en el ciclo " + ciclo));

        return ResumenMatriculaDTO.fromEntity(matricula);
    }

    public List<CursoMatriculadoDTO> obtenerCursosMatriculados(Long alumnoId, String ciclo) {

        Matricula matricula = matriculaRepository
                .findByAlumnoIdAndCicloAcademico(alumnoId, ciclo)
                .orElseThrow(() -> new RuntimeException("El alumno no tiene matrícula en el ciclo " + ciclo));

        List<DetalleMatricula> detalles =
                detalleMatriculaRepository.findByMatriculaId(matricula.getId());

        return detalles.stream()
                .map(CursoMatriculadoDTO::fromDetalle)
                .toList();
    }

    // ==============================
    // 3. Historial de matrículas
    // ==============================
    public List<HistorialMatriculaDTO> obtenerHistorial(Long alumnoId) {

        return matriculaRepository.findByAlumnoIdOrderByFechaMatriculaDesc(alumnoId)
                .stream()
                .map(HistorialMatriculaDTO::fromEntity)
                .toList();
    }
}
