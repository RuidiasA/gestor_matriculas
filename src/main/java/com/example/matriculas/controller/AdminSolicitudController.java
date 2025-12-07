package com.example.matriculas.controller;

import com.example.matriculas.dto.SolicitudSeccionAdminDTO;
import com.example.matriculas.service.SolicitudSeccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/solicitudes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSolicitudController {

    private final SolicitudSeccionService solicitudSeccionService;

    @GetMapping
    public List<SolicitudSeccionAdminDTO> listarTodas(@RequestParam(value = "estado", required = false) String estado) {
        if ("PENDIENTE".equalsIgnoreCase(estado)) {
            return solicitudSeccionService.listarPendientes();
        }
        return solicitudSeccionService.listarTodas();
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<Void> aprobar(@PathVariable Long id) {
        solicitudSeccionService.aprobar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<Void> rechazar(@PathVariable Long id, @RequestBody(required = false) String razon) {
        solicitudSeccionService.rechazar(id, razon);
        return ResponseEntity.noContent().build();
    }
}
