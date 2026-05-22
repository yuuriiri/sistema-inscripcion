package cl.duoc.inscripcion;

import cl.duoc.inscripcion.models.Curso;
import cl.duoc.inscripcion.repositories.CursoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    /**
     * Carga cursos de ejemplo la primera vez que no hay datos en la BD.
     * Con Oracle los datos persisten entre reinicios, por eso verificamos count().
     */
    @Bean
    public CommandLineRunner cargarDatosPrueba(CursoRepository cursoRepository) {
        return args -> {
            if (cursoRepository.count() == 0) {
                cursoRepository.save(new Curso(null, "Desarrollo Cloud Native", "Prof. Andres Morales", 60, 250000.0));
                cursoRepository.save(new Curso(null, "Spring Boot Avanzado",    "Prof. Carla Reyes",   40, 180000.0));
                cursoRepository.save(new Curso(null, "Docker y Kubernetes",     "Prof. Luis Vega",     30, 150000.0));
                System.out.println(">>> Datos de prueba cargados: 3 cursos disponibles");
            } else {
                System.out.println(">>> BD Oracle ya contiene datos, no se cargan datos de prueba.");
            }
        };
    }
}
