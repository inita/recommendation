FROM fabric8/java-jboss-openjdk8-jdk:1.5.2
ENV JAVA_APP_DIR=/deployments
EXPOSE 8080 8778 9779
COPY target/recommendation.jar /deployments/
