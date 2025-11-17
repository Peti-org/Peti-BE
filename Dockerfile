# syntax=docker/dockerfile:1.16.0
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml /build/
COPY src /build/src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy

#RUN --mount=type=cache,target=/var/cache/apt,id=slim-apt-cache-sv \
#    --mount=type=cache,target=/var/lib/apt,id=slim-apt-lib-sv \
#    set -eux \
#    && apt-get update \
#    && apt-get install -y --no-install-recommends \
#        supervisor \
#    && addgroup --system -gid 2999 marvin \
#    && adduser --system --ingroup marvin -uid 2000 marvin \
#    && mkdir -p /etc/supervisor.d \
#    && echo "[include]" > /etc/supervisord.conf \
#    && echo "files=/etc/supervisor.d/*.ini" >> /etc/supervisord.conf \
#    && chmod 0766 /etc/supervisord.conf

#COPY docker/api.ini /etc/supervisor.d/api.ini
#COPY docker/supervisord.ini /etc/supervisor.d/supervisord.ini
#COPY --from=build /build/target/application.jar /app.jar
#ADD --chown=marvin:marvin https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar /
#
EXPOSE 8080
#EXPOSE 6666
#
#USER marvin:marvin
#
#ENV JAVA_TOOL_OPTIONS "-javaagent:/opentelemetry-javaagent.jar"
#ENV API_JAVA_ARGS -Dserver.port=8080
#ENV API_SPRING_ARGS ""
#ENTRYPOINT ["/usr/bin/supervisord", "-u", "marvin"]
COPY --from=build /build/target/application.jar /app.jar
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v1.30.0/opentelemetry-javaagent.jar /opt/opentelemetry-agent.jar
ENTRYPOINT [ "java", "-javaagent:/opt/opentelemetry-agent.jar", \
    "-Dotel.resource.attributes=service.instance.id=back-end", \
    "-Dotel.javaagent.logging=application", \
    "-Dotel.service.name=back-end", \
   "-jar", "/app.jar" ]
