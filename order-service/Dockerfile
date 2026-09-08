FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8083 \
    ORDER_DB_URL=jdbc:postgresql://postgres:5432/petstore_order_db \
    ORDER_DB_USERNAME=postgres \
    ORDER_DB_PASSWORD=postgrespassword \
    JWT_SECRET=dGhpcy1pcy1hLXNlY3VyZS1qd3Qtc2VjcmV0LWZvci1wZXQtc3RvcmUtdjEtZGV2ZWxvcG1lbnQtdGVzdC1rZXk= \
    CATALOG_SERVICE_URL=http://catalog-service:8082 \
    USER_SERVICE_URL=http://user-service:8081

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
