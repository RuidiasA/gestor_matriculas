package com.example.matriculas.service;

import com.example.matriculas.dto.request.SeccionRequest;
import com.example.matriculas.dto.response.SeccionResponse;
import com.example.matriculas.mapper.SeccionHorarioMapper;
import com.example.matriculas.mapper.SeccionMapper;
import com.example.matriculas.model.Docente;
import com.example.matriculas.model.Seccion;
import com.example.matriculas.model.SeccionHorario;
import com.example.matriculas.model.Curso;
import com.example.matriculas.repository.DocenteRepository;
import com.example.matriculas.repository.SeccionHorarioRepository;
import com.example.matriculas.repository.SeccionRepository;
import com.example.matriculas.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeccionService {

    private final SeccionRepository seccionRepository;
    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;
    private final SeccionHorarioRepository seccionHorarioRepository;
    private final SeccionMapper seccionMapper;
    private final SeccionHorarioMapper seccionHorarioMapper;

    @Transactional(readOnly = true)
    public List<SeccionResponse> listar() {
        return seccionRepository.findAll().stream().map(seccionMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SeccionResponse obtener(Long id) {
        return seccionMapper.toResponse(buscarSeccion(id));
    }

    @Transactional
    public SeccionResponse crear(SeccionRequest request) {
        Seccion seccion = seccionMapper.toEntity(request);
        seccion.setCurso(buscarCurso(request.cursoId()));
        seccion.setDocente(buscarDocente(request.docenteId()));
        seccion.setFechaCreacion(LocalDateTime.now());
        Seccion guardada = seccionRepository.save(seccion);
        guardarHorarios(guardada, request.horarios());
        return seccionMapper.toResponse(guardada);
    }

    @Transactional
    public SeccionResponse actualizar(Long id, SeccionRequest request) {
        Seccion seccion = buscarSeccion(id);
        seccionMapper.update(seccion, request);
        seccion.setCurso(buscarCurso(request.cursoId()));
        seccion.setDocente(buscarDocente(request.docenteId()));
        seccionHorarioRepository.deleteAll(seccion.getHorarios());
        guardarHorarios(seccion, request.horarios());
        return seccionMapper.toResponse(seccionRepository.save(seccion));
    }

    @Transactional
    public void eliminar(Long id) {
        seccionRepository.deleteById(id);
    }

    private Seccion buscarSeccion(Long id) {
        return seccionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sección no encontrada"));
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
    }

    private Docente buscarDocente(Long id) {
        return docenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Docente no encontrado"));
    }

    private void guardarHorarios(Seccion seccion, List<com.example.matriculas.dto.request.SeccionHorarioRequest> horariosRequest) {
        if (horariosRequest == null) return;
        List<SeccionHorario> horarios = horariosRequest.stream()
                .map(seccionHorarioMapper::toEntity)
                .peek(h -> h.setSeccion(seccion))
                .toList();
        seccionHorarioRepository.saveAll(horarios);
        seccion.setHorarios(horarios);
    }
}
