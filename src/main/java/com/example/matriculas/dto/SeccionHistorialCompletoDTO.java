package com.example.matriculas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeccionHistorialCompletoDTO {
    private List<SeccionCambioDTO> cambios;
    private List<MovimientoMatriculaDTO> movimientos;
    private SeccionEstadisticaDTO estadisticas;
}
