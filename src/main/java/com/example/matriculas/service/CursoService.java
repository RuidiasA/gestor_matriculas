package com.example.matriculas.service;

import com.example.matriculas.dto.request.CursoRequest;
import com.example.matriculas.dto.response.CursoResponse;
import com.example.matriculas.mapper.CursoMapper;
import com.example.matriculas.model.Carrera;
import com.example.matriculas.model.Curso;
import com.example.matriculas.repository.CarreraRepository;
import com.example.matriculas.repository.CursoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;
    private final CarreraRepository carreraRepository;
    private final CursoMapper cursoMapper;

    @Transactional(readOnly = true)
    public List<CursoResponse> listar() {
        return cursoRepository.findAll().stream().map(cursoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CursoResponse obtener(Long id) {
        return cursoMapper.toResponse(buscarCurso(id));
    }

    @Transactional
    public CursoResponse crear(CursoRequest request) {
        Curso curso = cursoMapper.toEntity(request);
        curso.setCarrera(buscarCarrera(request.carreraId()));
        curso.setPrerrequisitos(buscarPrerrequisitos(request.prerrequisitos()));
        return cursoMapper.toResponse(cursoRepository.save(curso));
    }

    @Transactional
    public CursoResponse actualizar(Long id, CursoRequest request) {
        Curso curso = buscarCurso(id);
        cursoMapper.update(curso, request);
        curso.setCarrera(buscarCarrera(request.carreraId()));
        curso.setPrerrequisitos(buscarPrerrequisitos(request.prerrequisitos()));
        return cursoMapper.toResponse(cursoRepository.save(curso));
    }

    @Transactional
    public void eliminar(Long id) {
        cursoRepository.deleteById(id);
    }

    private Curso buscarCurso(Long id) {
        return cursoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Curso no encontrado"));
    }

    private Carrera buscarCarrera(Long id) {
        return carreraRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Carrera no encontrada"));
    }

    private List<Curso> buscarPrerrequisitos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return cursoRepository.findAllById(ids);
    }
}
