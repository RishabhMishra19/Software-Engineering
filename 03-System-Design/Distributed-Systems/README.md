# Distributed Systems

## Overview

A distributed system coordinates independent processes over unreliable networks. It gains independent scaling, deployment, and fault isolation, but must handle partial failure, concurrency, latency, and consistency explicitly. Start with the simplest architecture that meets present requirements; distribution is a trade, not an automatic upgrade.

Think of several restaurant branches fulfilling one brand's orders. Each branch can keep working or fail independently, travel between branches takes time, and two branches may briefly hold different inventory counts. Coordination can increase total capacity and geographic reach, but it cannot make distance or partial failure disappear.

## Why do we need it?

Distribution is justified when one deployable unit or datastore cannot meet availability, geographic, scaling, data-sovereignty, or team-autonomy needs. A modular monolith often preserves clear domain boundaries with far less operational cost and is a sound evolution point before microservices.

## Beginner vocabulary

- A **node** is one participating process, machine, or device. A remote call crosses a network; unlike a local function call, its request, response, or both can be lost or delayed.
- **Latency** is the duration of one operation. **Throughput** is completed work per unit of time. A system can process many requests overall while some individual requests remain slow.
- **Availability** is whether the system can perform its promised work when requested.
- **Consistency** defines which values operations may observe after a write. **Strong consistency** makes operations behave as if there were one current copy; **eventual consistency** permits temporary disagreement but requires copies to converge if updates stop.
- **Replication** keeps copies of data or services on multiple nodes. It helps read scale and recovery, but replicas can lag and failover can be ambiguous.
- A **network partition** separates nodes that are still running but cannot communicate. **Data partitioning**, also called sharding, deliberately divides records among owners.
- A **broker** stores and routes messages. A **cache** stores a faster temporary copy. A **load balancer** selects a healthy service instance. A **rate limit** restricts admitted work over time.

Abbreviations used below are: **create, read, update, and delete (CRUD)**; **command query responsibility segregation (CQRS)**; **atomicity, consistency, isolation, and durability (ACID)**; **two-phase commit (2PC)**; **application programming interface (API)**; **Domain Name System (DNS)**; **Transport Layer Security (TLS)**; **representational state transfer (REST)**; **command-line interface (CLI)**; **user interface (UI)**; **minimum viable product (MVP)**; **customer relationship management (CRM)**; **enterprise resource planning (ERP)**; **human resources (HR)**; **human resource management system (HRMS)**; **learning management system (LMS)**; **software as a service (SaaS)**; **short message service (SMS)**; **uniform resource locator (URL)**; **site reliability engineering (SRE)**; and **Amazon Web Services (AWS)**.

## Mental model map

- **Layered architecture** resembles an office with a reception desk, specialists, and records room; requests move through assigned responsibilities. **Clean architecture** protects the business rulebook at the center from tools around it. **Hexagonal architecture** resembles a universal socket whose adapters connect replaceable external tools.
- A **monolith** is one restaurant under one roof. A **modular monolith** adds enforced kitchen stations while retaining one building. **Microservices** are separately operated branches that must coordinate over roads and phones.
- **Synchronous communication** is a live phone call whose caller waits. **Asynchronous communication** is leaving a tracked ticket. **Event-driven architecture** is a bulletin announcing a fact so several teams can react.
- An **API gateway** is the public reception desk. **Service discovery** is the current staff directory. A **load balancer** is the dispatcher choosing one listed worker.
- A **timeout** is a stopwatch, a **retry** is another bounded attempt, a **circuit breaker** is an electrical fuse that stops repeated harm, a **bulkhead** is a ship compartment that contains flooding, and a **fallback** is an emergency reduced menu.
- **CQRS** resembles separate order-entry and read-only display counters. **Event sourcing** resembles a bank ledger whose entries, not a rewritten balance, are the record. A **saga** resembles a multi-company trip booking whose reservations need explicit cancellations when a later step fails.

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

**Concrete checkout request and data flow**

