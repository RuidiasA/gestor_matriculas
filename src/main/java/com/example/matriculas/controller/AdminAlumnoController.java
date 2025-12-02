package com.example.matriculas.controller;

import com.example.matriculas.dto.ActualizarAlumnoContactoDTO;
import com.example.matriculas.dto.AlumnoBusquedaDTO;
import com.example.matriculas.dto.AlumnoFichaDTO;
import com.example.matriculas.dto.CursoMatriculadoDTO;
import com.example.matriculas.dto.HistorialMatriculaDTO;
import com.example.matriculas.dto.ResumenMatriculaDTO;
import com.example.matriculas.service.AlumnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/alumnos")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAlumnoController {

    private final AlumnoService alumnoService;

    @GetMapping("/buscar")
    public List<AlumnoBusquedaDTO> buscar(@RequestParam(required = false, defaultValue = "") String filtro) {
        return alumnoService.buscar(filtro);
    }

    @GetMapping("/{id}")
    public AlumnoFichaDTO obtenerFicha(@PathVariable Long id) {
        return alumnoService.obtenerFicha(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarContacto(@PathVariable Long id,
                                                   @Valid @RequestBody ActualizarAlumnoContactoDTO dto) {
        alumnoService.actualizarContacto(id, dto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/matriculas")
    public List<CursoMatriculadoDTO> obtenerCursos(@PathVariable Long id, @RequestParam String ciclo) {
        return alumnoService.obtenerCursos(id, ciclo);
    }

    @GetMapping("/{id}/resumen")
    public ResumenMatriculaDTO obtenerResumen(@PathVariable Long id, @RequestParam String ciclo) {
        return alumnoService.obtenerResumen(id, ciclo);
    }

    @GetMapping("/{id}/historial")
    public List<HistorialMatriculaDTO> obtenerHistorial(@PathVariable Long id) {
        return alumnoService.obtenerHistorial(id);
    }
}
