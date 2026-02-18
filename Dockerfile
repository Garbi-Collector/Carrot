# Etapa 1: Construcción
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Etapa 2: Ejecución
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crear el directorio para SQLite
RUN mkdir -p /app/data

# Copiar el JAR
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto correcto
EXPOSE 8088

# Punto de montaje para persistir la DB
VOLUME /app/data

ENTRYPOINT ["java", "-jar", "app.jar"]