package com.example.matriculas.service;

import com.example.matriculas.dto.request.SolicitudSeccionRequest;
import com.example.matriculas.dto.response.SolicitudSeccionResponse;
import com.example.matriculas.mapper.SolicitudMapper;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.SolicitudSeccion;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.SolicitudSeccionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudSeccionRepository solicitudRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final SolicitudMapper solicitudMapper;

    @Transactional(readOnly = true)
    public List<SolicitudSeccionResponse> listar() {
        return solicitudRepository.findAll().stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudSeccionResponse> listarPorAlumno(Long alumnoId) {
        return solicitudRepository.findByAlumnoId(alumnoId).stream().map(solicitudMapper::toResponse).toList();
    }

    @Transactional
    public SolicitudSeccionResponse crear(SolicitudSeccionRequest request) {
        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado"));
        Curso curso = cursoRepository.findById(request.cursoId())
                .orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
        SolicitudSeccion solicitud = SolicitudSeccion.builder()
                .alumno(alumno)
                .curso(curso)
                .fechaSolicitud(LocalDateTime.now())
                .modalidad(request.modalidad())
                .turno(request.turno())
                .telefono(request.telefono())
                .correo(request.correo())
                .motivo(request.motivo())
                .evidenciaNombreArchivo(request.evidenciaNombreArchivo())
                .build();
        return solicitudMapper.toResponse(solicitudRepository.save(solicitud));
    }
}
