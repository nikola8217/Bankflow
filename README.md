# BankFlow

A distributed banking system built to demonstrate production-grade backend architecture patterns. BankFlow handles core banking operations — deposits, withdrawals, and transfers — with a focus on consistency, fault tolerance, and scalability.

---

## Architecture Overview

BankFlow is composed of four independent microservices that communicate asynchronously through Apache Kafka:

```
┌─────────────┐     ┌─────────────┐
│    Auth     │     │   Account   │
│   Service   │     │   Service   │
└─────────────┘     └─────────────┘
        │                   │
        └─────────┬─────────┘
                  │
         ┌────────▼────────┐
         │   Transaction   │  ← Write side (Event Store)
         │    Service      │
         └────────┬────────┘
                  │ Kafka (Outbox Pattern)
         ┌────────▼────────┐
         │     Ledger      │  ← Read side (Balance validation)
         │    Service      │
         └─────────────────┘
```

### Services

| Service | Responsibility |
|---|---|
| **Auth** | Authentication and authorization |
| **Account** | Account management and registration |
| **Transaction** | Write side — initiates transactions, owns the event store |
| **Ledger** | Read side — validates balances, approves or declines transactions |

---

## Key Architecture Patterns

### Event Sourcing
The Transaction service uses an event store as its source of truth. Every state change (deposit, withdrawal, transfer) is stored as an immutable event and appended to the aggregate. The current state is derived by replaying events.

### CQRS (Command Query Responsibility Segregation)
Write and read responsibilities are separated across services:
- **Transaction service** handles commands and owns the event store
- **Ledger service** maintains a read-optimized projection of account balances

### Choreography Saga
The transaction flow is coordinated through a choreography saga — each service reacts to events emitted by other services without a central orchestrator.

**Why choreography over orchestration?**

With only two services participating in the transaction flow (Transaction and Ledger), introducing an orchestrator would add unnecessary complexity and coupling. Choreography keeps the services fully decoupled — each service only knows about the events it produces and consumes, not about the other services.

**Transaction flow:**
1. Transaction service receives a command (e.g. withdrawal)
2. `TransactionInitiated` event is written to the event store and the outbox table
3. Outbox worker picks up the event and publishes it to Kafka
4. Ledger service consumes the event and validates the balance
5. Ledger emits `TransactionApproved` or `TransactionDeclined`
6. Transaction service consumes the response and appends the final event to the event store

### Outbox Pattern
To guarantee at-least-once delivery between the Transaction service and Kafka, the outbox pattern is used. Events are written to an outbox table within the same database transaction as the domain change, eliminating the dual-write problem. A dedicated worker polls the outbox and publishes events to Kafka.

### CommandBus (MediatR Pattern)
All write operations in the Transaction service are dispatched through a custom CommandBus — a manual implementation of the MediatR pattern. Each command has a single dedicated handler registered automatically via Spring's dependency injection. This decouples the controller layer from business logic and makes adding new commands straightforward without modifying existing code.

### Idempotency
- **HTTP level**: Transaction service accepts an optional `Idempotency-Key` header. Duplicate requests with the same key are detected and rejected before processing.
- **Event level**: Ledger service tracks processed events by `transactionId + eventType` to prevent duplicate projections on Kafka redelivery.

### Hexagonal Architecture (Ports & Adapters)
Each service follows hexagonal architecture — the domain core is isolated from infrastructure concerns (Kafka, database, HTTP). This makes the domain logic independently testable and infrastructure-replaceable.

### Domain-Driven Design
The Transaction aggregate encapsulates all domain logic and enforces invariants. Events are first-class citizens in the domain model.

---

## Tech Stack

- **Java 21** + **Spring Boot 4**
- **Apache Kafka** — async messaging
- **PostgreSQL** — event store and outbox table (separate DB per service)
- **SpringDoc OpenAPI 3** — Swagger UI documentation
- **Docker** + **Docker Compose** — local environment

---

## Running Locally

**Prerequisites:** Docker and Docker Compose

```bash
git clone https://github.com/nikola8217/Bankflow.git
cd Bankflow
docker-compose up --build
```

All services and infrastructure (Kafka, PostgreSQL, Zookeeper, Kafdrop) will start automatically.

---

## API Documentation

Swagger UI is available per service after startup:

| Service | URL |
|---|---|
| Auth | http://localhost:8081/swagger-ui.html |
| Account | http://localhost:8082/swagger-ui.html |
| Transaction | http://localhost:8083/swagger-ui.html |
| Ledger | http://localhost:8085/swagger-ui.html |

To authenticate, click **Authorize** in Swagger UI and enter your JWT token obtained from `POST /api/auth/login`.

---

## Kafka Topics

| Topic | Producer | Consumer |
|---|---|---|
| `transaction-created` | transaction-service | ledger-service |
| `transaction-approved` | ledger-service | transaction-service, ledger-service |
| `transaction-declined` | ledger-service | transaction-service, ledger-service |

---

## Design Decisions & Trade-offs

**Why Event Sourcing?**
Banking systems require a full audit trail of every state change. Event sourcing provides this natively — every transaction is stored as an immutable event, making it possible to reconstruct the state at any point in time.

**Why Event Sourcing only on the Transaction service?**
Account management is CRUD — no complex state transitions. Event Sourcing adds value where audit trail and state reconstruction matter, which is the transaction domain. Applying it everywhere would be over-engineering.

**Why separate Transaction and Ledger services?**
Separating the write side (Transaction) from the read side (Ledger) allows each to scale independently. The Ledger service can be optimized purely for read performance without affecting the write path.

**Why choreography instead of orchestration?**
With two services in the saga, an orchestrator would introduce a single point of failure and unnecessary coupling. Choreography keeps services autonomous and independently deployable.