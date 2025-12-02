package com.example.matriculas.service;

import com.example.matriculas.dto.request.DocenteRequest;
import com.example.matriculas.dto.response.DocenteResponse;
import com.example.matriculas.mapper.DocenteMapper;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.Docente;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.DocenteRepository;
import com.example.matriculas.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final DocenteMapper docenteMapper;

    @Transactional(readOnly = true)
    public List<DocenteResponse> listar() {
        return docenteRepository.findAll().stream().map(docenteMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DocenteResponse obtener(Long id) {
        return docenteMapper.toResponse(buscarDocente(id));
    }

    @Transactional
    public DocenteResponse crear(DocenteRequest request) {
        Docente docente = docenteMapper.toEntity(request);
        docente.setUsuario(buscarUsuario(request.usuarioId()));
        docente.setCursosDictables(buscarCursos(request.cursosDictables()));
        return docenteMapper.toResponse(docenteRepository.save(docente));
    }

    @Transactional
    public DocenteResponse actualizar(Long id, DocenteRequest request) {
        Docente docente = buscarDocente(id);
        docenteMapper.update(docente, request);
        docente.setUsuario(buscarUsuario(request.usuarioId()));
        docente.setCursosDictables(buscarCursos(request.cursosDictables()));
        return docenteMapper.toResponse(docenteRepository.save(docente));
    }

    @Transactional
    public void eliminar(Long id) {
        docenteRepository.deleteById(id);
    }

    private Docente buscarDocente(Long id) {
        return docenteRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Docente no encontrado"));
    }

    private Usuario buscarUsuario(Long id) {
        if (id == null) return null;
        return usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }

    private List<Curso> buscarCursos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return cursoRepository.findAllById(ids);
    }
}
