# Usar una imagen base ligera de OpenJDK
FROM amazoncorretto:17
# Copiar el archivo JAR generado al contenedor
COPY target/lab2-0.0.1-SNAPSHOT.jar lab2-0.0.1-SNAPSHOT.jar
COPY src/main/resources src/main/resources

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "lab2-0.0.1-SNAPSHOT.jar"]