1. A client sends `Place order` through a gateway and load balancer to an order node.
2. The order node validates the request, writes a pending order in its owned database, and atomically records an outbox event.
3. A relay publishes the event to a broker. Inventory and payment nodes consume it and commit their own local transactions.
4. An orchestrator records each completed step. The client reads workflow status from a query model, which may lag briefly.
5. If payment times out, the outcome is unknown rather than automatically failed. The system queries by idempotency key before retrying.
6. If payment is declined, compensating commands release inventory and cancel the order. Reconciliation finds workflows that remain stuck.

This flow exists because one database transaction cannot safely cover independently owned services without extra coordination. Its benefits are ownership and independent scaling; its costs are temporary inconsistency, duplicate messages, more operational state, and compensation for failures.

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
   **Answer guidance:** begin with team and domain boundaries, deployment cadence, scaling asymmetry, and availability needs; explain why a simpler deployable unit is or is not enough.
2. How do clean and hexagonal architecture differ from traditional layering?
   **Answer guidance:** layering organizes responsibilities, clean architecture constrains dependency direction, and hexagonal architecture makes external interactions explicit ports and adapters; discuss useful boundaries versus boilerplate.
3. Synchronous versus asynchronous communication: where does each belong?
   **Answer guidance:** use synchronous calls for immediate decisions and asynchronous messages for deferred, bursty, or fan-out work; then cover timeout, duplicate, ordering, and user-feedback behavior.
4. CQRS versus event sourcing, and saga versus ACID/2PC?
   **Answer guidance:** separate model choice from persistence choice, then compare one-owner transactions, coordinated commits, and compensating workflows by consistency and availability needs.
5. How do timeout, retry, circuit breaker, bulkhead, and fallback interact?
   **Answer guidance:** start with an end-to-end deadline, retry only safe transient failures, fail fast during repeated failure, isolate resources, and define an honest degraded result.
6. **Interview tip:** explain the requirement, failure model, consistency boundary, operational cost, alternatives, and why the simplest option is insufficient.

## References

