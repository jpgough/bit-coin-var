FROM openjdk:8-jdk-alpine
VOLUME /tmp
ADD target/value-at-risk-docker-0.1.0.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]