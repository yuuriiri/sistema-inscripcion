package cl.duoc.inscripcion.controllers;

import cl.duoc.inscripcion.services.InscripcionService;
import cl.duoc.inscripcion.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Autowired
    private S3Service s3Service;

    /**
     * POST /inscripciones
     * Inscribe a un estudiante en uno o mas cursos.
     * Devuelve un resumen con cursos seleccionados, costo de cada uno y total a pagar.
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

    /**
     * GET /inscripciones/{id}/resumen
     * Genera el archivo TXT de resumen y lo retorna para descargar en el computador.
     */
    @GetMapping("/{id}/resumen")
    public ResponseEntity<byte[]> descargarResumen(@PathVariable Long id) {
        try {
            byte[] contenido = inscripcionService.generarArchivoResumen(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "resumen-" + id + ".txt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(contenido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * POST /inscripciones/{id}/s3
     * Genera el resumen de la inscripcion y lo sube al bucket S3.
     * El archivo se guarda en la carpeta: {id}/resumen-{id}.txt
     */
    @PostMapping("/{id}/s3")
    public ResponseEntity<Map<String, Object>> subirResumenS3(@PathVariable Long id) {
        try {
            byte[] contenido = inscripcionService.generarArchivoResumen(id);
            String key = s3Service.subirArchivo(id, contenido, "text/plain");

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "mensaje", "Resumen subido exitosamente a S3.",
                    "bucket",  s3Service.getBucket(),
                    "key",     key
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir a S3: " + e.getMessage()));
        }
    }
}