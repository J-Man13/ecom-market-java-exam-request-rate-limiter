FROM amazoncorretto:17.0.0-alpine AS build
RUN mkdir -p /app
WORKDIR /app

COPY . /app
RUN ./gradlew clean build

FROM amazoncorretto:17.0.0-alpine
RUN mkdir -p /app
WORKDIR /app
COPY --from=build /app/build/libs/ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar /app/ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar
RUN chmod 755 /app/ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/app/ecom-market-java-exam-request-rate-limiter-0.0.1-SNAPSHOT.jar"]
