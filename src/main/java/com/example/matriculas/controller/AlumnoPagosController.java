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
    public List<PagoDTO> listarPagos(@RequestParam(value = "periodo", required = false) String periodo) {
        return alumnoPortalService.obtenerPagos(false, periodo);
    }

    @GetMapping("/pendientes")
    public List<PagoDTO> listarPendientes(@RequestParam(value = "periodo", required = false) String periodo) {
        return alumnoPortalService.obtenerPagos(true, periodo);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<Void> pagar(@PathVariable Long id) {
        alumnoPortalService.marcarPagoComoPagado(id);
        return ResponseEntity.noContent().build();
    }
}
