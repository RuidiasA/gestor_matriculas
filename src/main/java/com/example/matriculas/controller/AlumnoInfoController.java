package com.example.matriculas.controller;

import com.example.matriculas.dto.AlumnoPerfilDTO;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alumno")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoInfoController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping("/info")
    public AlumnoPerfilDTO obtenerPerfil() {
        return alumnoPortalService.obtenerPerfil();
    }
}
