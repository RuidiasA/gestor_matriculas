package com.example.matriculas.controller;

import com.example.matriculas.dto.request.MatriculaRequest;
import com.example.matriculas.dto.response.MatriculaResponse;
import com.example.matriculas.enums.EstadoMatricula;
import com.example.matriculas.service.MatriculaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/matriculas")
@RequiredArgsConstructor
public class AdminMatriculaController {

    private final MatriculaService matriculaService;

    @GetMapping
    public List<MatriculaResponse> listar() { return matriculaService.listar(); }

    @GetMapping("/{id}")
    public MatriculaResponse obtener(@PathVariable Long id) { return matriculaService.obtener(id); }

    @PostMapping
    public MatriculaResponse crear(@Valid @RequestBody MatriculaRequest request) { return matriculaService.crear(request); }

    @PatchMapping("/{id}/estado")
    public MatriculaResponse actualizarEstado(@PathVariable Long id, @RequestParam EstadoMatricula estado) {
        return matriculaService.actualizarEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { matriculaService.eliminar(id); return ResponseEntity.noContent().build(); }
}
