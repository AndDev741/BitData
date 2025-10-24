
FROM amazoncorretto:23-alpine

RUN apk update && \
    apk upgrade && \
    apk add --no-cache bash

ENV LANG=C.UTF-8

WORKDIR /home/application

COPY target/BitData-*.jar application.jar
EXPOSE 7070

ENTRYPOINT ["bash", "-c", "java -jar application.jar"]

