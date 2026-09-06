# Build Stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

COPY --from=build /app/target/pet-store-backend-1.0.0.jar app.jar

ENV PORT=8080 \
    DATABASE_URL=jdbc:postgresql://postgres:5432/petstoredb \
    DATABASE_USERNAME=postgres \
    DATABASE_PASSWORD=postgrespassword \
    JWT_SECRET=dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWZvci1wZXQtc3RvcmUtdjEtZGV2ZWxvcG1lbnQtdGVzdC1rZXk= \
    GCS_BUCKET=pet-store-bucket

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
