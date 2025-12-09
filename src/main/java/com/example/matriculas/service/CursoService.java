package com.example.matriculas.service;

import com.example.matriculas.model.Carrera;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.Docente;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.repository.DocenteRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CursoService {

    private final CursoRepository cursoRepository;
    private final DocenteRepository docenteRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // ===============================================================
    // 1. Registrar curso (con prerrequisitos)
    // ===============================================================
    @Transactional
    public Curso registrarCurso(Curso curso) {

        // Validar duplicado
        if (cursoRepository.findByCodigo(curso.getCodigo()).isPresent()) {
            throw new RuntimeException("Ya existe un curso con este código");
        }

        Set<Docente> docentes = curso.getDocentes() != null ? new HashSet<>(curso.getDocentes()) : new HashSet<>();
        curso.setDocentes(new HashSet<>());
        asegurarColecciones(curso);

        Curso guardado = cursoRepository.save(curso);
        sincronizarDocentes(guardado, docentes);
        return guardado;
    }

    // ===============================================================
    // 2. Actualizar curso
    // ===============================================================
    @Transactional
    public Curso actualizarCurso(Curso curso) {
        Set<Docente> nuevosDocentes = curso.getDocentes() != null ? new HashSet<>(curso.getDocentes()) : new HashSet<>();

        Curso gestionado = cursoRepository.findById(curso.getId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        gestionado.setCarrera(curso.getCarrera());
        gestionado.setCodigo(curso.getCodigo());
        gestionado.setNombre(curso.getNombre());
        gestionado.setDescripcion(curso.getDescripcion());
        gestionado.setCiclo(curso.getCiclo());
        gestionado.setCreditos(curso.getCreditos());
        gestionado.setHorasSemanales(curso.getHorasSemanales());
        gestionado.setModalidad(curso.getModalidad());
        gestionado.setTipo(curso.getTipo());
        gestionado.setPrerrequisitos(curso.getPrerrequisitos());

        asegurarColecciones(gestionado);
        Curso guardado = cursoRepository.save(gestionado);
        sincronizarDocentes(guardado, nuevosDocentes);
        return guardado;
    }

    // ===============================================================
    // 3. Eliminar curso
    // ===============================================================
    @Transactional
    public void eliminarCurso(Long id) {
        cursoRepository.deleteById(id);
    }

    // ===============================================================
    // 4. Buscar por ID
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Curso> obtenerPorId(Long id) {
        return cursoRepository.findById(id);
    }

    // ===============================================================
    // 5. Buscar por código
    // ===============================================================
    @Transactional(readOnly = true)
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoRepository.findByCodigo(codigo);
    }

    // ===============================================================
    // 6. Listar todos los cursos
    // ===============================================================
    @Transactional
    public List<Curso> listarTodos() {
        entityManager.flush();
        entityManager.clear();
        return cursoRepository.findAll();
    }

    // ===============================================================
    // 7. Listar cursos de una carrera
    // ===============================================================
    @Transactional(readOnly = true)
    public List<Curso> listarPorCarrera(Carrera carrera) {
        return cursoRepository.findByCarrera(carrera);
    }

    // ===============================================================
    // 7.1. Listar cursos de una carrera por ID (sin caché obsoleta)
    // ===============================================================
    @Transactional(readOnly = true)
    public List<Curso> listarPorCarreraId(Long carreraId) {
        entityManager.flush();
        entityManager.clear();
        return cursoRepository.findCursosPorCarrera(carreraId);
    }

    // ===============================================================
    // 8. Listar cursos por ciclo
    // ===============================================================
    @Transactional(readOnly = true)
    public List<Curso> listarPorCiclo(int ciclo) {
        return cursoRepository.findByCiclo(ciclo);
    }

    // ===============================================================
    // 9. Buscar por nombre (para filtros)
    // ===============================================================
    @Transactional(readOnly = true)
    public List<Curso> buscarPorNombre(String nombre) {
        return cursoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===============================================================
    // 10. Actualizar prerrequisitos
    // ===============================================================
    @Transactional
    public Curso actualizarPrerrequisitos(Curso curso, Set<Curso> prerrequisitos) {
        Curso gestionado = cursoRepository.findById(curso.getId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        gestionado.setPrerrequisitos(prerrequisitos);
        asegurarColecciones(gestionado);
        return cursoRepository.save(gestionado);
    }

    @Transactional
    public Curso actualizarDocentes(Curso curso, Set<Docente> docentes) {
        Curso gestionado = cursoRepository.findById(curso.getId())
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        asegurarColecciones(gestionado);
        sincronizarDocentes(gestionado, docentes);
        return cursoRepository.save(gestionado);
    }

    private void sincronizarDocentes(Curso curso, Set<Docente> nuevosDocentes) {
        Set<Docente> actuales = curso.getDocentes() != null ? new HashSet<>(curso.getDocentes()) : new HashSet<>();
        Set<Long> nuevosIds = nuevosDocentes.stream()
                .map(Docente::getId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        // Remover docentes que ya no dictan el curso
        for (Docente docente : actuales) {
            if (!nuevosIds.contains(docente.getId())) {
                Set<Curso> cursosDocente = docente.getCursosDictados();
                if (cursosDocente != null) {
                    cursosDocente.remove(curso);
                }
                docenteRepository.save(docente);
            }
        }

        // Agregar nuevos docentes
        for (Docente docente : nuevosDocentes) {
            Set<Curso> cursosDocente = docente.getCursosDictados();
            if (cursosDocente == null) {
                cursosDocente = new HashSet<>();
                docente.setCursosDictados(cursosDocente);
            }
            cursosDocente.add(curso);
            docenteRepository.save(docente);
        }

        curso.setDocentes(nuevosDocentes);
    }

    private void asegurarColecciones(Curso curso) {
        if (curso.getPrerrequisitos() == null) {
            curso.setPrerrequisitos(new HashSet<>());
        }
        if (curso.getDocentes() == null) {
            curso.setDocentes(new HashSet<>());
        }
    }
}
