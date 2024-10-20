# Usar una imagen base con Java 17
FROM openjdk:17-slim

# Directorio donde se colocará la aplicación en el contenedor
WORKDIR /app

# Copiar el archivo jar del proyecto al directorio /app en el contenedor
COPY target/ms-spring-security-jwt-1.0.0.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]

# Exponer el puerto que usa la aplicación
EXPOSE 3000

