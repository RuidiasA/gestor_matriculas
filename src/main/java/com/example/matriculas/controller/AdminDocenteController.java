package com.example.matriculas.controller;

import com.example.matriculas.dto.CursoDTO;
import com.example.matriculas.dto.DocenteActualizarContactoDTO;
import com.example.matriculas.dto.DocenteActualizarDatosDTO;
import com.example.matriculas.dto.DocenteBusquedaDTO;
import com.example.matriculas.dto.DocenteCursoDictableDTO;
import com.example.matriculas.dto.DocenteDetalleDTO;
import com.example.matriculas.model.enums.EstadoDocente;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.service.DocenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/docentes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDocenteController {

    private final DocenteService docenteService;
    private final CursoRepository cursoRepository;

    // ===========================================================
    // 1. BÚSQUEDA GENERAL DE DOCENTES
    // ===========================================================
    @GetMapping("/buscar")
    public List<DocenteBusquedaDTO> buscar(
            @RequestParam(defaultValue = "") String filtro,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) String estadoStr
    ) {

        // Convertir estado recibido
        EstadoDocente estado = null;

        if (estadoStr != null && !estadoStr.isBlank()) {
            try {
                estado = EstadoDocente.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado inválido: " + estadoStr);
            }
        }

        // 🔥 El service retorna List<DocenteBusquedaDTO> directamente
        return docenteService.buscar(
                filtro.trim(),
                cursoId,
                estado != null ? estado.name() : null
        );
    }

    // ===========================================================
    // 2. LISTAR CURSOS DISPONIBLES PARA ASIGNAR A DOCENTE
    // ===========================================================
    @GetMapping(value = "/cursos", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CursoDTO> listarCursosDisponibles() {
        return cursoRepository.findAll()
                .stream()
                .map(c -> CursoDTO.builder()
                        .idCurso(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .creditos(c.getCreditos())
                        .ciclo(c.getCiclo())
                        .build())
                .toList();
    }

    // ===========================================================
    // 3. DETALLE COMPLETO DEL DOCENTE
    // ===========================================================
    @GetMapping("/{id}")
    public DocenteDetalleDTO detalle(@PathVariable Long id) {
        return docenteService.obtenerDetalle(id);
    }

    // ===========================================================
    // 4. ACTUALIZAR DATOS PERSONALES + ESTADO
    // ===========================================================
    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarDatos(
            @PathVariable Long id,
            @Valid @RequestBody DocenteActualizarDatosDTO dto
    ) {
        docenteService.actualizarDatos(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ===========================================================
    // 5. ACTUALIZAR CONTACTO (teléfono, correos, dirección)
    // ===========================================================
    @PutMapping("/{id}/contacto")
    public ResponseEntity<Void> actualizarContacto(
            @PathVariable Long id,
            @Valid @RequestBody DocenteActualizarContactoDTO dto
    ) {
        docenteService.actualizarContacto(id, dto);
        return ResponseEntity.noContent().build();
    }

    // ===========================================================
    // 6. AGREGAR CURSO DICTABLE
    // ===========================================================
    @PostMapping("/{id}/cursos")
    public ResponseEntity<DocenteCursoDictableDTO> agregarCurso(
            @PathVariable Long id,
            @RequestParam Long cursoId
    ) {
        DocenteCursoDictableDTO dto = docenteService.agregarCursoDictado(id, cursoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // ===========================================================
    // 7. ELIMINAR CURSO DICTABLE
    // ===========================================================
    @DeleteMapping("/{id}/cursos/{cursoId}")
    public ResponseEntity<Void> eliminarCurso(
            @PathVariable Long id,
            @PathVariable Long cursoId
    ) {
        docenteService.eliminarCursoDictado(id, cursoId);
        return ResponseEntity.noContent().build();
    }
}
