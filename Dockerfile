# Usar una imagen base con Java 17
FROM openjdk:17-slim

# Copiar el archivo jar del proyecto al directorio /app en el contenedor
COPY target/ms-spring-security-jwt-1.0.0.jar /app/ms-spring-security-jwt.jar

ENTRYPOINT ["java", "-jar", "/app/ms-spring-security-jwt.jar"]

