# ---- build stage ----
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# ---- runtime stage ----
FROM openjdk:11-jre-slim
WORKDIR /app

# Copy the fat JAR produced by Spring Boot's repackage goal
COPY --from=build /app/target/inventory-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
