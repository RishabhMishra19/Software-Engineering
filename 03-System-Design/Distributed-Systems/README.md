# Distributed Systems

## Overview

A distributed system coordinates independent processes over unreliable networks. It gains independent scaling, deployment, and fault isolation, but must handle partial failure, concurrency, latency, and consistency explicitly. Start with the simplest architecture that meets present requirements; distribution is a trade, not an automatic upgrade.

## Why do we need it?

Distribution is justified when one deployable unit or datastore cannot meet availability, geographic, scaling, data-sovereignty, or team-autonomy needs. A modular monolith often preserves clear domain boundaries with far less operational cost and is a sound evolution point before microservices.

## How does it work?

**Application boundaries**

- **Layered architecture** separates controller, service, and repository concerns. It is familiar and effective for CRUD systems, but undisciplined layers leak business logic and grow into large services.
- **Clean architecture** points dependencies inward toward framework-independent domain rules. **Hexagonal architecture** expresses external boundaries as ports implemented by adapters. Both improve testability and replaceability, but interfaces at every class create needless boilerplate.
- A **monolith** deploys and scales together; a **modular monolith** enforces internal domain boundaries; **microservices** independently deploy business capabilities and usually own their data. Split only around stable capabilities and operational need.

**Communication and discovery**

Synchronous request-response gives immediate feedback but forms a latency and availability chain. Asynchronous [messaging](../Messaging/README.md) decouples time and absorbs bursts but creates eventual consistency. Event-driven architecture lets multiple consumers react without producer knowledge, while shared event contracts still create coupling.

Dynamic instances register with or are represented by service discovery. In client-side discovery, the caller chooses an instance; in server-side discovery, a proxy or [load balancer](../Load-Balancing/README.md) does. An API gateway is a client-facing entry point for routing, authentication, quotas, and transformation; keep domain logic out of it and deploy it redundantly.

**Data and consistency patterns**

- **CQRS** separates command and query models when their workloads genuinely differ; it does not require separate databases or event sourcing.
- **Event sourcing** persists immutable domain events and rebuilds state, often with snapshots and projections. It provides audit history but imposes event evolution, replay, storage, and privacy challenges.
- A **saga** coordinates local transactions across services using choreography or an orchestrator. Failure triggers compensating business actions, not a literal rollback; use a single ACID transaction when one database can own the workflow.

**Resilience**

Set end-to-end deadlines and per-hop timeouts. Retry only transient, idempotent operations with limits, exponential backoff, jitter, and a shared retry budget. Circuit breakers fail fast after repeated dependency failures and probe recovery in half-open state. Bulkheads isolate pools and quotas; fallbacks provide explicitly degraded results. These patterns limit failures rather than prevent them.

**Production failure modes and practices**

- Cascading failure: synchronized retries amplify an outage. Use backoff, jitter, admission control, and load shedding.
- Split brain and stale replicas: define consistency requirements and fencing/consensus where concurrent leaders are unsafe.
- Dual writes lose one side: use an outbox or change-data capture instead of hoping two independent writes both succeed.
- Event chains become invisible: use correlation IDs, distributed tracing, workflow state, and reconciliation jobs.
- Gateway, registry, or orchestrator becomes a bottleneck: run redundantly and keep responsibilities narrow.
- Common mistakes include premature microservices, chatty synchronous call graphs, shared databases with unclear ownership, assuming the network is reliable, and claiming “exactly once” without defining the boundary.

## Advantages

- Independent scaling, deployment, and technology choices where justified.
- Fault and ownership boundaries around business capabilities.
- Geographic distribution and higher aggregate capacity.
- Multiple consistency and data models can fit distinct workloads.

## Limitations

- Partial failures are ambiguous; remote calls are slower and less reliable than local calls.
- Observability, testing, security, deployment, and data evolution become harder.
- Cross-service transactions usually become eventually consistent.
- Infrastructure and cognitive cost can exceed the business benefit.

## Real-world examples

- Checkout uses an orchestrated saga: create order, reserve inventory, authorize payment, then ship; failures release reservations and cancel the order.
- A ledger uses event sourcing for auditability and projections for statements, with strict schema evolution and reconciliation.
- Kubernetes provides DNS/service discovery and server-side routing while services use timeouts, bulkheads, and bounded retries.

## Interview Questions

1. Monolith, modular monolith, or microservices: what evidence changes your choice?
2. How do clean and hexagonal architecture differ from traditional layering?
3. Synchronous versus asynchronous communication: where does each belong?
4. CQRS versus event sourcing, and saga versus ACID/2PC?
5. How do timeout, retry, circuit breaker, bulkhead, and fallback interact?
6. **Interview tip:** explain the requirement, failure model, consistency boundary, operational cost, alternatives, and why the simplest option is insufficient.

## References

- [Google SRE: Addressing Cascading Failures](https://sre.google/sre-book/addressing-cascading-failures/)
- [AWS Builders' Library: Timeouts, Retries, and Backoff with Jitter](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/)
- [Microsoft: Microservices Architecture Style](https://learn.microsoft.com/azure/architecture/guide/architecture-styles/microservices)
- [Martin Fowler: CQRS](https://martinfowler.com/bliki/CQRS.html)
- [Martin Fowler: Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Microsoft: Saga Pattern](https://learn.microsoft.com/azure/architecture/reference-architectures/saga/saga)
- [Related: Messaging](../Messaging/README.md)
