# Sistema de Inscripción de Cursos
**CDY2204 - Desarrollo Cloud Native | Semana 1 - Forma C**

Microservicio desarrollado con **Spring Boot 3** y **Java 21** que implementa un sistema de inscripción para una plataforma educativa virtual. La base de datos utilizada es **H2 embebida** (reemplaza Oracle Cloud en entorno local y de pruebas).

---

## Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `GET` | `/cursos` | Lista todos los cursos disponibles |
| `POST` | `/cursos` | Agrega un nuevo curso |
| `POST` | `/inscripciones` | Inscribe un estudiante en uno o más cursos |

---

## Cómo ejecutar localmente

**Requisitos:** Java 21, Maven 3.x

```bash
# Clonar el repositorio
git clone https://github.com/TU_USUARIO/sistema-inscripcion.git
cd sistema-inscripcion

# Ejecutar
./mvnw spring-boot:run
```

La app quedará disponible en `http://localhost:8080`

**Consola H2** (para ver los datos en el browser):
`http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:inscripciondb`
- Usuario: `sa` | Contraseña: *(vacío)*

---

## Ejemplos de uso (curl)

### 1. Listar cursos disponibles
```bash
curl -X GET http://localhost:8080/cursos
```

**Respuesta:**
```json
[
  { "id": 1, "nombre": "Desarrollo Cloud Native", "instructor": "Prof. Andres Morales", "duracionHoras": 60, "costo": 250000.0 },
  { "id": 2, "nombre": "Spring Boot Avanzado",    "instructor": "Prof. Carla Reyes",   "duracionHoras": 40, "costo": 180000.0 },
  { "id": 3, "nombre": "Docker y Kubernetes",     "instructor": "Prof. Luis Vega",     "duracionHoras": 30, "costo": 150000.0 }
]
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

**Respuesta:**
```json
{
  "inscripcionId": 1,
  "estudiante": "Maria Gonzalez",
  "fechaInscripcion": "2026-05-21T10:30:00",
  "cursosInscritos": [
    { "id": 1, "nombre": "Desarrollo Cloud Native", "costo": 250000.0 },
    { "id": 2, "nombre": "Spring Boot Avanzado",    "costo": 180000.0 }
  ],
  "totalAPagar": 430000.0
}
```

---

## Configuración de Secrets en GitHub

Para que el CI/CD funcione, configurar estos **Secrets** en GitHub:
`Settings > Secrets and variables > Actions > New repository secret`

| Secret | Descripción |
|--------|-------------|
| `DOCKERHUB_USERNAME` | Tu usuario de Docker Hub |
| `DOCKERHUB_TOKEN` | Access token de Docker Hub |
| `AWS_ACCESS_KEY_ID` | Del lab de AWS |
| `AWS_SECRET_ACCESS_KEY` | Del lab de AWS |
| `AWS_SESSION_TOKEN` | Del lab de AWS |
| `EC2_SSH_KEY` | Contenido del archivo `.pem` de tu instancia EC2 |
| `EC2_HOST` | IP pública de la instancia EC2 |
| `USER_SERVER` | Usuario SSH de EC2 (generalmente `ec2-user` o `ubuntu`) |

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
- H2 Database (embebida)
- Lombok
- Docker
- GitHub Actions
- AWS EC2
