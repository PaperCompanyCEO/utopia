# syntax = docker/dockerfile:1.2
FROM clojure:openjdk-17 AS build

WORKDIR /
COPY . /

RUN clj -Sforce -T:build all

FROM azul/zulu-openjdk-alpine:17

COPY --from=build /target/utopia-standalone.jar /utopia/utopia-standalone.jar

EXPOSE $PORT

ENTRYPOINT exec java $JAVA_OPTS -jar /utopia/utopia-standalone.jar
