FROM gcr.io/distroless/java21

WORKDIR /app

COPY out/artifacts/thot_jar/thot.jar /app/app.jar

EXPOSE 12903

CMD ["app.jar"]
