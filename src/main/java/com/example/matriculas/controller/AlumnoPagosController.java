package com.example.matriculas.controller;

import com.example.matriculas.dto.PagoDTO;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumno/pagos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoPagosController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping
    public List<PagoDTO> listarPagos() {
        return alumnoPortalService.obtenerPagos(false);
    }

    @GetMapping("/pendientes")
    public List<PagoDTO> listarPendientes() {
        return alumnoPortalService.obtenerPagos(true);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<Void> pagar(@PathVariable Long id) {
        alumnoPortalService.marcarPagoComoPagado(id);
        return ResponseEntity.noContent().build();
    }
}
