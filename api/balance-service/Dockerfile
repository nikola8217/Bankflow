FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY shared ./shared
RUN cd shared && mvn install -DskipTests

COPY balance-service/.mvn .mvn
COPY balance-service/mvnw balance-service/pom.xml ./
RUN ./mvnw dependency:go-offline
COPY balance-service/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]