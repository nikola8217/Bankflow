FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY shared ./shared
RUN cd shared && mvn install -DskipTests

COPY transaction-service/.mvn .mvn
COPY transaction-service/mvnw transaction-service/pom.xml ./
RUN ./mvnw dependency:go-offline
COPY transaction-service/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]