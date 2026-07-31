# System Design

Navigation for the core system-design topics in this repository:

- [Scalability](./Scalability/README.md) — scaling dimensions, capacity planning, replication, and partitioning.
- [Caching](./Caching/README.md) — cache patterns, invalidation, eviction, and failure modes.
- [Messaging](./Messaging/README.md) — queues, streams, delivery semantics, and event-driven communication.
- [Distributed Systems](./Distributed-Systems/README.md) — architecture choices, consistency, resilience, CQRS, event sourcing, and sagas.
- [Load Balancing](./Load-Balancing/README.md) — traffic distribution, health checks, and routing algorithms.
- [Rate Limiting](./Rate-Limiting/README.md) — overload protection, fairness, and distributed enforcement.
- [Frontend](../06-Frontend/README.md) — browser architecture, performance, state, and reliability.
- [DevOps](../07-DevOps/README.md) — delivery, infrastructure, observability, and operations.

## Architecture selection

Architecture selection decides how a system is organized, how components communicate, and how it evolves. Balance simplicity, maintainability, scalability, performance, reliability, delivery speed, team size, operational maturity, deployment strategy, and expected growth. There is no universally best style: every choice solves particular problems and introduces costs.

```text
Business requirements
        ↓
System complexity
        ↓
Scalability needs
        ↓
Communication pattern
        ↓
Operational complexity
        ↓
Choose architecture
```

Guidance:

- Start with the simplest architecture that satisfies current requirements.
- Avoid solving hypothetical problems and optimize only after finding real bottlenecks.
- Prefer maintainability and an explicit evolution path over fashionable complexity.
- Ask what problem the pattern solves, whether it is needed now, what it costs, whether the team can operate it, and whether a simpler option exists.

## Cross-topic decision matrices

### Application architecture

| Requirement | Recommended starting point |
| --- | --- |
| Simple CRUD or familiar enterprise application | Layered architecture |
| Large, business-driven system needing framework-independent domain logic | Clean architecture |
| Integration-heavy system needing replaceable external adapters | Hexagonal architecture |
| Small team, MVP, simple deployment | Monolith |
| Growing codebase with enforceable module boundaries | Modular monolith |
| Independent team deployment and per-capability scaling | Microservices |

### Communication and messaging

| Requirement | Recommended choice |
| --- | --- |
| Immediate response or request-time validation | Synchronous communication |
| Long-running work or temporary receiver unavailability | Asynchronous communication |
| Several independent reactions to one business fact | Event-driven architecture |
| One consumer handles each task | Queue |
| Independent subscribers each receive a publication | Topic |
| Retention, replay, and very high-throughput streams | Kafka-style log |
| Flexible routing and task processing | RabbitMQ-style broker |
| Reliable processing where duplicates are acceptable | At-least-once plus idempotency |

### API management and traffic

| Requirement | Recommended choice |
| --- | --- |
| Unified client entry point and API concerns | API gateway |
| Dynamic service locations | Service discovery |
| Traffic distribution across healthy instances | Load balancer |
| Protect capacity or enforce quotas | Rate limiter |

### Caching

| Requirement | Recommended choice |
| --- | --- |
| Read-heavy workload | Cache-aside |
| Cache-managed loading | Read-through |
| Fresher reads after writes | Write-through |
| Maximum write throughput with accepted durability risk | Write-behind |
| Predictably changing data | TTL |
| Cross-service invalidation | Change event |
| Capacity pressure | LRU or LFU selected from measured access patterns |

### Resilience and data

| Requirement | Recommended choice |
| --- | --- |
| Temporary failure | Bounded retry with backoff and jitter |
| Hanging request | Timeout/deadline |
| Repeated downstream failure | Circuit breaker |
| Resource exhaustion isolation | Bulkhead |
| Reduced service instead of total failure | Fallback |
| Very different read and write models | CQRS |
| Immutable audit history and replay | Event sourcing |
| Transaction spanning service-owned databases | Saga |
| Transaction contained by one database | ACID transaction |

### Scalability and availability

| Requirement | Recommended choice |
| --- | --- |
| More traffic | Horizontal scaling after removing the measured bottleneck |
| Less source-database work | Caching and query/index optimization |
| Independently scale reads | CQRS or read replicas, according to the problem |
| Independently scale business capabilities | Microservices, when operationally justified |
| Dynamic infrastructure | Service discovery |
| Survive instance failure | Redundant load balancing and healthy spare capacity |
| Survive dependency failure | Timeout, circuit breaker, bulkhead, and fallback |

## Common architecture-selection mistakes

- Choosing a pattern because it is popular rather than because it solves a measured problem.
- Optimizing too early when a simple architecture is sufficient.
- Ignoring deployment, monitoring, maintenance, and on-call costs.
- Designing primarily for imagined future requirements instead of allowing current architecture to evolve.

## Cross-topic interview and scenario questions

Explain trade-offs and rejected alternatives, not just definitions:

