# Use Liberica Runtime Container for minimized size (Alpine musl based)
# This image is significantly smaller (~40MB compressed) than standard Temurin/OpenJDK Alpine images
FROM bellsoft/liberica-runtime-container:jre-17-musl

# Set working directory
WORKDIR /app

# Copy the built jar file
COPY target/echo-server-1.0-SNAPSHOT.jar app.jar

# Expose the port the app runs on
EXPOSE 8086

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
