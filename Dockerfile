# 1. Build stage (Uses Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Run stage (Uses Java 21)
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
# Copy the built jar from the first stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx1g", "-Xms512m", "-XX:TieredStopAtLevel=1", "-jar", "app.jar"]
