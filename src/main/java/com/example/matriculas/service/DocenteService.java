package com.example.matriculas.service;

import com.example.matriculas.model.Docente;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.DocenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final UsuarioService usuarioService;

    // ===============================================================
    // 1. Registrar Docente (crea usuario + crea docente)
    // ===============================================================
    public Docente registrarDocente(Docente docente, String passwordPlano) {

        Usuario usuario = docente.getUsuario();
        usuario.setPassword(passwordPlano);

        // Crea usuario (con contraseña cifrada)
        usuarioService.crearUsuario(usuario);

        // Guarda docente
        return docenteRepository.save(docente);
    }

    // ===============================================================
    // 2. Buscar por ID
    // ===============================================================
    public Optional<Docente> obtenerPorId(Long id) {
        return docenteRepository.findById(id);
    }

    // ===============================================================
    // 3. Buscar por email
    // ===============================================================
    public Optional<Docente> obtenerPorEmail(String email) {
        return docenteRepository.findByUsuario_CorreoInstitucional(email);
    }

    // ===============================================================
    // 4. Buscar por DNI
    // ===============================================================
    public Optional<Docente> obtenerPorDni(String dni) {
        return docenteRepository.findByDni(dni);
    }

    // ===============================================================
    // 5. Listar todos
    // ===============================================================
    public List<Docente> listarTodos() {
        return docenteRepository.findAll();
    }

    // ===============================================================
    // 6. Actualizar docente
    // ===============================================================
    public Docente actualizarDocente(Docente docente) {
        return docenteRepository.save(docente);
    }

    // ===============================================================
    // 7. Eliminar docente
    // ===============================================================
    public void eliminarDocente(Long id) {
        docenteRepository.deleteById(id);
    }

    // ===============================================================
    // 8. Verificar si existe por email
    // ===============================================================
    public boolean existePorEmail(String email) {
        return docenteRepository.findByUsuario_CorreoInstitucional(email).isPresent();
    }

    // ===============================================================
    // 9. Validar disponibilidad horaria (placeholder)
    // ===============================================================
    public boolean docenteDisponible(Long docenteId, Integer diaSemana, String horaInicio, String horaFin) {
        // Más adelante se implementa con el horario real
        return true;
    }
}
