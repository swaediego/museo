# ETAPA 1: Compilación usando Gradle 8.13 y JDK 21
FROM gradle:8.13-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
# Construimos el .jar
RUN gradle clean processResources bootJar --no-daemon -x test

# ETAPA 2: Imagen ligera para correr la aplicación
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copiamos el .jar generado en la etapa anterior
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar

# Exponemos el puerto del servidor
EXPOSE 8080

# Comando para arrancar la app
ENTRYPOINT ["java", "-jar", "app.jar"]