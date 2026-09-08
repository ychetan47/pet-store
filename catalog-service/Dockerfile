FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8082 \
    CATALOG_DB_URL=jdbc:postgresql://postgres:5432/petstore_catalog_db \
    CATALOG_DB_USERNAME=postgres \
    CATALOG_DB_PASSWORD=postgrespassword

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
