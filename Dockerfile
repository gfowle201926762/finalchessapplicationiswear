# Use a base image with JDK
FROM openjdk:17-jdk-slim

# 559711505 size with openjdk:17-oracle
# 460360275 size with openjdk:17-jdk-slim

# Copy the JAR file from the host to the container
COPY target/application-0.0.1-SNAPSHOT.jar app.jar
#RUN #mkdir libs
COPY src/main/libs/libchess_amd.dylib libs/libchess.so

COPY src/main/resources/static/images/ /images/

# Expose the application port (adjust if your app runs on a different port)
EXPOSE 443
EXPOSE 8080
EXPOSE 3306

# Run the application
CMD ["java", "-Djava.library.path=libs", "-jar", "app.jar"]
#CMD ["tail", "-f", "/dev/null"]