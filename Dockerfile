FROM docker.klnsdr.com/nyx-cli:1.1 as builder

WORKDIR /app

COPY . .

RUN nyx build

FROM gcr.io/distroless/java21

WORKDIR /app

COPY /app/build/thot-2.0.jar /app/app.jar

EXPOSE 12903

CMD ["app.jar"]
