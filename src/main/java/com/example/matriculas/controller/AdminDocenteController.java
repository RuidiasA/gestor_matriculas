package com.example.matriculas.controller;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.enums.EstadoDocente;
import com.example.matriculas.repository.CursoRepository;
import com.example.matriculas.service.DocenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/buscar")
    public List<DocenteBusquedaDTO> buscar(String filtro, Long cursoId, String estadoStr) {

        EstadoDocente estado = null;

        if (estadoStr != null && !estadoStr.isBlank()) {
            try {
                estado = EstadoDocente.valueOf(estadoStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado inválido: " + estadoStr);
            }
        }

        return docenteRepository.buscar(filtro.toLowerCase(), estado, cursoId)
                .stream()
                .map(DocenteBusquedaDTO::fromEntity)
                .toList();
    }


    @GetMapping("/cursos")
    public List<DocenteCursoDictableDTO> listarCursosDisponibles() {
        return cursoRepository.findAll()
                .stream()
                .map(c -> DocenteCursoDictableDTO.builder()
                        .idCurso(c.getId())
                        .codigo(c.getCodigo())
                        .nombre(c.getNombre())
                        .creditos(c.getCreditos())
                        .ciclo(c.getCiclo())
                        .build())
                .toList();
    }

    @GetMapping("/{id}")
    public DocenteDetalleDTO detalle(@PathVariable Long id) {
        return docenteService.obtenerDetalle(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarDatos(
            @PathVariable Long id,
            @Valid @RequestBody DocenteActualizarDatosDTO dto
    ) {
        docenteService.actualizarDatos(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/contacto")
    public ResponseEntity<Void> actualizarContacto(
            @PathVariable Long id,
            @Valid @RequestBody DocenteActualizarContactoDTO dto
    ) {
        docenteService.actualizarContacto(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/cursos")
    public ResponseEntity<DocenteCursoDictableDTO> agregarCurso(
            @PathVariable Long id,
            @RequestParam Long cursoId
    ) {
        DocenteCursoDictableDTO dto = docenteService.agregarCursoDictable(id, cursoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{id}/cursos/{cursoId}")
    public ResponseEntity<Void> eliminarCurso(
            @PathVariable Long id,
            @PathVariable Long cursoId
    ) {
        docenteService.eliminarCursoDictable(id, cursoId);
        return ResponseEntity.noContent().build();
    }
}
