# =========================
# STAGE 1 : BUILD (compilation de l’application)
# =========================

FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B -q
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B -q


# =========================
# STAGE 2 : EXTRACT (Spring Boot layered JAR)
# =========================

FROM eclipse-temurin:21-jdk-alpine AS extractor
WORKDIR /extracted
COPY --from=builder /build/target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract


# =========================
# STAGE 3 : RUNTIME (image finale production)
# =========================

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root — bonne pratique de sécurité
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=extractor /extracted/dependencies/ ./
COPY --from=extractor /extracted/spring-boot-loader/ ./
COPY --from=extractor /extracted/snapshot-dependencies/ ./
COPY --from=extractor /extracted/application/ ./

EXPOSE 8080

# Health check sur l'actuator Spring Boot
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]