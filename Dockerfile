FROM eclipse-temurin:21-jdk AS buildstage

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY src /app/src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk

# Copiar el JAR
COPY --from=buildstage /app/target/inscripcion-1.0.0.jar /app/app.jar

# Copiar el Wallet de Oracle Cloud dentro de la imagen
COPY wallet/ /app/wallet/

EXPOSE 8080

# Activar perfil docker que apunta al wallet en /app/wallet
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "/app/app.jar"]
