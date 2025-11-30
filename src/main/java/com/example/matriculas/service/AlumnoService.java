package com.example.matriculas.service;

import com.example.matriculas.model.Alumno;
import com.example.matriculas.model.Usuario;
import com.example.matriculas.repository.AlumnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final UsuarioService usuarioService;

    // ===============================================================
    // 1. Registrar alumno (crea usuario + crea alumno)
    // ===============================================================
    public Alumno registrarAlumno(Alumno alumno, String passwordPlano) {

        // Crear usuario base
        Usuario usuario = alumno.getUsuario();
        usuario.setPassword(passwordPlano);
        usuarioService.crearUsuario(usuario);

        // Guardar alumno (sin código aún)
        Alumno guardado = alumnoRepository.save(alumno);

        // Generar código institucional
        String codigo = "S" + alumno.getAnioIngreso() + String.format("%04d", guardado.getId());
        guardado.setCodigoAlumno(codigo);

        // Guardarlo nuevamente con el código
        return alumnoRepository.save(guardado);
    }


    // ===============================================================
    // 2. Buscar alumno por ID
    // ===============================================================
    public Optional<Alumno> obtenerPorId(Long id) {
        return alumnoRepository.findById(id);
    }

    // ===============================================================
    // Buscar alumno por Codigo Institucional
    // ===============================================================
    public Optional<Alumno> obtenerPorCodigo(String codigoAlumno) {
        return alumnoRepository.findByCodigoAlumno(codigoAlumno);
    }

    // ===============================================================
    // 3. Buscar alumno por email institucional
    // ===============================================================
    public Optional<Alumno> obtenerPorCorreoInstitucional(String correoInstitucional) {
        return alumnoRepository.findByCorreoInstitucional(correoInstitucional);
    }

    // ===============================================================
    // 4. Buscar alumno por email de usuario (login)
    // ===============================================================
    public Optional<Alumno> obtenerPorEmailLogin(String email) {
        return alumnoRepository.findByUsuarioId(
                usuarioService.obtenerPorCorreoInstitucional(email)
                        .map(Usuario::getId)
                        .orElse(null)
        );
    }

    // ===============================================================
    // 5. Listar todos los alumnos
    // ===============================================================
    public List<Alumno> listarTodos() {
        return alumnoRepository.findAll();
    }

    // ===============================================================
    // 6. Actualizar datos del alumno
    // ===============================================================
    public Alumno actualizarAlumno(Alumno alumno) {
        return alumnoRepository.save(alumno);
    }

    // ===============================================================
    // 7. Eliminar alumno
    // ===============================================================
    public void eliminarAlumno(Long id) {
        alumnoRepository.deleteById(id);
    }

    // ===============================================================
    // 8. Verificar existencia por correo institucional del alumno
    // ===============================================================
    public boolean existeAlumnoPorCorreoInstitucional(String correo) {
        return alumnoRepository.findByCorreoInstitucional(correo).isPresent();
    }
}
