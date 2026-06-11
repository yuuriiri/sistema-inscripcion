package cl.duoc.inscripcion.controllers;

import cl.duoc.inscripcion.services.InscripcionService;
import cl.duoc.inscripcion.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.InputStream;
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

            return ResponseEntity.ok().headers(headers).body(contenido);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * POST /inscripciones/{id}/s3
     * Sube el resumen al bucket S3 en la carpeta {id}/resumen-{id}.txt
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

    /**
     * PUT /inscripciones/{id}/s3
     * Reemplaza el archivo existente en S3 con el resumen actualizado.
     */
    @PutMapping("/{id}/s3")
    public ResponseEntity<Map<String, Object>> actualizarResumenS3(@PathVariable Long id) {
        try {
            byte[] contenido = inscripcionService.generarArchivoResumen(id);
            String key = s3Service.actualizarArchivo(id, contenido, "text/plain");

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Resumen actualizado exitosamente en S3.",
                    "bucket",  s3Service.getBucket(),
                    "key",     key
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar en S3: " + e.getMessage()));
        }
    }

    /**
     * DELETE /inscripciones/{id}/s3
     * Elimina el archivo de resumen desde el bucket S3.
     */
    @DeleteMapping("/{id}/s3")
    public ResponseEntity<Map<String, Object>> eliminarResumenS3(@PathVariable Long id) {
        try {
            s3Service.eliminarArchivo(id);

            return ResponseEntity.ok(Map.of(
                    "mensaje", "Resumen eliminado exitosamente de S3.",
                    "bucket",  s3Service.getBucket(),
                    "key",     id + "/resumen-" + id + ".txt"
            ));
        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No existe archivo en S3 para la inscripcion " + id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar de S3: " + e.getMessage()));
        }
    }

    /**
     * GET /inscripciones/{id}/s3
     * Descarga el archivo de resumen directamente desde el bucket S3.
     */
    @GetMapping("/{id}/s3")
    public ResponseEntity<byte[]> descargarResumenS3(@PathVariable Long id) {
        try {
            InputStream stream = s3Service.descargarArchivo(id);
            byte[] contenido = stream.readAllBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "resumen-" + id + ".txt");

            return ResponseEntity.ok().headers(headers).body(contenido);
        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /inscripciones/archivos/s3
     * Lista todos los archivos almacenados en el bucket S3.
     */
    @GetMapping("/archivos/s3")
    public ResponseEntity<List<String>> listarArchivosS3() {
        return ResponseEntity.ok(s3Service.listarArchivos());
    }
}