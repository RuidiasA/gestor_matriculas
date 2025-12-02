package com.example.matriculas.service;

import com.example.matriculas.model.Carrera;
import com.example.matriculas.model.Curso;
import com.example.matriculas.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;

    // ===============================================================
    // 1. Registrar curso (con prerrequisitos)
    // ===============================================================
    public Curso registrarCurso(Curso curso) {

        // Validar duplicado
        if (cursoRepository.findByCodigo(curso.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un curso con este código");
        }

        return cursoRepository.save(curso);
    }

    // ===============================================================
    // 2. Actualizar curso
    // ===============================================================
    public Curso actualizarCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    // ===============================================================
    // 3. Eliminar curso
    // ===============================================================
    public void eliminarCurso(Long id) {
        cursoRepository.deleteById(id);
    }

    // ===============================================================
    // 4. Buscar por ID
    // ===============================================================
    public Optional<Curso> obtenerPorId(Long id) {
        return cursoRepository.findById(id);
    }

    // ===============================================================
    // 5. Buscar por código
    // ===============================================================
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoRepository.findByCodigo(codigo);
    }

    // ===============================================================
    // 6. Listar todos los cursos
    // ===============================================================
    public List<Curso> listarTodos() {
        return cursoRepository.findAll();
    }

    // ===============================================================
    // 7. Listar cursos de una carrera
    // ===============================================================
    public List<Curso> listarPorCarrera(Carrera carrera) {
        return cursoRepository.findByCarrera(carrera);
    }

    // ===============================================================
    // 8. Listar cursos por ciclo
    // ===============================================================
    public List<Curso> listarPorCiclo(int ciclo) {
        return cursoRepository.findByCiclo(ciclo);
    }

    // ===============================================================
    // 9. Buscar por nombre (para filtros)
    // ===============================================================
    public List<Curso> buscarPorNombre(String nombre) {
        return cursoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===============================================================
    // 10. Actualizar prerrequisitos
    // ===============================================================
    public Curso actualizarPrerrequisitos(Long idCurso, List<Curso> prerrequisitos) {
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        curso.setPrerrequisitos(prerrequisitos);

        return cursoRepository.save(curso);
    }
}
