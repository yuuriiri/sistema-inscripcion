# Sistema de Inscripción de Cursos
**CDY2204 - Desarrollo Cloud Native | Semana 1 - Forma C**

Microservicio desarrollado con **Spring Boot 3** y **Java 21** que implementa un sistema de inscripción para una plataforma educativa virtual. La base de datos utilizada es **Oracle Database** local.

---

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/cursos` | Lista todos los cursos disponibles |
| `POST` | `/cursos` | Agrega un nuevo curso |
| `POST` | `/inscripciones` | Inscribe un estudiante en uno o más cursos |

---

## Cómo ejecutar localmente

**Requisitos:** Java 21, Maven 3.x, Oracle Database local

```bash
# Clonar el repositorio
git clone https://github.com/yuuriiri/sistema-inscripcion.git
cd sistema-inscripcion

# Ejecutar
.\mvnw.cmd spring-boot:run
```

La app quedará disponible en `http://localhost:8080`

---

## Ejemplos de uso (curl)

### 1. Listar cursos disponibles
```bash
curl -X GET http://localhost:8080/cursos
```

### 2. Agregar un nuevo curso
```bash
curl -X POST http://localhost:8080/cursos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Microservicios con AWS",
    "instructor": "Prof. Pedro Soto",
    "duracionHoras": 50,
    "costo": 200000
  }'
```

### 3. Inscribir un estudiante en cursos
```bash
curl -X POST http://localhost:8080/inscripciones \
  -H "Content-Type: application/json" \
  -d '{
    "nombreEstudiante": "Maria Gonzalez",
    "cursoIds": [1, 2]
  }'
```

**Respuesta esperada:**
```json
{
  "inscripcionId": 1,
  "estudiante": "Maria Gonzalez",
  "fechaInscripcion": "2026-05-22T10:30:00",
  "cursosInscritos": [
    { "id": 1, "nombre": "Desarrollo Cloud Native", "costo": 250000.0 },
    { "id": 2, "nombre": "Spring Boot Avanzado", "costo": 180000.0 }
  ],
  "totalAPagar": 430000.0
}
```

---

## Flujo CI/CD

```
git push → main
      ↓
GitHub Actions
      ↓
docker build → Docker Hub
      ↓
SSH a EC2 → docker pull → docker run
```

---

## Tecnologías

- Java 21
- Spring Boot 3.4.1
- Spring Data JPA
- Oracle Database
- Docker
- GitHub Actions
- AWS EC2

## Deploy

Desplegado automáticamente en EC2 via GitHub Actions.