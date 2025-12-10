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
import java.util.*;

@Service
@RequiredArgsConstructor
public class PensionCuotaService {

    private static final double COSTO_MATRICULA = 400.0;

    private final PensionCuotaRepository pensionCuotaRepository;

    @Transactional
    public List<PensionCuota> generarCuotasSiNoExisten(Matricula matricula) {
        if (matricula == null || matricula.getAlumno() == null || matricula.getCicloAcademico() == null) {
            return List.of();
        }

        Long alumnoId = matricula.getAlumno().getId();
        String ciclo = matricula.getCicloAcademico();

        int creditos = Optional.ofNullable(matricula.getTotalCreditos()).orElse(0);
        double costoPensionCuota = creditos * 50.0;

        List<PensionCuota> existentes =
                pensionCuotaRepository.findByAlumnoAndPeriodo(alumnoId, ciclo);

        LocalDate fechaBase = Optional.ofNullable(matricula.getFechaMatricula())
                .map(f -> f.toLocalDate())
                .orElse(LocalDate.now());

        List<PensionCuota> resultado = new ArrayList<>();

        // =========================================================================
        // 1. MATRÍCULA — Única cuota
        // =========================================================================
        PensionCuota cuotaMatricula = existentes.stream()
                .filter(c -> c.getTipoConcepto() == TipoConcepto.MATRICULA)
                .findFirst()
                .orElseGet(() -> {
                    PensionCuota nueva = new PensionCuota();
                    nueva.setAlumno(matricula.getAlumno());
                    nueva.setMatricula(matricula);
                    nueva.setPeriodoAcademico(ciclo);
                    nueva.setTipoConcepto(TipoConcepto.MATRICULA);
                    nueva.setNumeroCuota(null);
                    return nueva;
                });

        // Actualizar vencimiento incluso si ya existía (pero no si está pagada)
        if (cuotaMatricula.getEstadoPago() == null || cuotaMatricula.getEstadoPago() != EstadoPago.PAGADO) {
            cuotaMatricula.setFechaVencimiento(fechaBase.plusDays(7));
            cuotaMatricula.setImporteOriginal(COSTO_MATRICULA);
            cuotaMatricula.setDescuento(0.0);
            cuotaMatricula.setMora(0.0);
            cuotaMatricula.recalcularImporteFinal();
        }
        resultado.add(cuotaMatricula);


        // =========================================================================
        // 2. PENSIONES — 5 cuotas
        // =========================================================================
        final Matricula matriculaFinal = matricula;
        final String cicloFinal = ciclo;

        List<PensionCuota> pensionesExistentes = existentes.stream()
                .filter(c -> c.getTipoConcepto() == TipoConcepto.PENSION)
                .sorted(Comparator.comparing(c -> Optional.ofNullable(c.getNumeroCuota()).orElse(0)))
                .toList();

        final LocalDate primerVencimiento = fechaBase.withDayOfMonth(
                Math.min(28, fechaBase.getDayOfMonth())
        );

        int NUM_CUOTAS = 5;

        for (int i = 1; i <= NUM_CUOTAS; i++) {

            final int numeroCuota = i; // ¡IMPORTANTE!

            PensionCuota cuota = pensionesExistentes.stream()
                    .filter(c -> Objects.equals(c.getNumeroCuota(), numeroCuota))
                    .findFirst()
                    .orElseGet(() -> {
                        PensionCuota nueva = new PensionCuota();
                        nueva.setAlumno(matriculaFinal.getAlumno());
                        nueva.setMatricula(matriculaFinal);
                        nueva.setPeriodoAcademico(cicloFinal);
                        nueva.setTipoConcepto(TipoConcepto.PENSION);
                        nueva.setNumeroCuota(numeroCuota);
                        return nueva;
                    });

            // Actualizar vencimiento si no está pagada
            if (cuota.getEstadoPago() == null || cuota.getEstadoPago() != EstadoPago.PAGADO) {
                cuota.setFechaVencimiento(
                        primerVencimiento.plusMonths(numeroCuota - 1)
                                .with(TemporalAdjusters.lastDayOfMonth())
                );
                cuota.setImporteOriginal(costoPensionCuota);
                cuota.setDescuento(0.0);
                cuota.setMora(0.0);
                cuota.recalcularImporteFinal();
            }

            resultado.add(cuota);
        }



        return pensionCuotaRepository.saveAll(resultado);
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

        cuota.setEstadoPago(EstadoPago.PAGADO);
        cuota.setFechaPago(LocalDate.now());

        return pensionCuotaRepository.save(cuota);
    }

    @Transactional
    public void actualizarEstadosVencidos(List<PensionCuota> cuotas) {
        if (cuotas == null || cuotas.isEmpty()) return;

        LocalDate hoy = LocalDate.now();
        boolean requiereGuardar = false;

        for (PensionCuota cuota : cuotas) {
            EstadoPago estadoAntes = cuota.getEstadoPago();
            cuota.actualizarEstadoPorVencimiento(hoy);

            if (estadoAntes != cuota.getEstadoPago()) {
                requiereGuardar = true;
            }
        }

        if (requiereGuardar) {
            pensionCuotaRepository.saveAll(cuotas);
        }
    }

}
