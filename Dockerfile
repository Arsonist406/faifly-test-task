FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./
RUN ./mvnw -B -q dependency:go-offline
COPY src src
RUN ./mvnw -B -q clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=UTC
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=10 \
    CMD ["sh", "-c", "curl -fsS http://localhost:8080/actuator/health || exit 1"]
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
