package com.example.matriculas.controller;

import com.example.matriculas.dto.EstudianteSeccionDTO;
import com.example.matriculas.dto.SeccionCatalogoDTO;
import com.example.matriculas.dto.SeccionDetalleDTO;
import com.example.matriculas.dto.SeccionListadoDTO;
import com.example.matriculas.service.SeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/secciones")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSeccionController {

    private final SeccionService seccionService;

    @GetMapping("/catalogos")
    public SeccionCatalogoDTO obtenerCatalogos() {
        return seccionService.obtenerCatalogos();
    }

    @GetMapping("/buscar")
    public List<SeccionListadoDTO> buscar(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) Long docenteId,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String codigo
    ) {
        return seccionService.buscar(cursoId, periodo, docenteId, modalidad, codigo);
    }

    @GetMapping("/{id}")
    public SeccionDetalleDTO obtenerDetalle(@PathVariable Long id) {
        return seccionService.obtenerDetalle(id);
    }

    @GetMapping("/{id}/estudiantes")
    public List<EstudianteSeccionDTO> listarEstudiantes(@PathVariable Long id) {
        return seccionService.listarEstudiantes(id);
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        seccionService.anular(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
