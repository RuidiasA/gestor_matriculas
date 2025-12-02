package com.example.matriculas.controller;

import com.example.matriculas.dto.CursoMatriculadoDTO;
import com.example.matriculas.dto.MatriculaActualDTO;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alumno/matricula")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoMatriculaController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping("/actual")
    public MatriculaActualDTO obtenerMatriculaActual() {
        return alumnoPortalService.obtenerMatriculaActual();
    }

    @GetMapping("/cursos")
    public List<CursoMatriculadoDTO> cursosMatriculados() {
        return alumnoPortalService.cursosMatriculadosActuales();
    }
}
