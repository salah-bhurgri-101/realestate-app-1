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









## First stage: extract Spring Boot layers using DHI JRE
#FROM dhi.io/eclipse-temurin:21-jre-dev as builder
#WORKDIR application
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} application.jar
#RUN java -Djarmode=layertools -jar application.jar extract

## Second stage: minimal runtime with DHI Alpine JRE, non-root user, and security hardening#FROM dhi.io/eclipse-temurin:21-jre-alpine
#WORKDIR application
#
## Create non-root user for security
#RUN addgroup -g 1000 appuser && adduser -D -u 1000 -G appuser appuser
#
## Copy extracted layers from builder
#COPY --from=builder --chown=appuser:appuser application/dependencies/ ./
#COPY --from=builder --chown=appuser:appuser application/spring-boot-loader/ ./
#COPY --from=builder --chown=appuser:appuser application/snapshot-dependencies/ ./
#COPY --from=builder --chown=appuser:appuser application/application/ ./
#
## Switch to non-root user
##USER appuser
#
## Use the image's built-in non-root user (no shell available to create one)
#USER nonroot
#
## Health check
#HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
#    CMD java -version || exit 1
#
## Application entry point
#ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
