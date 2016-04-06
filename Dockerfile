FROM alpine:latest
RUN apk add --no-cache openjdk8
RUN install -d /srv/openregister
COPY build/libs/loader.jar /srv/openregister/loader.jar
