package com.example.matriculas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parametro")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String clave; // ej: MATRICULA_INICIO, MATRICULA_FIN

    @Column(nullable = false, length = 200)
    private String valor;
}
