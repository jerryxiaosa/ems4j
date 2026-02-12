# EMS4J

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

[中文文档](README.md)

EMS4J is a Spring Boot multi-module energy management system that supports both prepaid operations and energy-consumption analytics. It provides remote device control and multiple billing modes (pay-as-you-go, consolidated, monthly), supports both WeChat Pay and offline payments, and includes peak/off-peak metering, tiered pricing, account management, and financial accounting. It is compatible with multi-protocol device access, and the codebase is cleanly structured for easy extension.

## Motivation
In an era where AI can churn out runnable code in seconds, merely “working” code is losing its value; what is becoming precious is the taste and design embodied in that code. Such taste is never born overnight—it is forged through long-term architectural refinement and deep design reflection.

This project is exactly that kind of practice: I refactored and distilled the architecture of production-grade code—through crisp layer separation and thorough module decoupling—so that the core logic becomes purer and more adaptable to ever-changing requirements.

A few design gems are woven into the codebase, and I hope they spark inspiration. As the saying goes, “Give a man a fish and you feed him for a day; teach a man to fish and you feed him for a lifetime.” What I would rather share is the design thinking and architectural philosophy behind the code.

If this project helps you, please consider giving it a Star ⭐️ as support—I would be deeply honored.

## Features

- Multi-protocol device access 
- Billing models (pay-as-you-go / consolidated billing / monthly subscription)
- Metering and billing (peak/off-peak/valley / tiered rates)
- Account management (opening / closing / recharge)
- Remote control (switch on/off, multi-rate configuration)
- Financial accounting (bills, transactions, reconciliation)

## Prepaid Mode Description

The system supports three billing types: pay-as-you-go, consolidated, and monthly. In pay-as-you-go and consolidated modes, balance is deducted based on actual usage. In pay-as-you-go mode, each water/electric meter is settled independently. In consolidated mode, the balance is recharged on one water/electric meter and other meters use that balance. Monthly is settled at a fixed amount per cycle.

The typical flow is recharge after account opening, usage generates charges and continuously updates the balance, and when the balance is insufficient or reaches the warning threshold it can trigger notifications and remote disconnect. Full account closure will be settled and a settlement order will be generated, resulting in refunds or additional payment.

## Requirements

| Component | Version | Required |
|-----------|---------|----------|
| JDK | 17+ | Yes |
| Maven | 3.8+ | Yes |
| MySQL | 8.0+ | Yes |
| Redis | 6.0+ | Yes |
| RabbitMQ | 3.x | No |

## Quick Start

### 1) Clone the repository

### 2) Initialize database

```bash
mysql -u <user> -p <db> < sql/ems.sql
```

### 3) Configure the application

Edit `ems-bootstrap/src/main/resources/application-dev.yml`:
- Database connection (`spring.datasource`)
- Redis connection (`spring.data.redis`)
- RabbitMQ connection (`spring.rabbitmq`, optional)

### 4) Build and run

```bash
mvn clean package -DskipTests
java -jar ems-bootstrap/target/ems-0.1.0.jar --spring.profiles.active=dev
```

### 5) Access the system

- API Documentation: http://localhost:8080/doc.html

## Build & Test

```bash
# Full build (skip tests)
mvn clean install -DskipTests

# Run tests
mvn test

# Module build/test (example)
mvn -pl ems-business/ems-business-device -am test
```

## Tech Stack

| Category | Technology |
|----------|------------|
| Language/Framework | Java 17 / Spring Boot 3.5 |
| Persistence | MyBatis-Plus / MySQL 8.0 |
| Cache | Redis / Redisson |
| Message Queue | RabbitMQ (optional) |
| IoT Access | Netty |
| Auth | Sa-Token + JWT |
| API Doc | Knife4j / SpringDoc OpenAPI |

## Module Layered Architecture

