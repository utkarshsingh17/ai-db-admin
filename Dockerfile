# Multi-stage build: compile with the full JDK, run on a slim JRE.
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN useradd --system --create-home appuser
COPY --from=build /app/target/app.jar app.jar
USER appuser

# Render sets $PORT; server.port in application.yaml already falls back to it.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
