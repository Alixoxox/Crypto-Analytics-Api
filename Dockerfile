# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Download dependencies first (caches this layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the app
COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.compiler.debug=false

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install Prophet with minimal dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    dumb-init \
    python3 \
    python3-pip \
    && pip3 install --no-cache-dir prophet \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /root/.cache

# Security: run as non-root user
RUN groupadd -g 1001 appuser && useradd -u 1001 -g appuser appuser
USER appuser

# Copy the built jar file
COPY --from=build /app/target/*.jar app.jar

# Copy the Python script to /app/scripts
COPY predict_prophet.py /app/scripts/predict_prophet.py

# Expose port
EXPOSE 8080

# EXTREME memory optimization - save RAM for compute workloads
ENV JAVA_OPTS="\
-Xmx120m \
-Xms80m \
-XX:MaxMetaspaceSize=48m \
-XX:MetaspaceSize=48m \
-XX:MaxDirectMemorySize=8m \
-XX:ReservedCodeCacheSize=16m \
-XX:+UseSerialGC \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:MinHeapFreeRatio=10 \
-XX:MaxHeapFreeRatio=30 \
-XX:GCTimeRatio=2 \
-XX:+UseStringDeduplication \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+ExitOnOutOfMemoryError \
-XX:+UseContainerSupport \
-XX:ActiveProcessorCount=1 \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.backgroundpreinitializer.ignore=true \
-Djava.awt.headless=true \
-Dserver.tomcat.threads.max=10 \
-Dserver.tomcat.threads.min-spare=1 \
-Dserver.tomcat.max-connections=50"

# Entrypoint
ENTRYPOINT ["dumb-init", "--", "sh", "-c", "java $JAVA_OPTS -jar app.jar"]