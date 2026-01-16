# build App
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Download dependencies first (caches this layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the app
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.compiler.debug=false

# Light weight image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install dumb-init (prevents zombie processes)
RUN apk add --no-cache dumb-init

# Security: run as non-root user
RUN addgroup -g 1001 -S appuser && adduser -u 1001 -S appuser -G appuser
USER appuser

# Copy the built jar file
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="\
-Xmx320m \
-Xms180m \
-XX:MaxMetaspaceSize=100m \
-XX:MetaspaceSize=80m \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=100 \
-XX:+UseStringDeduplication \
-XX:+ExitOnOutOfMemoryError \
-Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["dumb-init", "--", "sh", "-c", "java $JAVA_OPTS -jar app.jar"]