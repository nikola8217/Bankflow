# syntax=docker/dockerfile:1

# One Dockerfile for every service:
#   docker build --build-arg SERVICE=transaction-service -t bankflow/transaction-service ./api

# ---------- build ----------
FROM eclipse-temurin:21-jdk-alpine AS build
ARG SERVICE
RUN test -n "$SERVICE" || (echo "Build arg SERVICE is required, e.g. --build-arg SERVICE=auth-service" && false)

WORKDIR /workspace

# Build files first: these layers stay cached until a pom or the wrapper changes.
COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY shared/pom.xml shared/
COPY auth-service/pom.xml auth-service/
COPY account-service/pom.xml account-service/
COPY transaction-service/pom.xml transaction-service/
COPY ledger-service/pom.xml ledger-service/

# Only the sources this service needs.
COPY shared/src shared/src
COPY ${SERVICE}/src ${SERVICE}/src

# -pl/-am builds the service plus the modules it depends on (shared).
# The cache mount keeps ~/.m2 between builds, so dependencies are not downloaded every time.
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && \
    ./mvnw -B -q package -pl ${SERVICE} -am -DskipTests

# Split the fat jar into layers (dependencies change rarely, application code often).
RUN cp ${SERVICE}/target/${SERVICE}-*.jar app.jar && \
    java -Djarmode=tools -jar app.jar extract --layers --launcher --destination /extracted

# ---------- runtime ----------
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app

COPY --from=build /extracted/dependencies/ ./
COPY --from=build /extracted/spring-boot-loader/ ./
COPY --from=build /extracted/snapshot-dependencies/ ./
COPY --from=build /extracted/application/ ./

USER app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]