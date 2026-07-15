# =============================================================================
# Stage 1 – build
# =============================================================================
FROM registry.access.redhat.com/ubi8/openjdk-11:latest AS build
USER root
WORKDIR /app
RUN chown -R 185:0 /app
USER 185
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
FROM registry.access.redhat.com/ubi8/openjdk-11-runtime:latest

# ubi8/openjdk-11-runtime defaults to /deployments and USER 185 —
# both satisfy OpenShift's arbitrary-UID requirement out of the box.
COPY --from=build /app/target/inventory-api-0.0.1-SNAPSHOT.jar /deployments/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/deployments/app.jar"]
