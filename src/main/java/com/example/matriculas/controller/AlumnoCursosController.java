package com.example.matriculas.controller;

import com.example.matriculas.dto.*;
import com.example.matriculas.service.AlumnoPortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alumno")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ALUMNO')")
public class AlumnoCursosController {

    private final AlumnoPortalService alumnoPortalService;

    @GetMapping("/cursos/periodos")
    public List<String> periodosDisponibles() {
        return alumnoPortalService.obtenerPeriodosDisponibles();
    }

    @GetMapping("/cursos/disponibles")
    public List<CursoDisponibleDTO> cursosDisponibles(@RequestParam(required = false) String ciclo,
                                                      @RequestParam(required = false) String modalidad,
                                                      @RequestParam(required = false, name = "q") String texto) {
        return alumnoPortalService.buscarCursosDisponibles(ciclo, modalidad, texto);
    }

    @GetMapping("/cursos/{id}/secciones")
    public List<CursoDetalleAlumnoDTO> seccionesPorCurso(@PathVariable("id") Long cursoId) {
        return alumnoPortalService.listarSeccionesPorCurso(cursoId);
    }

    @GetMapping("/cursos/{id}/detalle")
    public CursoDetalleAlumnoDTO detalleCurso(@PathVariable("id") Long seccionId) {
        return alumnoPortalService.obtenerDetalleCurso(seccionId);
    }

    @PostMapping("/matricula/{idSeccion}")
    public CursoMatriculadoDTO matricular(@PathVariable Long idSeccion) {
        return alumnoPortalService.matricular(idSeccion);
    }

    @GetMapping("/matricula/validar/{idSeccion}")
    public ValidacionMatriculaDTO validar(@PathVariable Long idSeccion) {
        return alumnoPortalService.validarMatricula(idSeccion);
    }

    @DeleteMapping("/matricula/{idSeccion}")
    public void retirar(@PathVariable Long idSeccion) {
        alumnoPortalService.retirar(idSeccion);
    }

    @GetMapping("/historial")
    public List<HistorialAlumnoDTO> historial() {
        return alumnoPortalService.obtenerHistorial();
    }

    @PostMapping(value = "/solicitudes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void registrarSolicitud(@ModelAttribute SolicitudSeccionCreateDTO solicitud) {
        alumnoPortalService.registrarSolicitud(solicitud);
    }

    @GetMapping("/solicitudes/{id}/evidencia")
    public ResponseEntity<Resource> descargarEvidencia(@PathVariable Long id) {
        return alumnoPortalService.descargarEvidencia(id);
    }

    @GetMapping("/solicitudes")
    public List<SolicitudSeccionAlumnoDTO> listarSolicitudes() {
        return alumnoPortalService.listarSolicitudesAlumno();
    }

    @GetMapping("/solicitudes/cursos")
    public List<CursoSolicitudAlumnoDTO> cursosSolicitables(@RequestParam(value = "carreraId", required = false) Long carreraId,
                                                            @RequestParam(value = "ciclo", required = false) Integer ciclo) {
        return alumnoPortalService.listarCursosSolicitables(carreraId, ciclo);
    }
}
