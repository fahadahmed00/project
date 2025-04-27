FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the pom.xml file
COPY pom.xml .

# Download all dependencies
RUN mvn dependency:go-offline -B

# Copy the project source
COPY src ./src

# Package the application
RUN mvn package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]