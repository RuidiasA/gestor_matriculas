package com.example.matriculas.service;

import com.example.matriculas.dto.request.PagoRequest;
import com.example.matriculas.dto.response.PagoResponse;
import com.example.matriculas.enums.EstadoPago;
import com.example.matriculas.mapper.PagoMapper;
import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Matricula;
import com.example.matriculas.model.Pago;
import com.example.matriculas.repository.AlumnoRepository;
import com.example.matriculas.repository.MatriculaRepository;
import com.example.matriculas.repository.PagoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final AlumnoRepository alumnoRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagoMapper pagoMapper;

    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAll().stream().map(pagoMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> pagosAlumno(Long alumnoId) {
        return pagoRepository.findByAlumnoId(alumnoId).stream().map(pagoMapper::toResponse).toList();
    }

    @Transactional
    public PagoResponse registrar(PagoRequest request) {
        Alumno alumno = alumnoRepository.findById(request.alumnoId())
                .orElseThrow(() -> new EntityNotFoundException("Alumno no encontrado"));
        Matricula matricula = request.matriculaId() != null ? matriculaRepository.findById(request.matriculaId())
                .orElse(null) : null;
        Pago pago = Pago.builder()
                .alumno(alumno)
                .matricula(matricula)
                .periodo(request.periodo())
                .concepto(request.concepto())
                .tipo(request.tipo())
                .monto(request.monto())
                .estado(EstadoPago.PENDIENTE)
                .fechaVencimiento(request.fechaVencimiento())
                .build();
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public PagoResponse registrarPago(Long id, LocalDate fechaPago) {
        Pago pago = pagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pago no encontrado"));
        pago.setFechaPago(fechaPago);
        pago.setEstado(EstadoPago.PAGADO);
        return pagoMapper.toResponse(pagoRepository.save(pago));
    }

    @Transactional
    public void eliminar(Long id) {
        pagoRepository.deleteById(id);
    }
}
