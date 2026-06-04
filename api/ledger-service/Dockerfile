FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY shared ./shared
RUN cd shared && mvn install -DskipTests

COPY ledger-service/.mvn .mvn
COPY ledger-service/mvnw ledger-service/pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ledger-service/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]