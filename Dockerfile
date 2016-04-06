FROM alpine:latest
RUN apk add --no-cache openjdk8
RUN install -d /srv/openregister
COPY build/libs/loader.jar /srv/openregister/loader.jar
CMD sh -c java -jar /srv/openregister/loader.jar --minturl=$MINT_LOAD_URL --datasource=$MINT_DATASOURCE --type=$MINT_TYPE
