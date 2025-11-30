package com.example.matriculas.dto;

import lombok.Data;

@Data
public class CambiarPasswordDTO {
    private String correo;
    private String nuevaPassword;
}
