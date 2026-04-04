FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml ./
COPY ems-bootstrap ./ems-bootstrap
COPY ems-business ./ems-business
COPY ems-common ./ems-common
COPY ems-components ./ems-components
COPY ems-foundation ./ems-foundation
COPY ems-iot ./ems-iot
COPY ems-iot-simulator ./ems-iot-simulator
COPY ems-mq ./ems-mq
COPY ems-schedule ./ems-schedule
COPY ems-web ./ems-web

RUN mvn -pl ems-iot -am clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN mkdir -p /app/logs

COPY --from=builder /build/ems-iot/target/*-exec.jar /app/ems-iot.jar

EXPOSE 8880 19500

ENTRYPOINT ["java", "-jar", "/app/ems-iot.jar"]
