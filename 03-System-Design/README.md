# System Design

Navigation for the core system-design topics in this repository:

- [Scalability](./Scalability/README.md) — scaling dimensions, capacity planning, replication, and partitioning.
- [Caching](./Caching/README.md) — cache patterns, invalidation, eviction, and failure modes.
- [Messaging](./Messaging/README.md) — queues, streams, delivery semantics, and event-driven communication.
- [Distributed Systems](./Distributed-Systems/README.md) — architecture choices, consistency, resilience, command/query separation, event sourcing, and sagas.
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

## Beginner foundation

Use a restaurant chain as a mental model. Customers create requests, a front desk routes them, kitchens do the work, storage holds durable records, a small counter keeps frequently used items close, and a ticket rail buffers work. Adding software components can improve capacity or reliability, but every handoff adds delay and another failure case.

Core terms used throughout this section:

- A **distributed system** is a group of independent processes or computers that communicate over a network and appear to cooperate on one service.
- A **node** is one participating process, virtual machine, physical machine, or device.
- **Latency** is how long one operation takes, such as 120 milliseconds for one response. **Throughput** is how much work completes per unit of time, such as 5,000 requests per second. Improving one does not guarantee improving the other.
- **Availability** is the proportion of time the service can successfully perform its promised work.
- **Consistency** describes which values different reads are allowed to observe after a write. Stronger consistency usually needs more coordination; eventual consistency permits temporary disagreement but requires replicas to converge.
- **Replication** keeps multiple copies of the same data or service. It helps reads and failure recovery but creates synchronization and failover questions.
- A **partition** can mean either a network failure that separates nodes or a deliberate slice of data assigned to one owner. The surrounding sentence must make the meaning clear.
- A **broker** accepts, stores, and routes messages between producers and consumers.
- A **cache** is a faster, usually temporary copy of reusable data.
- A **load balancer** selects a healthy service instance for each connection or request.
- A **rate limit** is an admission rule that restricts how much work a caller may start during a period.

Common abbreviations are expanded before the decision tables use them: **create, read, update, and delete (CRUD)**; **minimum viable product (MVP)**; **application programming interface (API)**; **time to live (TTL)**; **least recently used (LRU)**; **least frequently used (LFU)**; **command query responsibility segregation (CQRS)**; **atomicity, consistency, isolation, and durability (ACID)**; **two-phase commit (2PC)**; **dead-letter queue (DLQ)**; **central processing unit (CPU)**; and **software as a service (SaaS)**.

### One concrete request flow

```text
Client
  → rate limit
  → load balancer
  → application node
  → cache
      ├─ hit  → response
      └─ miss → database → fill cache → response
                         └→ broker → background consumers
```

Step by step, the admission rule rejects excess traffic, the balancer chooses a healthy node, the application checks for a reusable result, and a miss reaches durable storage. The application may publish follow-up work through a broker. Failures can occur at every arrow: a timeout can leave the outcome unknown, a cache can be stale, a replica can lag, or a message can be delivered twice. Production design therefore specifies deadlines, ownership, consistency, retries, idempotency, observability, and fallback behavior instead of drawing only a happy path.

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

For every prompt, give a compact answer in this order: clarify the workload and correctness goal; estimate traffic, data, latency, throughput, and availability; sketch the request/data flow; identify the bottleneck and failure boundaries; choose the simplest fitting design; then state consistency, recovery, operations, cost, and rejected alternatives. Explain trade-offs rather than listing definitions:

1. **How would you choose among layered, clean, and hexagonal architecture?**
   **Key points:** layered architecture organizes common application concerns;
   clean architecture protects domain rules from frameworks; hexagonal
   architecture makes external integrations replaceable through ports. Choose
   the simplest boundary that matches the expected changes.
2. **When is a modular monolith preferable to microservices, and why do
   startups often begin with a monolith?** **Key points:** use one deployment
   while a small team and domain are still changing. Add explicit modules so
   boundaries can be tested and extracted later if independent ownership,
   scaling, or deployment becomes necessary.
3. **What are the costs of adopting microservices too early? What would you
   choose for five developers building a new SaaS product?** **Key points:**
   microservices add network failures, distributed data, tracing, deployment,
   and on-call work. Five developers usually benefit from a modular monolith
   unless a measured constraint requires separate services.
4. **When should communication be synchronous or asynchronous?**
   **Key points:** use synchronous calls when the caller needs an immediate
   answer. Use asynchronous messaging when work can finish later or producers
   and consumers should be decoupled. Events add delay, duplicates, ordering
   questions, and harder debugging.
5. **Queue versus topic; Kafka versus RabbitMQ; and delivery guarantees?**
   **Key points:** a queue distributes work among consumers; a topic broadcasts
   to subscriptions. Kafka emphasizes retained, replayable logs; RabbitMQ
   emphasizes flexible routing and task delivery. At-most-once may lose work;
   at-least-once may duplicate it; end-to-end exactly-once effects require
   transactional or idempotent handling.
