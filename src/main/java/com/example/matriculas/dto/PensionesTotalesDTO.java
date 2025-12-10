package com.example.matriculas.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PensionesTotalesDTO {
    private Double deudaPendiente;
    private Double totalPagado;
    private Double totalInvertidoDelAlumno;
}
