FROM eclipse-temurin:21-jre
LABEL authors="notkulonme"
WORKDIR /app
COPY target/freshPotato-dev.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]