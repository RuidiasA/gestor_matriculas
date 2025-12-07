package com.example.matriculas.controller;

import com.example.matriculas.dto.*;
import com.example.matriculas.model.SolicitudSeccion;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumno")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoCursosController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping("/cursos/disponibles")
    public List<CursoDisponibleDTO> cursosDisponibles(@RequestParam(required = false) String ciclo,
                                                      @RequestParam(required = false) String modalidad,
                                                      @RequestParam(required = false, name = "q") String texto) {
        return alumnoPortalService.buscarCursosDisponibles(ciclo, modalidad, texto);
    }

    @GetMapping("/cursos/{id}/detalle")
    public CursoDetalleAlumnoDTO detalleCurso(@PathVariable("id") Long seccionId) {
        return alumnoPortalService.obtenerDetalleCurso(seccionId);
    }

    @PostMapping("/matricula/{idSeccion}")
    public CursoMatriculadoDTO matricular(@PathVariable Long idSeccion) {
        return alumnoPortalService.matricular(idSeccion);
    }

    @DeleteMapping("/matricula/{idSeccion}")
    public void retirar(@PathVariable Long idSeccion) {
        alumnoPortalService.retirar(idSeccion);
    }

    @GetMapping("/historial")
    public List<HistorialAlumnoDTO> historial() {
        return alumnoPortalService.obtenerHistorial();
    }

    @PostMapping("/solicitudes")
    public void registrarSolicitud(@RequestBody SolicitudSeccion solicitud) {
        alumnoPortalService.registrarSolicitud(solicitud);
    }
}
