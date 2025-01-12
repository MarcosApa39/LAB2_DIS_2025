# Usar una imagen base ligera de OpenJDK
FROM amazoncorretto:17

# Copiar el archivo JAR generado al contenedor
COPY target/spring-skeleton-1.0-SNAPSHOT.jar spring-skeleton-1.0-SNAPSHOT.jar


# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "spring-skeleton-1.0-SNAPSHOT.jar"]
