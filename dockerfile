FROM openjdk:11-jre-slim
# try out alpine/debian

COPY build/libs/sandbox_bap_client-*.*.*-SNAPSHOT.jar /usr/local/lib/sandbox_bap_client.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /usr/local/lib/sandbox_bap_client.jar --spring.config.location=file:///usr/local/lib/application.yml"]
