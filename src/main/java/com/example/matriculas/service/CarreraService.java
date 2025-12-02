package com.example.matriculas.service;

import com.example.matriculas.model.Carrera;
import com.example.matriculas.repository.CarreraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;

    public List<Carrera> listarTodas() {
        return carreraRepository.findAll();
    }

    public Optional<Carrera> obtenerPorId(Long id) {
        return carreraRepository.findById(id);
    }
}
