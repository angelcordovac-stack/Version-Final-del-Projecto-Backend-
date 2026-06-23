# ============================================================
#  ETAPA 1: BUILD — compila el proyecto y genera el .jar
# ============================================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# 1) Copiamos solo el pom primero para aprovechar la cache de Docker:
#    si el pom no cambia, las dependencias no se vuelven a descargar.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# 2) Copiamos el codigo fuente y empaquetamos.
#    -DskipTests: no corremos los tests aqui (no hay BD ni .env en el build).
COPY src ./src
RUN mvn -B clean package -DskipTests

# ============================================================
#  ETAPA 2: RUNTIME — imagen ligera solo con el JRE y el .jar
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Usuario sin privilegios (buena practica de seguridad)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copiamos el jar generado en la etapa de build y lo renombramos a app.jar
COPY --from=build /app/target/*.jar app.jar

# Render inyecta la variable PORT; la app ya la lee en application.properties.
EXPOSE 8080

# -XX:MaxRAMPercentage limita la memoria del heap al % de la RAM del contenedor
# (importante en planes con poca memoria como el Free de Render).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
