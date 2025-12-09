package com.example.matriculas.controller;

import com.example.matriculas.dto.SolicitudSeccionDetalleDTO;
import com.example.matriculas.dto.SolicitudSeccionListadoDTO;
import com.example.matriculas.dto.SolicitudSeccionUpdateDTO;
import com.example.matriculas.model.Carrera;
import com.example.matriculas.model.Curso;
import com.example.matriculas.model.enums.EstadoSolicitud;
import com.example.matriculas.repository.CarreraRepository;
import com.example.matriculas.service.SolicitudSeccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin/solicitudes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSolicitudController {

    private final SolicitudSeccionService solicitudSeccionService;
    private final CarreraRepository carreraRepository;

    @GetMapping
    public List<SolicitudSeccionListadoDTO> listarTodas(@RequestParam(value = "estado", required = false) String estado,
                                                        @RequestParam(value = "cursoId", required = false) Long cursoId,
                                                        @RequestParam(value = "carreraId", required = false) Long carreraId,
                                                        @RequestParam(value = "ciclo", required = false) String ciclo,
                                                        @RequestParam(value = "desde", required = false) String desde,
                                                        @RequestParam(value = "hasta", required = false) String hasta) {
        LocalDate inicio = parseFecha(desde);
        LocalDate fin = parseFecha(hasta);
        return solicitudSeccionService.listar(estado, cursoId, carreraId, ciclo, inicio, fin);
    }

    @GetMapping("/count")
    public Map<String, Long> contarPendientes(@RequestParam("estado") String estado) {
        EstadoSolicitud estadoSolicitud = EstadoSolicitud.valueOf(estado.toUpperCase());
        return Map.of("total", solicitudSeccionService.contarPorEstado(estadoSolicitud));
    }

    @GetMapping("/catalogo")
    public Map<String, Object> catalogoFiltros() {
        List<Map<String, Object>> cursos = solicitudSeccionService.catalogoCursos().stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("nombre", c.getNombre());
                    map.put("codigo", c.getCodigo());
                    map.put("carrera", Optional.ofNullable(c.getCarrera()).map(Carrera::getNombre).orElse(null));
                    map.put("carreraId", Optional.ofNullable(c.getCarrera()).map(Carrera::getId).orElse(null));
                    map.put("ciclo", c.getCiclo());
                    return map;
                }).toList();
        List<Map<String, Object>> carreras = carreraRepository.findAll().stream()
                .map(c -> Map.of("id", c.getId(), "nombre", c.getNombre()))
                .toList();
        return Map.of(
                "cursos", cursos,
                "carreras", carreras,
                "ciclos", solicitudSeccionService.catalogoCiclos()
        );
    }

    @GetMapping("/{id}")
    public SolicitudSeccionDetalleDTO obtenerDetalle(@PathVariable Long id) {
        return solicitudSeccionService.obtenerDetalle(id);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> actualizarEstado(@PathVariable Long id, @Valid @RequestBody SolicitudSeccionUpdateDTO dto) {
        dto.setId(id);
        solicitudSeccionService.actualizarEstado(id, dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public String exportarCsv(@RequestParam(value = "estado", required = false) String estado,
                              @RequestParam(value = "cursoId", required = false) Long cursoId,
                              @RequestParam(value = "carreraId", required = false) Long carreraId,
                              @RequestParam(value = "ciclo", required = false) String ciclo,
                              @RequestParam(value = "desde", required = false) String desde,
                              @RequestParam(value = "hasta", required = false) String hasta) {
        List<SolicitudSeccionListadoDTO> solicitudes = listarTodas(estado, cursoId, carreraId, ciclo, desde, hasta);
        String encabezado = "Alumno,Curso,Código,Carrera,Ciclo,Estado,Fecha,Solicitantes";
        String cuerpo = solicitudes.stream()
                .map(s -> String.join(",",
                        wrap(s.getAlumno()),
                        wrap(s.getCurso()),
                        wrap(s.getCodigoCurso()),
                        wrap(s.getCarrera()),
                        wrap(s.getCiclo()),
                        wrap(s.getEstado()),
                        wrap(s.getFechaSolicitud() != null ? s.getFechaSolicitud().toString() : ""),
                        Optional.ofNullable(s.getSolicitantes()).map(Object::toString).orElse("0")))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");
        return encabezado + (cuerpo.isEmpty() ? "" : "\n" + cuerpo);
    }

    @GetMapping("/curso/{cursoId}/pendientes")
    public List<SolicitudSeccionListadoDTO> pendientesPorCurso(@PathVariable Long cursoId) {
        return solicitudSeccionService.listarPendientesPorCurso(cursoId);
    }

    private LocalDate parseFecha(String valor) {
        if (!org.springframework.util.StringUtils.hasText(valor)) {
            return null;
        }
        return LocalDate.parse(valor);
    }

    private String wrap(String valor) {
        return valor == null ? "" : '"' + valor.replace("\"", "'") + '"';
    }
}
