package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PensionesResponseDTO {
    private List<String> periodosDisponibles;
    private String periodoActual;
    private List<PensionCuotaDTO> pagos;
    private PensionesTotalesDTO totales;
}
