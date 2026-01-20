# STEP 1: BUILD (The "Chef" phase - uses Maven to cook the code)
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

# STEP 2: RUN (The "Server" phase - just serves the cooked meal)
FROM eclipse-temurin:21-jre-jammy
# This line finds the JAR created in STEP 1 and copies it over
COPY --from=build /target/FPL-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]