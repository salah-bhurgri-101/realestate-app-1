# the first stage of our build will extract the layers
FROM eclipse-temurin:21.0.5_11-jre as builder
WORKDIR application
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
#RUN java -Djarmode=layertools -jar application.jar extract ### Since Boot 3.3, the layertools jarmode was deprecated in favor of a new tools jarmode, and by the 4.x line layertools
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted


# the second stage of our build will copy the extracted layers
FROM eclipse-temurin:21.0.5_11-jre
WORKDIR application

COPY --from=builder application/extracted/dependencies/ ./
COPY --from=builder application/extracted/spring-boot-loader/ ./
COPY --from=builder application/extracted/snapshot-dependencies/ ./
COPY --from=builder application/extracted/application/ ./

ENTRYPOINT ["java", "-jar", "application.jar"]








# # ---- Build stage ----
# FROM maven:3.9-eclipse-temurin-17 AS build
# WORKDIR /app
# COPY pom.xml .
# RUN mvn -B dependency:go-offline
# COPY src ./src
# RUN mvn -B clean package -DskipTests

# # ---- Run stage ----
# FROM eclipse-temurin:17-jre-alpine
# WORKDIR /app
# COPY --from=build /app/target/user-service.jar app.jar
# EXPOSE 8081
# ENTRYPOINT ["java", "-jar", "app.jar"]