```
+-------------------------------+          +-------------------------------+
|        ems-bootstrap          |          |           ems-iot             |
|       (Web Service Entry)     |          |      (IoT Service Standalone) |
+-------------------------------+          +-------------------------------+
               |                                          |
   +-----------+-----------+-----------+                  |
   |           |           |           |                  |
+--v-----+ +---v----+ +----v-------+   |                  |
| ems-web| | ems-mq | |ems-schedule|   |                  |
|(HTTP   | | (Msg)  | |  (Schedule)|   |                  |
| API)   | |        | |            |   |                  |
+--+-----+ +---+----+ +-----+------+   |                  |
   |           |            |          |                  |
   +-----------+------------+----------+------------------+
               |
+------------------------------------------------------------------+
|                        ems-business                               |
|    +------------+  +------------+  +------------+  +------------+ |
|    |   device   |  |  account   |  |  finance   |  |    plan    | |
|    | (Device Mgmt)| | (Account   |  | (Finance   |  | (Pricing  | |
|    |            |  |  Mgmt)     |  |  Accounting)|  |  Plan)    | |
|    +------------+  +------------+  +------------+  +------------+ |
+------------------------------------------------------------------+
               |
   +-----------+-----------+
   |                       |
+--v-------------------+  +v-----------------------+
|    ems-foundation    |  |    ems-components      |
| +------+ +---------+ |  | +----------+ +------+  |
| | user | |integrat.| |  | |datasource| | lock |  |
| +------+ +---------+ |  | +----------+ +------+  |
| +------+ +---------+ |  | +---------+ +-------+  |
| | space| | system  | |  | | context | | redis |  |
| +------+ +---------+ |  | +---------+ +-------+  |
| +------+ +---------+ |  +------------------------+
| | org  | | notifi. | |
| +------+ +---------+ |
+----------------------+
               |
       +-------v-------+
       |  ems-common   |
       | (Common Utils)|
       +---------------+
```

Notes:
- ems-web can depend on both ems-business and ems-foundation (user/org/space/system, etc.).
- ems-web should depend on service/dto only; avoid direct repository/entity/mapper access.
- ems-foundation should not depend on ems-business/ems-web to keep base domains reusable.

## Data Flow

```
+----------+    Command Send    +----------+    Protocol Conv   +----------+
|  ems-web |----------------->|  ems-iot |----------------->|   Device   |
+----------+                  +----------+                  +----------+
     ^                              |                              |
     |                              | Data Report                  |
     |                              v                              |
     |                       +----------+                         |
     +-----------------------| Business |<------------------------+
        API Result           | Layer    |
                           +----------+
                                 |
                                 v
                           +----------+
                           |   MySQL  |
                           +----------+
```

## Module Details

| Module | Responsibility |
|--------|----------------|
| `ems-bootstrap` | Application entry (Spring Boot) |
| `ems-web` | HTTP API layer |
| `ems-business-device` | Meter, gateway, device management |
| `ems-business-account` | Account opening/closing, balance, recharge |
| `ems-business-finance` | Billing, transactions, reconciliation |
| `ems-business-plan` | Pricing plans, rates, time-of-use periods |
| `ems-foundation-user` | Authentication, permissions, roles |
| `ems-foundation-organization` | Multi-tenant, org structure |
| `ems-foundation-space` | Space/area management |
| `ems-foundation-system` | System configuration |
| `ems-foundation-integration` | Third-party platform integration |
| `ems-components-*` | Common components (datasource/lock/context) |
| `ems-mq-*` | Messaging infrastructure API (ems-mq-api) and business messaging app layer (ems-mq-rabbitmq) |
| `ems-iot` | Netty device access, protocol parsing |
| `ems-schedule` | Scheduled jobs |

Notes:
- ems-mq-api provides message contracts and base messaging services (infrastructure layer).
- ems-mq-rabbitmq is the business messaging app layer, hosting message listeners and orchestration.

## Supported Devices

| Vendor | Type |
|--------|------|
| Acrel (安科瑞) | Meter / Gateway |
| Sfere (斯菲尔) | Meter |
| Yige (仪歌) | Meter |
| Yke (燕赵) | Meter |

## IoT Integration

There are two integration approaches:

1) **Direct device access (in-house platform)**
- Implement protocol access, parsing, command translation, and event publishing in `ems-iot`.
- References:
  - [Protocol Integration Guide](doc/modules/iot/protocol-integration-guide.md)
  - [Netty Multi-Protocol](doc/modules/iot/netty-multi-protocol.md)

2) **Third-party IoT platforms**
- Implement platform adapters under `ems-foundation/integration` and coordinate with `ems-iot` and business modules.
- Reference:
  - [Integration Module Overview](doc/modules/foundation/ems-foundation-integration.md)

For detailed platform integration solutions, see:
- [IoT Platform Integration Solutions](doc/iot-platform-integration-solutions.md)

## Documentation

| Document | Description |
|----------|-------------|
| [Development Practices Guide](doc/development-practices-guide.md) | Code style, naming conventions and development practices |
| [Business Module Documentation](doc/modules/business/README.md) | Business modules documentation (device, account, finance, plan) |
| [Foundation Module Documentation](doc/modules/foundation/README.md) | Foundation modules documentation (user, organization, space, system, integration) |
| [IoT Module Documentation](doc/modules/iot/README.md) | IoT module documentation for device access and protocol integration |
| [Test Guidelines](doc/test-guidelines.md) | Unit and integration test standards and best practices |

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

## Contact

- Email: jerryxiaoff@163.com
