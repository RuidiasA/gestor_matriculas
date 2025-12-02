package com.example.matriculas.controller;

import com.example.matriculas.dto.request.AlumnoRequest;
import com.example.matriculas.dto.response.AlumnoResponse;
import com.example.matriculas.dto.response.MatriculaResponse;
import com.example.matriculas.service.AlumnoService;
import com.example.matriculas.service.MatriculaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumno")
@RequiredArgsConstructor
public class AlumnoPerfilController {

    private final AlumnoService alumnoService;
    private final MatriculaService matriculaService;

    @GetMapping("/{id}/perfil")
    public AlumnoResponse perfil(@PathVariable Long id) { return alumnoService.obtener(id); }

    @PutMapping("/{id}/perfil")
    public AlumnoResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlumnoRequest request) { return alumnoService.actualizar(id, request); }

    @GetMapping("/{id}/matriculas")
    public List<MatriculaResponse> matriculas(@PathVariable Long id) { return matriculaService.obtenerPorAlumno(id); }
}
