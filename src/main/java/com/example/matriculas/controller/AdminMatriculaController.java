package com.example.matriculas.controller;

import com.example.matriculas.dto.AlumnoInfoDTO;
import com.example.matriculas.dto.CursoMatriculadoDTO;
import com.example.matriculas.dto.HistorialMatriculaDTO;
import com.example.matriculas.service.AlumnoService;
import com.example.matriculas.service.MatriculaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/matriculas")
@RequiredArgsConstructor
public class AdminMatriculaController {

    private final MatriculaService matriculaService;

    // CICLOS
    @GetMapping("/{alumnoId}/ciclos")
    public List<String> ciclos(@PathVariable Long alumnoId) {
        return matriculaService.obtenerCiclosAlumno(alumnoId);
    }

    // CURSOS POR CICLO
    @GetMapping("/{alumnoId}/ciclo/{ciclo}")
    public List<CursoMatriculadoDTO> cursos(
            @PathVariable Long alumnoId,
            @PathVariable String ciclo) {
        return matriculaService.obtenerCursosPorCiclo(alumnoId, ciclo);
    }

    // HISTORIAL
    @GetMapping("/{alumnoId}/historial")
    public List<HistorialMatriculaDTO> historial(@PathVariable Long alumnoId) {
        return matriculaService.obtenerHistorial(alumnoId);
    }
}