- [Google SRE: Addressing Cascading Failures](https://sre.google/sre-book/addressing-cascading-failures/)
- [AWS Builders' Library: Timeouts, Retries, and Backoff with Jitter](https://aws.amazon.com/builders-library/timeouts-retries-and-backoff-with-jitter/)
- [Microsoft: Microservices Architecture Style](https://learn.microsoft.com/azure/architecture/guide/architecture-styles/microservices)
- [Martin Fowler: CQRS](https://martinfowler.com/bliki/CQRS.html)
- [Martin Fowler: Event Sourcing](https://martinfowler.com/eaaDev/EventSourcing.html)
- [Microsoft: Saga Pattern](https://learn.microsoft.com/azure/architecture/reference-architectures/saga/saga)
- [Related: Messaging](../Messaging/README.md)

## Source coverage supplement

### Layered, clean, and hexagonal styles

```text
Client → Controller Layer → Service Layer → Repository Layer → Database
```

Layering offers separation of concerns, familiar framework conventions (Spring Boot, ASP.NET, and Django), reusable services, and independently testable business logic. Its downward dependencies, call overhead, oversized service layers, and logic leakage become liabilities without discipline. It fits CRUD APIs, inventory/HR/learning-management systems, banking portals, and admin dashboards. The number of layers is not fixed; controllers should handle transport and validation, repositories data access, and services business rules. It can scale and can be used inside microservices.

```text
Frameworks & Drivers (Database, UI, External APIs)
                         ↓
                Interface Adapters
                         ↓
                 Application Layer
                         ↓
                    Domain Layer
```

Clean architecture’s dependency rule points source dependencies inward. It keeps domain rules independent of frameworks and infrastructure, improving testability, replaceability, maintenance, and flexibility at the cost of interfaces, boilerplate, initial complexity, and a learning curve. It is suited to evolving enterprise domains such as banking, insurance, ERP, healthcare, and compliance—not every small CRUD application. It neither eliminates databases nor replaces layering and is not framework-specific.

```text
                  Web API
                     │
               REST Adapter
                     │
              ┌─────────────┐
              │ Input Port  │
              └─────────────┘
                     │
              Business Logic
                     │
              ┌─────────────┐
              │ Output Port │
              └─────────────┘
              /      |      \
     Database   Message Queue  External API
      Adapter      Adapter        Adapter
```

Hexagonal architecture defines meaningful input/output ports at application boundaries and adapters for databases, queues, APIs, UIs, REST, CLI, and messaging. It supports multiple clients and integrations, mock adapters, and technology replacement, with added abstraction and boilerplate. Payment gateways, banking, e-commerce, logistics, and integration platforms are representative uses. Ports are interfaces, not network ports; not every class needs one; adapters translate and should not own domain rules. Clean and hexagonal ideas can complement one another and can coexist with internal layers.

| Style | Best fit |
| --- | --- |
| Layered | Familiar small-to-medium enterprise CRUD |
| Clean | Large, evolving business-driven domain |
| Hexagonal | Integration-heavy system with replaceable boundaries |
| Event-driven | Distributed reactions and loose temporal coupling |

### Monolith, modular monolith, and microservices

| Feature | Monolith | Modular monolith | Microservices |
| --- | --- | --- | --- |
| Deployment | Single | Single | Independent |
| Scaling | Whole application | Whole application | Individual services |
| Development complexity | Low | Moderate | High |
| Operational complexity | Low | Low | High |
| Team independence | Low | Moderate | High |
| Fault isolation | Low | Moderate | High |
| Network calls | None between modules | None between modules | Yes |
| Technology flexibility | Low | Low | High |

A monolith is simple to develop, debug, deploy, and operate for small teams, MVPs, blogs, portfolios, internal tools, and small SaaS products, but can become tightly coupled and must deploy and scale together. A modular monolith keeps one deployment and in-process calls while enforcing boundaries; it suits CRM, ERP, HRMS, and medium SaaS, but needs discipline and cannot scale modules independently. Microservices add independent deployment, scaling, technology, ownership, and fault isolation for large domains and multiple autonomous teams, but add latency, discovery, observability, consistency, infrastructure cost, and distributed failure.

```text
Monolith
   ↓
Modular Monolith
   ↓
Microservices
```

Evolution should follow evidence, not trends. Large applications need not be microservices; monoliths can scale vertically and horizontally; remote calls are slower than in-process calls; and a modular monolith is more than tidy folders—it enforces boundaries.

### Communication and event-driven architecture

| Feature | Synchronous | Asynchronous |
| --- | --- | --- |
| Response | Immediate | Deferred |
| Coupling | Request-time | Temporal decoupling |
| Caller-perceived latency | Waits for completion | Returns before completion |
| Fault tolerance | Downstream availability is on path | Broker/buffer can bridge outages |
| Complexity | Lower | Higher |
| User feedback | Immediate | Usually delayed |

Synchronous calls suit login, authentication, profiles, product search, and payment authorization when immediate feedback or strong request-time consistency is needed. They are easy to understand and handle errors for, but create latency, availability coupling, and cascading-failure risk. Asynchronous calls suit email/SMS, image processing, reports, order processing, and analytics; they improve burst handling and loose coupling, but add eventual consistency, retries, and debugging complexity. Asynchronous does not mean total processing is faster or failures disappear, and scalable systems commonly combine both.

```text
Order Service
      │ publishes
      ▼
Order Created Event
      │
 ┌────┴─────────┐
 ▼              ▼
Inventory     Notification
 Service         Service
                  │
                  ▼
             Email Service
```

An event is a fact that already happened. Event-driven architecture enables scalable fan-out, extensibility, fault isolation, and long-running workflows such as orders, payments, notifications, auditing, analytics, and recommendations. Costs include invisible flows, eventual consistency, duplicates, event versioning, and shared-contract coupling. Authentication and profile reads may remain synchronous; events do not provide immediate consistency and are one kind of message, not a synonym for every message.

### API gateway and service discovery

```text
           Client
              │
              ▼
        API Gateway
      ┌─────┼─────┐
      ▼     ▼     ▼
 User  Order Payment
Service Service Service
```

An API gateway gives clients one endpoint and may centralize routing, authentication/authorization, TLS termination, rate limiting, validation, aggregation/transformation, logging, and monitoring. It can hide internal services and simplify mobile, banking, SaaS, and e-commerce clients. It also adds latency, operations, bottleneck and availability risks; deploy redundantly and keep business logic in services. Small systems can use direct access; a backend-for-frontend can serve distinct client needs. A gateway neither replaces a load balancer nor guarantees better performance.

```text
          API Gateway
               │
               ▼
        Service Registry
               │
     ┌─────────┼─────────┐
     ▼         ▼         ▼
 User       Order     Payment
Service     Service    Service
```

Instances register or are represented in a registry so callers can locate changing healthy endpoints without hardcoded addresses.

```text
Client → Service Registry → Service Instance
```

Client-side discovery lets the caller query and select (for example Eureka or Consul).

```text
Client → Load Balancer / API Gateway → Service Registry → Service Instance
```

Server-side discovery delegates selection to infrastructure (for example Kubernetes Services; an AWS load balancer can participate in server-side routing). Discovery fits dynamic containers, clouds, and large microservice systems and improves scaling, fault avoidance, and configuration, but adds registry availability, health, deregistration, and synchronization concerns. Static or DNS-based discovery is often enough for smaller or cloud-native cases. Discovery identifies candidates; balancing selects one; it does not replace a gateway, and a shared registry—not one per service—is typical.

**Monoliths do not need service discovery.** A monolithic application typically runs as a single process with in-process calls, so there are no independently moving service instances to locate. Service discovery becomes relevant when multiple deployable services scale, fail, and relocate independently.

**API gateway versus reverse proxy.** Both can sit in front of backend servers and terminate TLS, but they solve different problems:

| Concern | Reverse proxy | API gateway |
| --- | --- | --- |
| Primary role | Forward requests to upstream servers | Provide a unified client entry point for multiple backend services |
| Typical features | Routing, TLS termination, load distribution | Routing plus API-management concerns such as authentication, authorization, rate limiting, request validation, aggregation, and transformation |
| Business logic | Should not contain domain rules | Should not contain domain rules |
| Relationship | A simple reverse proxy may only forward traffic | Often implemented as a specialized reverse proxy or edge component with richer API policies |

A reverse proxy forwards traffic; an API gateway adds client-facing API policy and multi-service orchestration on top of that edge role. Neither replaces a load balancer, and neither should own business logic.

### Resilience patterns

Retry handles transient network, database-connection, or external-API failures; use limits, exponential backoff, and randomized jitter, and never retry validation failures. Timeouts bound waiting and should reflect the operation and end-to-end deadline. Bulkheads use separate thread pools, connection pools, quotas, or resources. Fallbacks return cached/default data, friendly errors, or disable nonessential features.

| Pattern | Solves |
| --- | --- |
| Retry | Temporary failure |
| Timeout | Hanging request |
| Bulkhead | Resource isolation |
| Fallback | Graceful degradation |
| Circuit breaker | Repeated downstream failure |

```text
Client → Retry → Timeout → Circuit Breaker → Fallback → Service
```

These patterns improve availability, fault isolation, recovery, and user experience but add code, telemetry, tuning, and failure modes. Longer timeouts do not improve reliability; fallback data may be stale; bulkheads limit rather than prevent failures; production systems normally combine patterns.

```text
Client
   │
   ▼
Circuit Breaker
   ├──────────────► Service (Healthy)
   └──────────────► Fallback (Unhealthy)
```

A circuit protects calls to APIs, payment gateways, databases, microservices, and third parties. **Closed** forwards normally:

```text
Client → Service
```

**Open** fails fast:

```text
Client → Circuit Open → Fallback
```

**Half-open** admits a limited probe after a wait:

```text
          Failures
Closed --------------► Open
   ▲                     │
   │                     │ Wait interval
   │                     ▼
   └──────── Half-open ◄─┘
          Success
```

Circuit breakers reduce cascading failures and wasted resources and permit recovery, but thresholds can open too early or recover too late. They do not prevent failures or replace retries, are unnecessary for every local call, and an open state normally signals self-protection. Combine them with timeouts, bounded backoff, sensible thresholds, state-transition monitoring, and an explicit fallback.

### CQRS

```text
                Client
                   │
          ┌────────┴────────┐
          ▼                 ▼
     Command API       Query API
          │                 │
          ▼                 ▼
    Write Model       Read Model
          │                 │
          └──────┬──────────┘
                 ▼
              Database(s)
```

Commands such as create user, place order, update profile, and cancel booking validate rules and change state; queries such as profile, orders, product search, and dashboards are read-only and optimized for retrieval.

```text
Client ─────────────► Command API
   │                       │
   │                 Update database
   │                       │
   │                  Publish event
   ▼                       ▼
Query API ◄──────── Update read model
   │
   ▼
Return optimized data
```

CQRS independently optimizes and scales reads/writes and permits denormalized projections, but synchronization, infrastructure, maintenance, and eventual consistency cost more. It fits complex, read-heavy e-commerce, banking, dashboards, orders, and inventory when models genuinely differ; avoid it for small balanced CRUD systems. CQRS separates models, not necessarily databases, does not require event sourcing, is not appropriate for every service, and does not automatically improve performance.

### Event sourcing

```text
Create Account
       │
Deposit ₹1000
       │
Withdraw ₹200
       │
Deposit ₹500
       │
───────────────
Current Balance = ₹1300
```

Traditional CRUD stores only the current `Balance = ₹1300`; event sourcing retains `Account Created`, `Deposit ₹1000`, `Withdraw ₹200`, and `Deposit ₹500` as the source of truth. This yields audit history, traceability, replay, integration events, and flexible projections for ledgers, trading, insurance, orders, and regulated systems. It adds event-version compatibility, storage growth, event-centric design, and replay cost.

```text
Events 1 → 1000
        │
     Snapshot
        │
Events 1001 →
```

Snapshots reduce reconstruction time. Choose event sourcing when history, compliance, replay, or multiple projections justify it; avoid it for ordinary CRUD where history has little value.

| CQRS | Event sourcing |
| --- | --- |
| Separates reads and writes | Persists history as immutable events |
| Optimizes workload models | Optimizes traceability |
| Can use optional read models | Uses an immutable event log |
| Can exist without event sourcing | Can exist without CQRS |

```text
Commands → Event Store → Publish Events → Read Model
```

Events are immutable; corrections are new events. Event sourcing changes persistence rather than replacing databases, and query databases are common.

### Saga

```text
Place Order → Order Service → Reserve Inventory → Payment Service → Shipping Service
```

If payment fails:

```text
Order Cancelled → Cancel Inventory Reservation
```

A saga splits a cross-service business transaction into local transactions and compensating actions. It avoids global locks, supports service data ownership, and fits e-commerce checkout, travel, food delivery, banking workflows, and insurance claims, but accepts temporary inconsistency and requires tracing/correlation, careful workflow design, and feasible compensation.

Choreography has no central coordinator:

```text
Order Created → Inventory Reserved → Payment Completed → Shipment Created
```

It is loosely coupled and easy to extend but long event chains are hard to understand and monitor. Orchestration centralizes flow:

```text
           Orchestrator
          /     |      \
         ▼      ▼       ▼
     Order  Inventory Payment
                     │
                     ▼
                 Shipping
```

It improves visibility and debugging but adds a highly available component and potential bottleneck.

| Aspect | Choreography | Orchestration |
| --- | --- | --- |
| Control | Distributed | Centralized |
| Communication | Events | Commands |
| Coupling | Lower | Higher |
| Workflow visibility | Lower | Higher |
| Large-flow complexity | Harder | Easier to manage |

```text
Create Order ✓
Reserve Inventory ✓
Payment Failed ✗
Compensation:
Release Inventory
Cancel Order
```

Compensation is a new business operation that restores a valid state, not necessarily a database rollback. Avoid saga when one service/database can use ACID or immediate cross-step consistency is mandatory. Saga does not guarantee ACID, every microservice does not need one, choreography is not universally superior, and saga does not require event sourcing.

| Alternative | Best fit |
| --- | --- |
| ACID transaction | One database owner |
| Two-phase commit | Narrow cases requiring coordinated strong consistency and accepting its costs |
| Saga | Long-running workflows across independently owned service data |

## Expanded interview questions

1. **Where should business logic live in a layered design?** Keep domain rules
   in the domain or application layer. Transport, database, and framework
   layers translate or persist data but should not become the only owners of
   business rules.
2. **What is the clean architecture dependency rule?** Source-code
   dependencies point inward toward stable business policy. Inner policy does
   not import web frameworks, databases, or vendor clients.
3. **What are ports and adapters?** A port is an application-owned interface
   for an external capability. An adapter implements that port for one database,
   message broker, or vendor.
4. **Why are modular monoliths popular?** They provide one simple deployment
   while still enforcing domain boundaries. Teams can delay distributed-system
   cost until independent deployment or scaling is justified.
5. **Are microservices an architecture style or deployment strategy?** They
   involve both. The design separates business capabilities and data ownership;
   the deployment runs those capabilities as independent network services.
6. **How do you version event schemas and handle duplicates?** Make changes
   backward compatible, include a schema version, tolerate old fields, and use
   stable event or operation IDs for idempotent processing.
7. **How does an API gateway differ from a reverse proxy?** Both can forward
   traffic. A gateway also applies API-level policy such as authentication,
   quotas, aggregation, and protocol translation.
8. **Can services use discovery without a gateway?** Yes. A client, sidecar,
   platform service, or load balancer can resolve a service name and choose a
   healthy instance directly.
9. **Why are retries unsafe without timeouts?** A call can wait indefinitely,
   consuming resources before a retry policy can act. Set one end-to-end
   deadline and ensure each attempt fits inside it.
10. **How do you configure a circuit-breaker threshold?** Use measured error
    rate, latency, traffic volume, and recovery behavior. Require a minimum
    sample size, open for a bounded period, then allow limited half-open probes.
11. **Can CQRS use one database?** Yes. Separate command and query models can
    share one database when independent storage or scaling is unnecessary.
12. **What are event-store snapshots?** A snapshot stores derived state at a
    known event position so recovery replays only later events. It is an
    optimization, not a replacement for the event log.
13. **Can a saga work without event sourcing?** Yes. A saga coordinates local
    transactions and compensations; each service may store ordinary current
    state rather than an event-sourced history.

## Professional correction

- Clean architecture does not inherently mean “medium-sized,” and hexagonal architecture is not uniquely “more testable”; select them by dependency and integration boundaries.
- Microservices do not automatically provide high availability. Independent deployment and fault isolation help only with redundant capacity, sound data ownership, and operations.
- An event publisher remains coupled to event meaning and schema even when it does not know consumers.
- Kubernetes service discovery is normally DNS plus virtual/service routing; describing every instance as explicitly self-registering with an application-visible registry is only one implementation model.
- A saga is not a replacement for all distributed consistency, and compensation may be impossible or only approximate for irreversible real-world actions.

## Provenance

- **Source-derived:** Architecture-style definitions, all source diagrams, comparisons, advantages, limitations, examples, alternatives, misconceptions, and interview prompts in this supplement come from `03-Architecture.md`, including the monolith/service-discovery misconception and the API gateway versus reverse proxy distinction under `# 10. API Gateway` and `# 11. Service Discovery`.
- **Editorial:** The opening production guidance, references, cross-links, wording consolidations, and `Professional correction` add operational precision and remove exact repetition.
