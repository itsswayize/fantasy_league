# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
# We use 'mvn' directly instead of the 'mvnw' wrapper
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]