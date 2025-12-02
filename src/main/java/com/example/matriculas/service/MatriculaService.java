package com.example.matriculas.service;

import com.example.matriculas.dto.request.MatriculaDetalleRequest;
import com.example.matriculas.dto.request.MatriculaRequest;
import com.example.matriculas.dto.response.MatriculaResponse;
import com.example.matriculas.enums.EstadoDetalleMatricula;
import com.example.matriculas.enums.EstadoMatricula;
import com.example.matriculas.mapper.MatriculaMapper;
import com.example.matriculas.model.*;
import com.example.matriculas.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final AlumnoRepository alumnoRepository;
    private final SeccionRepository seccionRepository;
    private final DocenteRepository docenteRepository;
    private final DetalleMatriculaRepository detalleMatriculaRepository;
    private final PagoRepository pagoRepository;
    private final MatriculaMapper matriculaMapper;

    @Transactional(readOnly = true)
    public List<MatriculaResponse> listar() {
        return matriculaRepository.findAll().stream().map(matriculaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MatriculaResponse obtener(Long id) {
        return matriculaMapper.toResponse(buscarMatricula(id));
    }

    @Transactional
    public MatriculaResponse crear(MatriculaRequest request) {
        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado"));
        Matricula matricula = Matricula.builder()
                .alumno(alumno)
                .cicloAcademico(request.cicloAcademico())
                .fechaMatricula(LocalDateTime.now())
                .estado(EstadoMatricula.PREREGISTRO)
                .totalCreditos(0)
                .totalHoras(0)
                .montoMatricula(BigDecimal.ZERO)
                .montoPension(BigDecimal.ZERO)
                .montoTotal(BigDecimal.ZERO)
                .build();
        Matricula guardada = matriculaRepository.save(matricula);
        if (request.detalles() != null) {
            agregarDetalles(guardada, request.detalles());
            recalcularTotales(guardada);
        }
        return matriculaMapper.toResponse(guardada);
    }

    @Transactional
    public MatriculaResponse actualizarEstado(Long id, EstadoMatricula estado) {
        Matricula matricula = buscarMatricula(id);
        matricula.setEstado(estado);
        return matriculaMapper.toResponse(matriculaRepository.save(matricula));
    }

    @Transactional(readOnly = true)
    public List<MatriculaResponse> obtenerPorAlumno(Long alumnoId) {
        return matriculaRepository.findByAlumnoId(alumnoId).stream().map(matriculaMapper::toResponse).toList();
    }

    @Transactional
    public void eliminar(Long id) {
        matriculaRepository.deleteById(id);
    }

    private void agregarDetalles(Matricula matricula, List<MatriculaDetalleRequest> detalleRequests) {
        List<DetalleMatricula> detalles = detalleRequests.stream().map(request -> {
            Seccion seccion = seccionRepository.findById(request.seccionId())
                    .orElseThrow(() -> new EntityNotFoundException("Sección no encontrada"));
            Docente docente = docenteRepository.findById(request.docenteId())
                    .orElseThrow(() -> new EntityNotFoundException("Docente no encontrado"));
            return DetalleMatricula.builder()
                    .matricula(matricula)
                    .seccion(seccion)
                    .docente(docente)
                    .creditos(request.creditos())
                    .horasSemanales(request.horasSemanales())
                    .aula(request.aula())
                    .modalidad(request.modalidad())
                    .horarioTexto(request.horarioTexto())
                    .estadoDetalle(EstadoDetalleMatricula.MATRICULADO)
                    .build();
        }).toList();
        detalleMatriculaRepository.saveAll(detalles);
        matricula.setDetalles(detalles);
    }

    private void recalcularTotales(Matricula matricula) {
        int totalCreditos = matricula.getDetalles().stream().mapToInt(DetalleMatricula::getCreditos).sum();
        int totalHoras = matricula.getDetalles().stream().mapToInt(DetalleMatricula::getHorasSemanales).sum();
        matricula.setTotalCreditos(totalCreditos);
        matricula.setTotalHoras(totalHoras);
        BigDecimal pagosMatricula = pagoRepository.findByMatriculaId(matricula.getId()).stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        matricula.setMontoTotal(pagosMatricula);
        matriculaRepository.save(matricula);
    }

    private Matricula buscarMatricula(Long id) {
        return matriculaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Matrícula no encontrada"));
    }
}
