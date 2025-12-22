# STEP 1: Build the JAR on the server
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# STEP 2: Create the final small image
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# This line grabs the JAR that was just built in STEP 1
COPY --from=build /app/target/Crypto-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]