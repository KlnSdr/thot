FROM openjdk:21-jdk-slim

WORKDIR /app

COPY out/artifacts/thot_jar/thot.jar /app/app.jar

EXPOSE 12903

CMD ["java", "-jar", "/app/app.jar"]
