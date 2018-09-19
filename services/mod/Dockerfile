FROM maven:3-jdk-8-alpine as builder

RUN mkdir /app
COPY . /app
WORKDIR /app
RUN mvn clean package

FROM openjdk:8-jre-alpine
COPY --from=builder /app/target/mod.jar /mod.jar
EXPOSE 8080
CMD ["java", "-jar", "/mod.jar"]