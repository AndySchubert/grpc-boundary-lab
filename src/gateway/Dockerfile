FROM gradle:8-jdk21 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew :gateway:installDist

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/gateway/build/install/gateway /app/
EXPOSE 50052
ENTRYPOINT ["/app/bin/gateway"]
