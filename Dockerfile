FROM eclipse-temurin:17.0.6_10-jre
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
