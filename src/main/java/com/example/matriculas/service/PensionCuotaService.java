package com.example.matriculas.service;

import com.example.matriculas.model.Matricula;
import com.example.matriculas.model.PensionCuota;
import com.example.matriculas.model.enums.EstadoPago;
import com.example.matriculas.model.enums.TipoConcepto;
import com.example.matriculas.repository.PensionCuotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PensionCuotaService {

    private static final double MONTO_MATRICULA = 350.0;
    private static final double MONTO_PENSION = 800.0;

    private final PensionCuotaRepository pensionCuotaRepository;

    @Transactional
    public List<PensionCuota> generarCuotasSiNoExisten(Matricula matricula) {
        if (matricula == null || matricula.getAlumno() == null || matricula.getCicloAcademico() == null) {
            return List.of();
        }
        List<PensionCuota> existentes = pensionCuotaRepository
                .findByAlumnoAndPeriodo(matricula.getAlumno().getId(), matricula.getCicloAcademico());
        if (!existentes.isEmpty()) {
            return existentes;
        }
        LocalDate fechaBase = Optional.ofNullable(matricula.getFechaMatricula())
                .map(fecha -> fecha.toLocalDate())
                .orElse(LocalDate.now());

        List<PensionCuota> cuotas = new ArrayList<>();

        PensionCuota matriculaCuota = construirCuota(matricula, TipoConcepto.MATRICULA, null,
                fechaBase.plusDays(7), MONTO_MATRICULA);
        cuotas.add(matriculaCuota);

        LocalDate primerVencimiento = fechaBase.withDayOfMonth(Math.min(28, fechaBase.getDayOfMonth()));
        for (int i = 1; i <= 5; i++) {
            LocalDate vencimiento = primerVencimiento.plusMonths(i - 1)
                    .with(TemporalAdjusters.lastDayOfMonth());
            PensionCuota cuota = construirCuota(matricula, TipoConcepto.PENSION, i, vencimiento, MONTO_PENSION);
            cuotas.add(cuota);
        }
        return pensionCuotaRepository.saveAll(cuotas);
    }

    @Transactional(readOnly = true)
    public List<PensionCuota> obtenerCuotas(Long alumnoId, String periodo) {
        return pensionCuotaRepository.findByAlumnoAndPeriodo(alumnoId, periodo);
    }

    @Transactional
    public PensionCuota registrarPago(Long cuotaId, Long alumnoId) {
        PensionCuota cuota = pensionCuotaRepository.findByIdAndAlumnoId(cuotaId, alumnoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado"));
        if (cuota.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuota ya fue pagada");
        }
        cuota.actualizarEstadoPorVencimiento(LocalDate.now());
        if (cuota.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cuota ya fue pagada");
        }
        cuota.setEstadoPago(EstadoPago.PAGADO);
        cuota.setFechaPago(LocalDate.now());
        return pensionCuotaRepository.save(cuota);
    }

    @Transactional
    public void actualizarEstadosVencidos(List<PensionCuota> cuotas) {
        LocalDate hoy = LocalDate.now();
        boolean actualizar = false;
        for (PensionCuota cuota : cuotas) {
            EstadoPago antes = cuota.getEstadoPago();
            cuota.actualizarEstadoPorVencimiento(hoy);
            if (antes != cuota.getEstadoPago()) {
                actualizar = true;
            }
        }
        if (cuotas.isEmpty() || !actualizar) {
            return;
        }
        pensionCuotaRepository.saveAll(cuotas);
    }

    private PensionCuota construirCuota(Matricula matricula, TipoConcepto tipo, Integer numeroCuota,
                                        LocalDate vencimiento, double monto) {
        PensionCuota cuota = new PensionCuota();
        cuota.setAlumno(matricula.getAlumno());
        cuota.setMatricula(matricula);
        cuota.setPeriodoAcademico(matricula.getCicloAcademico());
        cuota.setTipoConcepto(tipo);
        cuota.setNumeroCuota(numeroCuota);
        cuota.setFechaVencimiento(vencimiento);
        cuota.setImporteOriginal(monto);
        cuota.setMora(0.0);
        cuota.setDescuento(0.0);
        cuota.setEstadoPago(EstadoPago.PENDIENTE);
        cuota.recalcularImporteFinal();
        return cuota;
    }
}
