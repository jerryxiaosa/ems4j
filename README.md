# EMS4J

EMS4J is a comprehensive energy management platform with capabilities for device access, metering and billing, account management, financial accounting, and operations support. It provides an abstract integration model for IoT platforms and also supports direct device access via the built-in IoT module.

## Tech Stack

- Java 17 / Spring Boot 3 / Maven
- MyBatis / JUnit 5 / Mockito
- Netty (IoT access and protocol handling)
- RabbitMQ (messaging, optional)

## Module Responsibilities

- `ems-bootstrap`: Application entry (Spring Boot).
- `ems-web`: HTTP API layer.
- `ems-business/*`: Core business domains (device/account/billing/finance/aggregation).
- `ems-foundation/*`: Shared foundation services (system/user/area/organization/integration/notification).
- `ems-components/*`: Common components (datasource/lock/context).
- `ems-mq/*`: Messaging API and RabbitMQ implementation.
- `ems-iot`: Device access, protocol parsing, command dispatching, event processing.
- `ems-schedule`: Scheduled jobs.
- `doc/`: Documentation.
- `sql/`: Database initialization scripts.

## Module Dependency Diagram (ASCII)

```
                      +-------------------+
                      |   ems-bootstrap   |
                      +---------+---------+
                                |
        +-----------------------+-----------------------+
        |                       |                       |
     +--v---+               +--v---+               +----v-----+
     |ems-web|              |ems-iot|              |ems-schedule|
     +--+---+               +--+---+               +----+-----+
        \                     |                       /
         \                    |                      /
          +--------------------v---------------------+
          |              ems-business                |
          +--------------------+---------------------+
                               |
                 +-------------+-------------+
                 |                           |
        +--------v--------+         +--------v--------+
        | ems-foundation  |         | ems-components  |
        +-----------------+         +-----------------+

```

## Quick Start

1) Initialize database

```
mysql -u <user> -p <db> < sql/ems.sql
```

2) Run service

```
mvn clean package -DskipTests
java -jar ems-bootstrap/target/ems-0.1.0.jar
```

## Build & Test

```
# Full build (skip tests)
mvn clean install -DskipTests

# Run tests
mvn test

# Module build/test (example)
mvn -pl ems-business/ems-business-device -am test
```

## IoT Integration

There are two integration approaches:

1) **Direct device access (in-house platform)**
- Implement protocol access, parsing, command translation, and event publishing in `ems-iot`.
- References:
  - `doc/modules/iot/protocol-integration-guide.md`
  - `doc/modules/iot/vendor-extension-checklist.md`
  - `doc/modules/iot/netty-multi-protocol.md`

2) **Third-party IoT platforms**
- Implement platform adapters under `ems-foundation/integration` and coordinate with `ems-iot` and business modules.
- Reference:
  - `doc/modules/integration/ems-foundation-integration-overview.md`

## IoT Module Docs

Currently integrated vendors include Acrel, Sfere, Yige, and Yke devices/meters.

- Architecture & layering: `doc/modules/iot/architecture-layering.md`
- Protocol integration guide: `doc/modules/iot/protocol-integration-guide.md`
- Device identity mapping: `doc/modules/iot/device-identity-mapping.md`
- Netty & channel management:
  - `doc/modules/iot/netty-multi-protocol.md`
  - `doc/modules/iot/channel-manager.md`
- Exception handling: `doc/modules/iot/exception-handling-guidelines.md`

## Coding Standards

- Development practices: `doc/development-practices-guide.md`
- Unit test guidelines: `doc/test/unit-test-guidelines.md`

## License

- This project is licensed under the MIT License. See `LICENSE`.

## Contact

- Email: jerryxiaoff@163.com
