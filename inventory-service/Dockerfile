FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8084 \
    INVENTORY_DB_URL=jdbc:postgresql://postgres:5432/petstore_inventory_db \
    INVENTORY_DB_USERNAME=postgres \
    INVENTORY_DB_PASSWORD=postgrespassword \
    KAFKA_BOOTSTRAP_SERVERS=kafka:9092

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
