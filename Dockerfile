FROM openjdk:11-jre-slim-buster

ARG PROFILE

ENV USE_PROFILE=$PROFILE
ENV CONFIG_SERVER ""
ENV ENCRYPT_KEY ""
ENV JOB_NAME ""
ENV DATE ""

COPY app.jar /app.jar

CMD ["java", "-Dspring.profiles.active=${USE_PROFILE}", "-Duser.timezone=Asia/Seoul", "-jar", "./app.jar", "--job.name=${JOB_NAME}", "--date=${DATE}"]