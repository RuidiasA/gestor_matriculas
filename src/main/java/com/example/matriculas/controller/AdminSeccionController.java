package com.example.matriculas.controller;

import com.example.matriculas.dto.EstudianteSeccionDTO;
import com.example.matriculas.dto.SeccionActualizarDTO;
import com.example.matriculas.dto.SeccionCatalogoDTO;
import com.example.matriculas.dto.SeccionDetalleDTO;
import com.example.matriculas.dto.SeccionHistorialDTO;
import com.example.matriculas.dto.SeccionHistorialCompletoDTO;
import com.example.matriculas.dto.SeccionHorariosActualizarDTO;
import com.example.matriculas.dto.SeccionListadoDTO;
import com.example.matriculas.dto.SeccionCambioDTO;
import com.example.matriculas.dto.SeccionEstadisticaDTO;
import com.example.matriculas.model.enums.EstadoSeccion;
import com.example.matriculas.service.SeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

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

    @GetMapping("/{id}/historial")
    public List<SeccionHistorialDTO> obtenerHistorial(@PathVariable Long id) {
        return seccionService.obtenerHistorial(id);
    }

    @GetMapping("/{id}/historial-completo")
    public SeccionHistorialCompletoDTO obtenerHistorialCompleto(@PathVariable Long id) {
        return seccionService.obtenerHistorialCompleto(id);
    }

    @GetMapping("/{id}/cambios")
    public List<SeccionCambioDTO> obtenerCambios(@PathVariable Long id) {
        return seccionService.obtenerCambios(id);
    }

    @GetMapping("/{id}/estadisticas")
    public SeccionEstadisticaDTO obtenerEstadisticas(@PathVariable Long id) {
        return seccionService.obtenerEstadisticas(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizar(@PathVariable Long id, @Valid @RequestBody SeccionActualizarDTO dto) {
        seccionService.actualizar(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/horarios")
    public ResponseEntity<Void> actualizarHorarios(@PathVariable Long id, @Valid @RequestBody SeccionHorariosActualizarDTO dto) {
        seccionService.actualizarHorarios(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable Long id) {
        seccionService.anular(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/log")
    public ResponseEntity<Void> registrarLog(@PathVariable Long id, @RequestBody SeccionCambioDTO dto) {
        seccionService.registrarLogManual(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/actualizar-docente")
    public ResponseEntity<Void> actualizarDocente(@PathVariable Long id, @RequestParam Long docenteId) {
        seccionService.actualizarDocente(id, docenteId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/actualizar-aula")
    public ResponseEntity<Void> actualizarAula(@PathVariable Long id, @RequestParam String aula) {
        seccionService.actualizarAula(id, aula);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/actualizar-estado")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id, @RequestParam String estado) {
        seccionService.actualizarEstado(id, EstadoSeccion.valueOf(estado));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/actualizar-cupos")
    public ResponseEntity<Void> actualizarCupos(@PathVariable Long id, @RequestParam int cupos) {
        seccionService.actualizarCupos(id, cupos);
        return ResponseEntity.noContent().build();
    }
}
