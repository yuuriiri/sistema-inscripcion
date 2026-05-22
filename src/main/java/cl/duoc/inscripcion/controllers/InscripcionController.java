package cl.duoc.inscripcion.controllers;

import cl.duoc.inscripcion.services.InscripcionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    /**
     * POST /inscripciones
     * Inscribe a un estudiante en uno o mas cursos.
     * Devuelve un resumen con cursos seleccionados, costo de cada uno y total a pagar.
     *
     * Body esperado:
     * {
     *   "nombreEstudiante": "Maria Gonzalez",
     *   "cursoIds": [1, 2]
     * }
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> inscribir(@RequestBody Map<String, Object> body) {

        System.out.println("Se recibio llamada a POST /inscripciones");

        String nombreEstudiante = (String) body.get("nombreEstudiante");

        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("cursoIds");
        List<Long> cursoIds = idsRaw.stream().map(Integer::longValue).toList();

        if (nombreEstudiante == null || nombreEstudiante.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'nombreEstudiante' es obligatorio."));
        }
        if (cursoIds == null || cursoIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe proporcionar al menos un curso en 'cursoIds'."));
        }

        try {
            Map<String, Object> resumen = inscripcionService.inscribir(nombreEstudiante, cursoIds);
            return ResponseEntity.status(HttpStatus.CREATED).body(resumen);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
