package com.example.matriculas.controller;

import com.example.matriculas.dto.request.AlumnoRequest;
import com.example.matriculas.dto.response.AlumnoResponse;
import com.example.matriculas.dto.response.MatriculaResponse;
import com.example.matriculas.service.AlumnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/alumnos")
@RequiredArgsConstructor
public class AdminAlumnoController {

    private final AlumnoService alumnoService;

    @GetMapping
    public List<AlumnoResponse> listar() {
        return alumnoService.listar();
    }

    @GetMapping("/{id}")
    public AlumnoResponse obtener(@PathVariable Long id) {
        return alumnoService.obtener(id);
    }

    @PostMapping
    public AlumnoResponse crear(@Valid @RequestBody AlumnoRequest request) {
        return alumnoService.crear(request);
    }

    @PutMapping("/{id}")
    public AlumnoResponse actualizar(@PathVariable Long id, @Valid @RequestBody AlumnoRequest request) {
        return alumnoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        alumnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historial")
    public List<MatriculaResponse> historial(@PathVariable Long id) {
        return alumnoService.obtenerHistorialMatriculas(id);
    }
}
