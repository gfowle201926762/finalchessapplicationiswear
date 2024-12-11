# Use a base image with JDK
FROM openjdk:17-jdk-slim

# Copy the JAR file from the host to the container
COPY target/application-0.0.1-SNAPSHOT.jar app.jar

# Expose the application port (adjust if your app runs on a different port)
EXPOSE 8080
EXPOSE 3306

# Run the application
CMD ["java", "-Djava.library.path=src/main/libs", "-jar", "app.jar"]
#CMD ["tail", "-f", "/dev/null"]