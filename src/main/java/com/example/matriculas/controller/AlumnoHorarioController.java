package com.example.matriculas.controller;

import com.example.matriculas.dto.HorarioDTO;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alumno")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoHorarioController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping("/horario")
    public List<HorarioDTO> obtenerHorario() {
        return alumnoPortalService.obtenerHorarioActual();
    }
}
