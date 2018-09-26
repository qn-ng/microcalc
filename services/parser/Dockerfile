FROM maven:3-jdk-8-alpine as builder

COPY pom.xml /app/
WORKDIR /app
RUN mvn verify -fn
COPY . /app
RUN mvn clean package

FROM openjdk:8-jre-alpine
COPY --from=builder /app/target/parser.jar /parser.jar
EXPOSE 8080
CMD ["java", "-jar", "/parser.jar"]