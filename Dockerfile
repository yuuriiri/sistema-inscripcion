FROM eclipse-temurin:21-jdk AS buildstage

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src /app/src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

COPY --from=buildstage /app/target/inscripcion-1.0.0.jar /app/app.jar

EXPOSE 8080

# El perfil "docker" activa H2 en vez de Oracle
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/app/app.jar"]
