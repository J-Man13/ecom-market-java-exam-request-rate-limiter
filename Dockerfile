FROM gradle:7.6-jdk17-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build test


FROM amazoncorretto:17.0.0-alpine
WORKDIR /app
COPY --from=build /home/gradle/src/build/libs/ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar ./ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar"]