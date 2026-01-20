# Stage 1: Build the application (using Eclipse Temurin)
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Stage 2: Run the application (using Eclipse Temurin JRE)
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]