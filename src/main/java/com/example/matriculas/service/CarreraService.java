package com.example.matriculas.service;

import com.example.matriculas.model.Carrera;
import com.example.matriculas.repository.CarreraRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;

    @Transactional(readOnly = true)
    public List<Carrera> listar() {
        return carreraRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Carrera obtener(Long id) {
        return carreraRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Carrera no encontrada"));
    }

    @Transactional
    public Carrera crear(Carrera carrera) {
        carrera.setId(null);
        return carreraRepository.save(carrera);
    }

    @Transactional
    public Carrera actualizar(Long id, Carrera carrera) {
        Carrera existente = obtener(id);
        carrera.setId(existente.getId());
        return carreraRepository.save(carrera);
    }

    @Transactional
    public void eliminar(Long id) {
        carreraRepository.deleteById(id);
    }
}
