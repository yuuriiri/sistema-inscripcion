package cl.duoc.inscripcion.services;

import cl.duoc.inscripcion.models.Curso;
import cl.duoc.inscripcion.repositories.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    // Inyectar el repositorio de cursos para acceder a la base de datos
    @Autowired
    private CursoRepository cursoRepository;

    // Listar todos los cursos disponibles
    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    // Agregar un nuevo curso
    public Curso agregarCurso(Curso curso) {
        return cursoRepository.save(curso);
    }

    // Buscar curso por ID (usado al inscribir)
    public Optional<Curso> buscarPorId(Long id) {
        return cursoRepository.findById(id);
    }
}
