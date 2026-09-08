FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV PORT=8085 \
    KAFKA_BOOTSTRAP_SERVERS=kafka:9092

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
