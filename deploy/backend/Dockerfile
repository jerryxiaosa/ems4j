FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml ./
COPY ems-bootstrap ./ems-bootstrap
COPY ems-common ./ems-common
COPY ems-components ./ems-components
COPY ems-business ./ems-business
COPY ems-foundation ./ems-foundation
COPY ems-iot ./ems-iot
COPY ems-mq ./ems-mq
COPY ems-schedule ./ems-schedule
COPY ems-web ./ems-web

RUN mvn -pl ems-bootstrap -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /build/ems-bootstrap/target/*.jar /app/ems-bootstrap.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/ems-bootstrap.jar", "--spring.profiles.active=docker"]
