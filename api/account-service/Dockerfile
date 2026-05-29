FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

RUN apk add --no-cache maven

COPY shared ./shared
RUN cd shared && mvn install -DskipTests

COPY account-service/.mvn .mvn
COPY account-service/mvnw account-service/pom.xml ./
RUN ./mvnw dependency:go-offline
COPY account-service/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]