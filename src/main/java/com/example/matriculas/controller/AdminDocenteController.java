package com.example.matriculas.controller;

import com.example.matriculas.dto.request.DocenteRequest;
import com.example.matriculas.dto.response.DocenteResponse;
import com.example.matriculas.service.DocenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/docentes")
@RequiredArgsConstructor
public class AdminDocenteController {

    private final DocenteService docenteService;

    @GetMapping
    public List<DocenteResponse> listar() {
        return docenteService.listar();
    }

    @GetMapping("/{id}")
    public DocenteResponse obtener(@PathVariable Long id) {
        return docenteService.obtener(id);
    }

    @PostMapping
    public DocenteResponse crear(@Valid @RequestBody DocenteRequest request) {
        return docenteService.crear(request);
    }

    @PutMapping("/{id}")
    public DocenteResponse actualizar(@PathVariable Long id, @Valid @RequestBody DocenteRequest request) {
        return docenteService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        docenteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
