package com.example.matriculas.dto;

import lombok.Data;

import java.util.List;

@Data
public class CursoPrerrequisitoUpdateDTO {
    private List<Long> idsPrerrequisitos;
}
