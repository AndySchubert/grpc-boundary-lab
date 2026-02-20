FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :backend:installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/backend/build/install/backend /app/
EXPOSE 50051
ENTRYPOINT ["/app/bin/backend"]
