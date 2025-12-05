package com.example.matriculas.controller;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.*;
import com.example.matriculas.model.enums.Modalidad;
import com.example.matriculas.model.enums.TipoCurso;
import com.example.matriculas.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/cursos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCursoController {

    private final CursoService cursoService;
    private final CarreraService carreraService;
    private final DocenteService docenteService;

    // ============================================================
    // LISTADO GENERAL
    // ============================================================
    @GetMapping
    public List<CursoListadoDTO> listarCursos() {
        return cursoService.listarTodos()
                .stream()
                .map(c -> CursoListadoDTO.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .carrera(c.getCarrera().getNombre())
                        .ciclo(c.getCiclo())
                        .creditos(c.getCreditos())
                        .tipo(c.getTipo() != null ? c.getTipo().name() : null)
                        .horasSemanales(c.getHorasSemanales())
                        .modalidad(c.getModalidad() != null ? c.getModalidad().name() : null)
                        .build())
                .toList();
    }

    // ============================================================
    // CATÁLOGOS
    // ============================================================
    @GetMapping("/catalogos")
    public CursoCatalogoDTO cargarCatalogos() {

        var carreras = carreraService.listarTodas()
                .stream()
                .map(c -> new CarreraDTO(c.getId(), c.getNombre()))
                .toList();

        var docentes = docenteService.buscar("", null, (String) null);

        var cursos = cursoService.listarTodos()
                .stream()
                .map(c -> CursoListadoDTO.builder()
                        .id(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .carrera(c.getCarrera().getNombre())
                        .ciclo(c.getCiclo())
                        .creditos(c.getCreditos())
                        .build())
                .toList();

        return CursoCatalogoDTO.builder()
                .carreras(carreras)
                .docentes(docentes)
                .cursos(cursos)
                .build();
    }

    // ============================================================
    // DETALLE DE CURSO
    // ============================================================
    @GetMapping("/{id}")
    public CursoDetalleDTO obtenerDetalle(@PathVariable Long id) {

        Curso curso = cursoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        return CursoDetalleDTO.builder()
                .id(curso.getId())
                .codigo(curso.getCodigo())
                .nombre(curso.getNombre())
                .descripcion(curso.getDescripcion())
                .ciclo(curso.getCiclo())
                .creditos(curso.getCreditos())
                .horasSemanales(curso.getHorasSemanales())
                .modalidad(curso.getModalidad() != null ? curso.getModalidad().name() : null)
                .tipo(curso.getTipo() != null ? curso.getTipo().name() : null)
                .carreraId(curso.getCarrera().getId())

                // ---- PRERREQUISITOS ----
                .prerrequisitos(
                        curso.getPrerrequisitos() != null
                                ? curso.getPrerrequisitos().stream()
                                .map(p -> CursoPrerequisitoDTO.builder()
                                        .idCurso(p.getId())
                                        .codigo(p.getCodigo())
                                        .nombre(p.getNombre())
                                        .build())
                                .toList()
                                : List.of()
                )

                // ---- DOCENTES DICTABLES ----
                .docentesDictables(
                        curso.getDocentes() != null
                                ? curso.getDocentes().stream()
                                .map(d -> DocenteCursoDictableDTO.builder()
                                        .idDocente(d.getId())                        // ID REAL
                                        .codigoDocente(d.getCodigoDocente())         // visible al usuario
                                        .nombreCompleto(d.getNombres() + " " + d.getApellidos())
                                        .dni(d.getDni())
                                        .build())
                                .toList()
                                : List.of()
                )

                .build();
    }

    // ============================================================
    // REGISTRAR
    // ============================================================
    @PostMapping
    public ResponseEntity<Void> registrarCurso(@RequestBody CursoDetalleDTO dto) {
        Curso curso = buildCursoFromRequest(dto, new Curso());
        cursoService.registrarCurso(curso);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarCurso(
            @PathVariable Long id,
            @RequestBody CursoDetalleDTO dto
    ) {

        Curso curso = cursoService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        curso = buildCursoFromRequest(dto, curso);
        cursoService.actualizarCurso(curso);

        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ELIMINAR
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCurso(@PathVariable Long id) {
        cursoService.eliminarCurso(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // MÉTODO UTIL PARA ARMAR ENTIDAD DESDE UN DTO
    // ============================================================
    private Curso buildCursoFromRequest(CursoDetalleDTO dto, Curso curso) {

        Carrera carrera = carreraService.obtenerPorId(dto.getCarreraId())
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));

        curso.setCodigo(dto.getCodigo());
        curso.setNombre(dto.getNombre());
        curso.setDescripcion(dto.getDescripcion());
        curso.setCiclo(dto.getCiclo());
        curso.setCreditos(dto.getCreditos());
        curso.setHorasSemanales(dto.getHorasSemanales());
        curso.setModalidad(dto.getModalidad() != null ? Modalidad.valueOf(dto.getModalidad()) : null);
        curso.setTipo(dto.getTipo() != null ? TipoCurso.valueOf(dto.getTipo()) : null);
        curso.setCarrera(carrera);

        // -------------------------------
        // PRERREQUISITOS
        // -------------------------------
        if (dto.getPrerrequisitos() != null) {
            List<Curso> prerrequisitos = dto.getPrerrequisitos()
                    .stream()
                    .map(p -> cursoService.obtenerPorId(p.getIdCurso())
                            .orElseThrow(() -> new RuntimeException("Prerrequisito inválido")))
                    .toList();

            curso.setPrerrequisitos(prerrequisitos);
        }

        // -------------------------------
        // DOCENTES DICTABLES
        // -------------------------------
        if (dto.getDocentesDictables() != null) {

            List<Docente> docentes = dto.getDocentesDictables()
                    .stream()
                    .map(d -> docenteService.obtenerPorId(d.getIdDocente())
                            .orElseThrow(() -> new RuntimeException("Docente no válido")))
                    .toList();

            curso.setDocentes(new HashSet<>(docentes));
        }

        return curso;
    }

}
