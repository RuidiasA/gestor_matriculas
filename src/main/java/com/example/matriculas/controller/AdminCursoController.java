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
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.IntStream;

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
    public List<CursoListadoDTO> listarCursos(
            @RequestParam(required = false) String filtro,
            @RequestParam(required = false) Long carreraId,
            @RequestParam(required = false) Integer ciclo,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String modalidad
    ) {
        String filtroLimpio = filtro != null ? filtro.trim().toLowerCase() : "";
        TipoCurso tipoCurso = parseTipo(tipo);
        Modalidad modalidadCurso = parseModalidad(modalidad);

        return cursoService.listarTodos()
                .stream()
                .filter(c -> filtroLimpio.isEmpty()
                        || c.getCodigo().toLowerCase().contains(filtroLimpio)
                        || c.getNombre().toLowerCase().contains(filtroLimpio))
                .filter(c -> carreraId == null || (c.getCarrera() != null && Objects.equals(c.getCarrera().getId(), carreraId)))
                .filter(c -> ciclo == null || Objects.equals(c.getCiclo(), ciclo))
                .filter(c -> tipoCurso == null || c.getTipo() == tipoCurso)
                .filter(c -> modalidadCurso == null || c.getModalidad() == modalidadCurso)
                .map(c -> CursoListadoDTO.builder()
                        .id(c.getId())
                        .idCurso(c.getId())
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

        var docentes = docenteService.buscar("", null, (String) null)
                .stream()
                .map(DocenteBusquedaDTO::fromEntity)
                .toList();

        var cursos = cursoService.listarTodos()
                .stream()
                .map(c -> CursoListadoDTO.builder()
                        .id(c.getId())
                        .idCurso(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .carrera(c.getCarrera().getNombre())
                        .ciclo(c.getCiclo())
                        .creditos(c.getCreditos())
                        .build())
                .toList();

        List<Integer> ciclos = cursos.stream()
                .map(CursoListadoDTO::getCiclo)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        if (ciclos.isEmpty()) {
            ciclos = IntStream.rangeClosed(1, 10).boxed().toList();
        }

        var tipos = Arrays.stream(TipoCurso.values()).map(Enum::name).toList();
        var modalidades = Arrays.stream(Modalidad.values()).map(Enum::name).toList();

        return CursoCatalogoDTO.builder()
                .carreras(carreras)
                .docentes(docentes)
                .cursos(cursos)
                .ciclos(ciclos)
                .tipos(tipos)
                .modalidades(modalidades)
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
                .carreraNombre(curso.getCarrera().getNombre())

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
                                        .creditosCurso(curso.getCreditos())
                                        .cicloCurso(curso.getCiclo())
                                        .build())
                                .toList()
                                : List.of()
                )

                .build();
    }

    // ============================================================
    // ACTUALIZAR PRERREQUISITOS
    // ============================================================
    @PutMapping("/{id}/prerrequisitos")
    public ResponseEntity<Void> actualizarPrerrequisitos(
            @PathVariable Long id,
            @RequestBody CursoPrerrequisitoUpdateDTO dto
    ) {
        Curso curso = cursoService.obtenerPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado"));

        Set<Curso> prerrequisitos = dto.getIdsPrerrequisitos() == null
                ? Set.of()
                : dto.getIdsPrerrequisitos().stream()
                .map(pid -> cursoService.obtenerPorId(pid)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prerrequisito inválido")))
                .collect(java.util.stream.Collectors.toSet());

        cursoService.actualizarPrerrequisitos(curso, prerrequisitos);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ACTUALIZAR DOCENTES DICTABLES
    // ============================================================
    @PutMapping("/{id}/docentes")
    public ResponseEntity<Void> actualizarDocentesDictables(
            @PathVariable Long id,
            @RequestBody CursoDocenteUpdateDTO dto
    ) {
        Curso curso = cursoService.obtenerPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso no encontrado"));

        Set<Docente> docentes = dto.getIdsDocentes() == null
                ? Set.of()
                : dto.getIdsDocentes().stream()
                .map(did -> docenteService.obtenerPorId(did)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Docente no válido")))
                .collect(java.util.stream.Collectors.toSet());

        cursoService.actualizarDocentes(curso, docentes);
        return ResponseEntity.noContent().build();
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrera no encontrada"));

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
        Set<Curso> prerrequisitos = dto.getPrerrequisitos() == null
                ? new HashSet<>()
                : dto.getPrerrequisitos()
                .stream()
                .map(p -> cursoService.obtenerPorId(p.getIdCurso())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prerrequisito inválido")))
                .collect(java.util.stream.Collectors.toSet());
        curso.setPrerrequisitos(prerrequisitos);

        // -------------------------------
        // DOCENTES DICTABLES
        // -------------------------------
        Set<Docente> docentes = dto.getDocentesDictables() == null
                ? new HashSet<>()
                : dto.getDocentesDictables()
                .stream()
                .map(d -> docenteService.obtenerPorId(d.getIdDocente())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Docente no válido")))
                .collect(java.util.stream.Collectors.toSet());
        curso.setDocentes(docentes);

        return curso;
    }

    private TipoCurso parseTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) return null;
        try {
            return TipoCurso.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private Modalidad parseModalidad(String modalidad) {
        if (modalidad == null || modalidad.isBlank()) return null;
        try {
            return Modalidad.valueOf(modalidad.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

}
