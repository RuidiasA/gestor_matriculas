package com.example.matriculas.controller;

import com.example.matriculas.dto.request.SeccionRequest;
import com.example.matriculas.dto.response.SeccionResponse;
import com.example.matriculas.service.SeccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/secciones")
@RequiredArgsConstructor
public class AdminSeccionController {

    private final SeccionService seccionService;

    @GetMapping
    public List<SeccionResponse> listar() { return seccionService.listar(); }

    @GetMapping("/{id}")
    public SeccionResponse obtener(@PathVariable Long id) { return seccionService.obtener(id); }

    @PostMapping
    public SeccionResponse crear(@Valid @RequestBody SeccionRequest request) { return seccionService.crear(request); }

    @PutMapping("/{id}")
    public SeccionResponse actualizar(@PathVariable Long id, @Valid @RequestBody SeccionRequest request) { return seccionService.actualizar(id, request); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { seccionService.eliminar(id); return ResponseEntity.noContent().build(); }
}
