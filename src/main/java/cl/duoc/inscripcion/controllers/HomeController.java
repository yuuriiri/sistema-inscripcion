package cl.duoc.inscripcion.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.LinkedHashMap;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("sistema", "Sistema de Inscripcion de Cursos");
        info.put("version", "1.0.0");
        info.put("endpoints", Map.of(
            "GET  /cursos",        "Listar todos los cursos disponibles",
            "POST /cursos",        "Agregar un nuevo curso",
            "POST /inscripciones", "Inscribir estudiante en cursos"
        ));
        return ResponseEntity.ok(info);
    }
}
