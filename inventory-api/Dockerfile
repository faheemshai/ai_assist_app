# =============================================================================
# Stage 1 – build
# =============================================================================
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# =============================================================================
# Stage 2 – runtime (OpenShift-compatible)
#
# OpenShift assigns a random UID from the project's namespace range at runtime,
# so we CANNOT use a fixed "USER <uid>" instruction.  Instead we:
#   1. Set the working directory to /app
#   2. Copy the JAR
#   3. chgrp -R 0 /app  →  hand group ownership to root (GID 0)
#   4. chmod -R g=u /app →  give the group the same permissions as the owner
#
# This satisfies the OpenShift arbitrary-UID requirement: whatever random UID
# the platform injects will inherit the root-group permissions and be able to
# read/write /app without needing elevated privileges.
# =============================================================================
FROM openjdk:11-jre-slim
WORKDIR /app

COPY --from=build /app/target/inventory-api-0.0.1-SNAPSHOT.jar app.jar

RUN chgrp -R 0 /app && chmod -R g=u /app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