6. **What are idempotency and a dead-letter queue (DLQ)?**
   **Key points:** idempotency makes repeating one logical request safe. A DLQ
   isolates messages that repeatedly fail. Use stable operation IDs, durable
   deduplication, and atomic side-effect recording to prevent duplicate effects.
7. **API gateway, load balancer, and service discovery: how do they differ?**
   **Key points:** a gateway applies API policy, a load balancer distributes
   traffic, and discovery locates live instances. Kubernetes Services normally
   combine Domain Name System (DNS) names with virtual routing to healthy pods.
8. **When should caching be introduced, and how do its choices differ?**
   **Key points:** add it after measuring repeated expensive reads. Cache-aside
   fills on a miss; write-through updates on writes. Least recently used (LRU)
   and least frequently used (LFU) evict different access patterns. Prevent
   stampedes, synchronized-expiry avalanches, and repeated absent-key
   penetration.
9. **How would you cache an e-commerce product catalog?** **Key points:** cache
   product views by tenant, locale, currency, and version; invalidate after
   catalog changes; jitter expiry times; protect hot keys; and keep checkout
   price and inventory validation authoritative.
10. **How do retry, timeout, circuit breaker, bulkhead, and fallback interact?**
    **Key points:** timeout bounds waiting, retry handles transient failures,
    exponential backoff spreads retries, a circuit breaker stops repeated calls,
    a bulkhead limits affected resources, and a fallback provides reduced
    service. Every retry needs a limit and idempotency analysis.
11. **CQRS versus CRUD; does CQRS require event sourcing?** **Key points:**
    create/read/update/delete (CRUD) uses one model for ordinary operations.
    Command Query Responsibility Segregation (CQRS) separates write and read
    models when their needs differ. It does not require event sourcing. Avoid
    event sourcing when replay, audit, and temporal reconstruction do not
    justify its schema and operational complexity.
12. **Saga versus ACID and two-phase commit (2PC)?** **Key points:** one
    database transaction with atomicity, consistency, isolation, and durability
    (ACID) is simplest. 2PC coordinates participants but can reduce
    availability. A saga uses local commits and compensating business actions;
    orchestration centralizes the flow, while choreography reacts to events.
13. **For 100 million daily reads, what would you investigate?** **Key points:**
    calculate peak requests per second, inspect query plans, add suitable
    indexes, cache repeated reads, use replicas when staleness is acceptable,
    paginate results, precompute expensive views, and load-test before sharding.
14. **How would you handle an intermittently timing-out payment gateway?**
    **Key points:** set a deadline, use an idempotency key, retry only safe
    transient failures with bounded backoff, open a circuit after repeated
    failures, persist an unknown state, and reconcile with provider records.
15. **What provides complete financial audit history?** **Key points:** keep an
    immutable double-entry ledger or append-only event history, stable IDs,
    actor and timestamp metadata, corrections as new entries, access controls,
    retention, reconciliation, and tested backups.
16. **How can order, payment, and inventory complete one business
    transaction?** **Key points:** prefer one ACID owner when possible.
    Otherwise use a saga: reserve inventory, authorize payment, confirm the
    order, and compensate completed steps when a later step fails.
17. **How would you investigate a slow dashboard or database CPU above 90%?**
    **Key points:** measure end-to-end latency, trace requests, inspect slow
    queries and execution plans, check connection pools and lock waits, identify
    traffic or deployment changes, then optimize the measured bottleneck.
18. **How should changing service instances find one another?** **Key points:**
    use a service registry or platform DNS, health checks, and client-side or
    server-side load balancing. Remove unhealthy instances quickly and avoid
    hard-coded addresses.
19. **How do you stop one unavailable service from causing cascading
    failures?** **Key points:** use deadlines, bounded retries, circuit
    breakers, bulkheads, backpressure, load shedding, queues where appropriate,
    and graceful degradation backed by useful observability.
20. **What supports zero-downtime deployment?** **Key points:** keep API and
    database changes backward compatible, use readiness checks and gradual
    rollout, drain in-flight work, monitor service-level objectives, and retain
    a tested rollback or roll-forward path.
21. **Does a 50:1 read/write ratio justify CQRS?** **Key points:** not by
    itself. Examine whether read and write models need different schemas,
    scaling, latency, security, ownership, or release cycles, and whether the
    consistency and operational costs are acceptable.
22. **How would choices differ across common scenarios?** **Key points:** a
    bank prioritizes correctness, audit, and controlled change; a startup
    minimum viable product usually prioritizes a simple modular deployment.
    Analytics often fits retained event streams, profile caching needs explicit
    freshness, payment dependencies need resilience and reconciliation, and
    checkout needs an ACID boundary or carefully designed saga.

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
