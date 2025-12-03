package com.example.matriculas.dto;

import com.example.matriculas.model.Docente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocenteBusquedaDTO {

    private Long id;
    private String codigo;
    private String nombreCompleto;
    private String dni;
    private String estado;

    public static DocenteBusquedaDTO fromEntity(Docente d) {
        if (d == null) return null;

        return DocenteBusquedaDTO.builder()
                .id(d.getId())
                .codigo(d.getCodigoDocente())
                .nombreCompleto((d.getApellidos() + " " + d.getNombres()).trim())
                .dni(d.getDni())
                .estado(d.getEstado() != null ? d.getEstado().name() : null)
                .build();
    }
}
