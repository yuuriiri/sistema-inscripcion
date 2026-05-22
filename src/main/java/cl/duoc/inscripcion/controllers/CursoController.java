package cl.duoc.inscripcion.controllers;

import cl.duoc.inscripcion.models.Curso;
import cl.duoc.inscripcion.services.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    /**
     * GET /cursos
     * Retorna la lista de todos los cursos disponibles.
     * Muestra nombre, instructor, duracion y costo de cada curso.
     */
    @GetMapping
    public ResponseEntity<List<Curso>> listarCursos() {
        System.out.println("Se recibio llamada a GET /cursos");
        List<Curso> cursos = cursoService.listarCursos();
        return ResponseEntity.ok(cursos);
    }

    /**
     * POST /cursos
     * Agrega un nuevo curso a la oferta educativa.
     *
     * Body esperado:
     * {
     *   "nombre": "Java Avanzado",
     *   "instructor": "Juan Perez",
     *   "duracionHoras": 40,
     *   "costo": 150000
     * }
     */
    @PostMapping
    public ResponseEntity<Curso> agregarCurso(@RequestBody Curso curso) {
        System.out.println("Se recibio llamada a POST /cursos - nuevo curso: " + curso.getNombre());
        Curso nuevo = cursoService.agregarCurso(curso);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }
}
