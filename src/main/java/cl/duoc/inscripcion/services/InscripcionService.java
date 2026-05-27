package cl.duoc.inscripcion.services;

import cl.duoc.inscripcion.models.Curso;
import cl.duoc.inscripcion.models.Inscripcion;
import cl.duoc.inscripcion.repositories.CursoRepository;
import cl.duoc.inscripcion.repositories.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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
     */
    public Map<String, Object> inscribir(String nombreEstudiante, List<Long> cursoIds) {

        List<Curso> cursosSeleccionados = cursoRepository.findAllById(cursoIds);

        if (cursosSeleccionados.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron cursos con los IDs proporcionados.");
        }

        double total = cursosSeleccionados.stream()
                .mapToDouble(Curso::getCosto)
                .sum();

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setNombreEstudiante(nombreEstudiante);
        inscripcion.setCursos(cursosSeleccionados);
        inscripcion.setTotalPagar(total);

        Inscripcion guardada = inscripcionRepository.save(inscripcion);

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

    /**
     * Busca una inscripcion por su ID.
     *
     * @throws IllegalArgumentException si no existe
     */
    public Inscripcion buscarPorId(Long inscripcionId) {
        return inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontro la inscripcion con ID: " + inscripcionId));
    }

    /**
     * Genera el contenido del archivo TXT de resumen para una inscripcion.
     *
     * @param inscripcionId ID de la inscripcion
     * @return bytes del archivo TXT codificado en UTF-8
     */
    public byte[] generarArchivoResumen(Long inscripcionId) {
        Inscripcion inscripcion = buscarPorId(inscripcionId);

        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append("       RESUMEN DE INSCRIPCION #").append(inscripcion.getId()).append("\n");
        sb.append("================================================\n\n");
        sb.append("Estudiante     : ").append(inscripcion.getNombreEstudiante()).append("\n");
        sb.append("Fecha          : ").append(inscripcion.getFechaInscripcion()).append("\n\n");
        sb.append("Cursos inscritos:\n");
        sb.append("------------------------------------------------\n");

        for (Curso c : inscripcion.getCursos()) {
            sb.append(String.format("  - %-30s | Instructor: %-20s | Duracion: %2dh | Costo: $%.2f\n",
                    c.getNombre(), c.getInstructor(), c.getDuracionHoras(), c.getCosto()));
        }

        sb.append("------------------------------------------------\n");
        sb.append(String.format("TOTAL A PAGAR  : $%.2f\n", inscripcion.getTotalPagar()));
        sb.append("================================================\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}