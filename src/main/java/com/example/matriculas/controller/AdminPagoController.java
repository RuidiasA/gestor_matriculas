package com.example.matriculas.controller;

import com.example.matriculas.dto.request.PagoRequest;
import com.example.matriculas.dto.response.PagoResponse;
import com.example.matriculas.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/pagos")
@RequiredArgsConstructor
public class AdminPagoController {

    private final PagoService pagoService;

    @GetMapping
    public List<PagoResponse> listar() { return pagoService.listar(); }

    @PostMapping
    public PagoResponse registrar(@Valid @RequestBody PagoRequest request) { return pagoService.registrar(request); }

    @PatchMapping("/{id}/registrar")
    public PagoResponse registrarPago(@PathVariable Long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaPago) {
        return pagoService.registrarPago(id, fechaPago);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) { pagoService.eliminar(id); return ResponseEntity.noContent().build(); }
}
