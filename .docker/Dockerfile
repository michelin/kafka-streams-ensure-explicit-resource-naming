FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.authors="Michelin"

COPY target/kafka-streams-ensure-explicit-resource-naming.jar /app/kafka-streams-ensure-explicit-resource-naming.jar

RUN apk update \
    && echo -e "#!/bin/sh\n\njava -jar /app/kafka-streams-ensure-explicit-resource-naming.jar \"\$@\"" > /usr/bin/kafka-streams-ensure-explicit-resource-naming \
    && chmod +x /usr/bin/kafka-streams-ensure-explicit-resource-naming \
    && apk upgrade \
    && rm -rf /var/cache/apk/*

ENTRYPOINT ["kafka-streams-ensure-explicit-resource-naming"]