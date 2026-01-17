# ---------- Build stage ----------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -Dmaven.compiler.debug=false

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    dumb-init \
    python3 \
    python3-pip \
    && pip3 install --no-cache-dir prophet \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /root/.cache

RUN groupadd -g 1001 appuser && useradd -u 1001 -g appuser appuser
USER appuser

# Fix matplotlib warning (Prophet imports it internally)
ENV MPLCONFIGDIR=/tmp/matplotlib

COPY --from=build /app/target/*.jar app.jar
COPY predict_prophet.py /app/scripts/predict_prophet.py

EXPOSE 8080

ENV JAVA_OPTS="\
-Xms96m \
-Xmx160m \
-XX:MetaspaceSize=96m \
-XX:MaxMetaspaceSize=128m \
-XX:MaxDirectMemorySize=16m \
-XX:ReservedCodeCacheSize=32m \
-XX:+UseSerialGC \
-XX:+TieredCompilation \
-XX:TieredStopAtLevel=1 \
-XX:+UseCompressedOops \
-XX:+UseCompressedClassPointers \
-XX:+ExitOnOutOfMemoryError \
-XX:+UseContainerSupport \
-XX:ActiveProcessorCount=1 \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.backgroundpreinitializer.ignore=true \
-Djava.awt.headless=true \
-Dserver.tomcat.threads.max=8 \
-Dserver.tomcat.threads.min-spare=1 \
-Dserver.tomcat.max-connections=30"

ENTRYPOINT ["dumb-init", "--", "sh", "-c", "java $JAVA_OPTS -jar app.jar"]