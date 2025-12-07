package com.example.matriculas.controller;

import com.example.matriculas.dto.HorarioDTO;
import com.example.matriculas.dto.HorarioGuardadoDTO;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/alumno")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoHorarioController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping("/horario")
    public List<HorarioDTO> obtenerHorario() {
        return alumnoPortalService.obtenerHorarioActual();
    }

    @PostMapping("/horario/guardar")
    public HorarioGuardadoDTO guardarHorario() {
        var archivo = alumnoPortalService.guardarHorarioActual();
        return new HorarioGuardadoDTO("Horario guardado correctamente", archivo.toString());
    }

    @GetMapping("/horario/pdf")
    public ResponseEntity<ByteArrayResource> descargarPdf() {
        byte[] pdf = alumnoPortalService.generarHorarioPdf();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=horario.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(new ByteArrayResource(pdf));
    }
}
