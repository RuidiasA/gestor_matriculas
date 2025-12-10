package com.example.matriculas.controller;

import com.example.matriculas.dto.PensionCuotaDTO;
import com.example.matriculas.dto.PensionesResponseDTO;
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
    public PensionesResponseDTO listarPagos(@RequestParam(value = "periodo", required = false) String periodo) {
        return alumnoPortalService.obtenerPagos(periodo);
    }

    @GetMapping("/pendientes")
    public PensionesResponseDTO listarPendientes(@RequestParam(value = "periodo", required = false) String periodo) {
        return alumnoPortalService.obtenerPagos(periodo);
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<PensionCuotaDTO> pagar(@PathVariable Long id) {
        PensionCuotaDTO dto = alumnoPortalService.marcarPagoComoPagado(id);
        return ResponseEntity.ok(dto);
    }
}
