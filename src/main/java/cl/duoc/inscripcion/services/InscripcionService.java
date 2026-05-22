package cl.duoc.inscripcion.services;

import cl.duoc.inscripcion.models.Curso;
import cl.duoc.inscripcion.models.Inscripcion;
import cl.duoc.inscripcion.repositories.CursoRepository;
import cl.duoc.inscripcion.repositories.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private CursoRepository cursoRepository;

    /**
     * Inscribe un estudiante en uno o mas cursos.
     * Calcula el total a pagar y persiste la inscripcion.
     *
     * @param nombreEstudiante nombre del estudiante
     * @param cursoIds         lista de IDs de cursos a inscribir
     * @return mapa con el resumen de inscripcion
     */
    public Map<String, Object> inscribir(String nombreEstudiante, List<Long> cursoIds) {

        // Buscar los cursos por sus IDs
        List<Curso> cursosSeleccionados = cursoRepository.findAllById(cursoIds);

        if (cursosSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron cursos con los IDs proporcionados.");
        }

        // Calcular el total a pagar
        double total = cursosSeleccionados.stream()
                .mapToDouble(Curso::getCosto)
                .sum();

        // Crear y guardar la inscripcion
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setNombreEstudiante(nombreEstudiante);
        inscripcion.setCursos(cursosSeleccionados);
        inscripcion.setTotalPagar(total);

        Inscripcion guardada = inscripcionRepository.save(inscripcion);

        // Armar el resumen de respuesta
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("inscripcionId", guardada.getId());
        resumen.put("estudiante", guardada.getNombreEstudiante());
        resumen.put("fechaInscripcion", guardada.getFechaInscripcion().toString());

        List<Map<String, Object>> cursosResumen = cursosSeleccionados.stream()
                .map(c -> {
                    Map<String, Object> detalle = new LinkedHashMap<>();
                    detalle.put("id", c.getId());
                    detalle.put("nombre", c.getNombre());
                    detalle.put("costo", c.getCosto());
                    return detalle;
                })
                .toList();

        resumen.put("cursosInscritos", cursosResumen);
        resumen.put("totalAPagar", total);

        return resumen;
    }
}
