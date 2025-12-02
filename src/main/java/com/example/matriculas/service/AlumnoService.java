package com.example.matriculas.service;

import com.example.matriculas.dto.request.AlumnoRequest;
import com.example.matriculas.dto.response.AlumnoResponse;
import com.example.matriculas.dto.response.MatriculaResponse;
import com.example.matriculas.mapper.AlumnoMapper;
import com.example.matriculas.mapper.MatriculaMapper;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Carrera;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.CarreraRepository;
import com.example.matriculas.repository.MatriculaRepository;
import com.example.matriculas.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final CarreraRepository carreraRepository;
    private final UsuarioRepository usuarioRepository;
    private final MatriculaRepository matriculaRepository;
    private final AlumnoMapper alumnoMapper;
    private final MatriculaMapper matriculaMapper;

    @Transactional(readOnly = true)
    public List<AlumnoResponse> listar() {
        return alumnoRepository.findAll().stream().map(alumnoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AlumnoResponse obtener(Long id) {
        return alumnoMapper.toResponse(buscarAlumno(id));
    }

    @Transactional
    public AlumnoResponse crear(AlumnoRequest request) {
        Alumno alumno = alumnoMapper.toEntity(request);
        alumno.setCarrera(buscarCarrera(request.carreraId()));
        alumno.setUsuario(buscarUsuario(request.usuarioId()));
        return alumnoMapper.toResponse(alumnoRepository.save(alumno));
    }

    @Transactional
    public AlumnoResponse actualizar(Long id, AlumnoRequest request) {
        Alumno alumno = buscarAlumno(id);
        alumnoMapper.update(alumno, request);
        alumno.setCarrera(buscarCarrera(request.carreraId()));
        alumno.setUsuario(buscarUsuario(request.usuarioId()));
        return alumnoMapper.toResponse(alumnoRepository.save(alumno));
    }

    @Transactional
    public void eliminar(Long id) {
        alumnoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> obtenerHistorialMatriculas(Long alumnoId) {
        Alumno alumno = buscarAlumno(alumnoId);
        return matriculaRepository.findByAlumnoId(alumno.getId()).stream()
                .map(matriculaMapper::toResponse)
                .toList();
    }

    private Alumno buscarAlumno(Long id) {
        return alumnoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado"));
    }

    private Carrera buscarCarrera(Long id) {
        return carreraRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Carrera no encontrada"));
    }

    private Usuario buscarUsuario(Long id) {
        if (id == null) {
            return null;
        }
        return usuarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}