1. How would you choose among layered, clean, and hexagonal architecture?
2. When is a modular monolith preferable to microservices, and why do startups often begin with a monolith?
3. What are the costs of adopting microservices too early? What would you choose for five developers building a new SaaS product?
4. When should communication be synchronous or asynchronous? Explain an event-driven flow and its disadvantages.
5. Queue versus topic; Kafka versus RabbitMQ; and at-most-once versus at-least-once versus exactly-once?
6. What is idempotency, why is a DLQ useful, and how do you prevent duplicate processing?
7. API gateway versus load balancer; client-side versus server-side discovery; and how do Kubernetes Services provide discovery?
8. When should caching be introduced? Compare cache-aside and write-through, LRU and LFU, and stampede, avalanche, and penetration.
9. How would you cache an e-commerce product catalog?
10. Retry versus circuit breaker; why use limited exponential backoff; and how do timeout, bulkhead, fallback, and circuit states interact?
11. CQRS versus CRUD; does CQRS require event sourcing; why are events immutable; and when should event sourcing be avoided?
12. Saga versus ACID and 2PC; choreography versus orchestration; and what is a compensating transaction?
13. For 100 million daily reads, what patterns would you investigate?
14. How would you handle an intermittently timing-out payment gateway?
15. What would provide complete financial audit history?
16. How would order, payment, and inventory complete one business transaction?
17. How would you investigate a slow dashboard or database CPU consistently above 90%?
18. How should frequently changing service instances find one another?
19. How do you stop one unavailable service from causing cascading failures?
20. What supports zero-downtime deployment?
21. Does a 50:1 read/write ratio justify CQRS? What other evidence is needed?
22. Choose and justify architectures for a bank and a startup MVP, messaging for analytics, caching for profiles, resilience for a third-party payment provider, and distributed transactions for checkout.

## Professional correction

- “Medium-sized application with clear separation → Clean Architecture” is not a sufficient rule. Clean architecture is justified by domain complexity, boundary longevity, and testability needs; size alone does not select it.
- “Highly testable domain logic → Hexagonal Architecture” is incomplete. Clean and hexagonal approaches both support isolated testing; hexagonal architecture is especially useful when ports to several external systems must remain replaceable.
- “At-least-once + idempotency prevents duplicate side effects” is an engineering strategy, not a delivery guarantee. Idempotency must cover the actual side-effect boundary.
- “Strong consistency → write-through” overstates cache guarantees. Updating cache and database is not automatically atomic; define the failure and transaction boundary.
- “Saga is required for distributed transactions” is too broad. Prefer one ACID owner when possible; sagas provide eventual business consistency, while 2PC is a distinct coordination option with availability and operational costs.

## Source-heading coverage

| `03-Architecture.md` heading | Canonical destination |
| --- | --- |
| 1. Architecture Selection | This README: Architecture selection |
| 2. Layered Architecture | [Distributed Systems](./Distributed-Systems/README.md#layered-clean-and-hexagonal-styles) |
| 3. Clean Architecture | [Distributed Systems](./Distributed-Systems/README.md#layered-clean-and-hexagonal-styles) |
| 4. Hexagonal Architecture | [Distributed Systems](./Distributed-Systems/README.md#layered-clean-and-hexagonal-styles) |
| 5. Monolith vs Modular Monolith vs Microservices | [Distributed Systems](./Distributed-Systems/README.md#monolith-modular-monolith-and-microservices) |
| 6. Synchronous vs Asynchronous Communication | [Distributed Systems](./Distributed-Systems/README.md#communication-and-event-driven-architecture) |
| 7. Event-Driven Architecture | [Distributed Systems](./Distributed-Systems/README.md#communication-and-event-driven-architecture) |
| 8. Message Brokers & Queues | [Messaging](./Messaging/README.md#source-coverage-supplement) |
| 9. Message Delivery Guarantees | [Messaging](./Messaging/README.md#delivery-guarantees) |
| 10. API Gateway | [Distributed Systems](./Distributed-Systems/README.md#api-gateway-and-service-discovery); rate enforcement in [Rate Limiting](./Rate-Limiting/README.md#architecture-source-routing-context) |
| 11. Service Discovery | [Distributed Systems](./Distributed-Systems/README.md#api-gateway-and-service-discovery) |
| 12. Load Balancing | [Load Balancing](./Load-Balancing/README.md#source-coverage-supplement) |
| 13. Caching Strategy | [Caching](./Caching/README.md#source-coverage-supplement) |
| 14. Cache Invalidation & Eviction Policies | [Caching](./Caching/README.md#source-coverage-supplement) |
| 15. Resilience Patterns | [Distributed Systems](./Distributed-Systems/README.md#resilience-patterns) |
| 16. Circuit Breaker | [Distributed Systems](./Distributed-Systems/README.md#resilience-patterns) |
| 17. CQRS | [Distributed Systems](./Distributed-Systems/README.md#cqrs) |
| 18. Event Sourcing | [Distributed Systems](./Distributed-Systems/README.md#event-sourcing) |
| 19. Saga Pattern | [Distributed Systems](./Distributed-Systems/README.md#saga) |
| 20. Decision Matrix | This README: Cross-topic decision matrices |
| 21. Interview Questions | This README: Cross-topic interview and scenario questions; topic-specific banks in each canonical README |

## Provenance

- **Source-derived:** Architecture-selection factors and flow, all decision-matrix choices, common mistakes, and the consolidated interview/scenario question set were restored from `03-Architecture.md`.
- **Editorial:** Navigation, cross-links, clarified selection criteria, and `Professional correction` reconcile source shorthand with production semantics.
