FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Download dependencies first (caches this layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build the app
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.compiler.debug=false

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
-Xmx256m \
-Xms256m \
-XX:MaxMetaspaceSize=80m \
-XX:MetaspaceSize=80m \
-XX:MaxDirectMemorySize=32m \
-XX:ReservedCodeCacheSize=32m \
-XX:+UseSerialGC \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:+UseStringDeduplication \
-XX:+OptimizeStringConcat \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+ExitOnOutOfMemoryError \
-noverify \
-XX:+UnlockExperimentalVMOptions \
-XX:+UseContainerSupport \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.backgroundpreinitializer.ignore=true \
-Djava.awt.headless=true"

ENTRYPOINT ["dumb-init", "--", "sh", "-c", "java $JAVA_OPTS -jar app.jar"]