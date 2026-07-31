# Backend Engineering

Backend systems expose capabilities, enforce business rules, protect data, and integrate infrastructure behind stable interfaces.

## Overview

Backend technology selection starts with the system's business and technical requirements, not a preferred language or framework. Mature stacks can all scale when the surrounding database, caching, asynchronous processing, architecture, and operations are sound. The useful question is: **which option best satisfies this system's constraints?**

Framework choice affects delivery speed, performance, scalability, team productivity, maintainability, ecosystem support, hiring, and long-term ownership. Evaluate expected traffic, product complexity, timeline, team size and expertise, compliance, ecosystem maturity, operational capabilities, and maintenance horizon.

## Topics

- [Spring](Spring/README.md) — the Spring ecosystem and production Java services.
- [Node.js](Node/README.md) — event-driven JavaScript and TypeScript services.
- [REST](REST/README.md) — resource-oriented HTTP APIs.
- [GraphQL](GraphQL/README.md) — schema-driven, client-selected API responses.
- [Authentication](Authentication/README.md) — verifying identities and managing credentials or sessions.
- [Authorization](Authorization/README.md) — deciding which authenticated principals may perform an action.

## Why do we need explicit decisions?

A startup validating an MVP and a regulated banking platform optimize for different outcomes. A capable framework that the team cannot operate may slow delivery more than a smaller, familiar stack. Conversely, optimizing only for initial speed can leave a growing system without consistent architecture, security, testing, or observability.

Ask:

- Does the ecosystem provide maintained authentication, persistence, validation, security, testing, and observability support?
- Can the stack scale horizontally and integrate caches, queues, background workers, and tracing?
- Is project structure predictable enough for onboarding and multi-team ownership?
- Is the talent pool practical, and can the team diagnose production behavior?
- Is added complexity solving a present requirement?

Performance should be measured against the real workload. Database queries, indexes, network calls, serialization, caching, and application design usually dominate framework overhead.

## How do we decide?

```text
Business requirements
        ↓
System complexity
        ↓
Team expertise
        ↓
Scalability and operational needs
        ↓
Ecosystem support
        ↓
Long-term maintenance
        ↓
Choose technology
```

The technology is the final decision, not the starting point.

### Cross-technology decision matrix

| Decision | Choose | When it is a strong fit | Main caution |
| --- | --- | --- | --- |
| Backend framework | Spring Boot | Enterprise applications, complex business rules, transactions, security, large teams, long-lived products | More startup, memory, and conceptual overhead |
| Backend framework | Express.js | Lightweight APIs, MVPs, rapid delivery, simple services, maximum Node.js flexibility | Production architecture and integrations must be selected and enforced |
| Backend framework | NestJS | Structured TypeScript services needing modules and dependency injection | More abstraction than a tiny API needs |
| Backend framework | FastAPI | Python, AI/ML, and data-centric services | Less natural for a Java-centered organization |
| Backend framework | ASP.NET Core | Teams and systems aligned with the Microsoft/C# ecosystem | Ecosystem fit should be deliberate |
| Backend framework | Quarkus | Cloud-native Java and startup-sensitive deployments | Less direct fit for teams deeply invested in Spring |
| Runtime | Java | Type safety, CPU-heavy work, mature enterprise tooling, complex long-term systems | Typically more ceremony and runtime weight |
| Runtime | Node.js | I/O-heavy APIs, real-time systems, rapid development, shared JavaScript/TypeScript skills | CPU-heavy work blocks an event loop unless isolated |
| API style | REST | CRUD, independent services, standard HTTP semantics, intermediary caching | Fixed representations can require extra calls or over-fetch |
| API style | GraphQL | Diverse client data needs, related graphs, frontend aggregation | Query cost, authorization, caching, and operations are more involved |
| Architecture | Monolith | MVP, small team, limited complexity, fastest delivery | Boundaries and whole-application scaling can become constraints |
| Architecture | Modular monolith | Growing product with identifiable domains but no distributed-systems requirement | Boundaries require discipline; modules are not independently deployable |
| Architecture | Microservices | Autonomous teams, independent deployment, distinct scaling or reliability needs | Network, data consistency, tracing, deployment, and infrastructure complexity |
| Dependency injection | Constructor injection | Required dependencies, immutability, explicit design, unit testing | Constructors expose excessive class responsibility—which is useful feedback |
| Dependency injection | Setter injection | Truly optional or replaceable dependencies | Objects may be temporarily incomplete or mutable |
| Dependency injection | Field injection | Generally avoid | Hidden dependencies, poor testability, and no immutability |
| Validation | API boundary | Required fields, shape, type, format, range, enum values | Does not replace domain validation |
| Validation | Service/domain | Business rules and state transitions | Must remain reusable outside HTTP |
| Validation | Database | Referential integrity, uniqueness, checks, and non-null guarantees | Constraint errors still need safe application-level mapping |
| Failure | `400 Bad Request` | Malformed input or validation failure | Do not use for authentication or authorization |
| Failure | `401 Unauthorized` | Missing or invalid authentication | Despite the name, it does not mean “authenticated but forbidden” |
| Failure | `403 Forbidden` | Authenticated principal lacks permission | Avoid leaking sensitive resource existence |
| Failure | `500 Internal Server Error` | Unexpected system failure | Log internal context, but never return internals |
| Logging | TRACE / DEBUG | Detailed diagnostics and development troubleshooting | Usually too noisy and costly for normal production operation |
| Logging | INFO | Meaningful normal business and lifecycle events | Avoid logging every method call |
| Logging | WARN | Unexpected but recoverable conditions | A warning should be actionable |
| Logging | ERROR | Failures requiring attention | Log an exception once at its handling boundary |
| API evolution | URI versioning | Visible, simple routing and documentation; most common public style | Duplicates route families and changes URLs |
| API evolution | Header versioning | Clean URLs and some internal APIs | Less visible in manual use |
| API evolution | Media-type negotiation | Advanced APIs preserving resource identifiers | Harder to understand and configure |
| API evolution | Query parameter | Simple experiments | Uncommon and generally weak for public API contracts |
| Processing | Synchronous | The caller requires the result and work is bounded | Long work ties up request capacity |
| Processing | Background job | Long-running, retryable, scheduled, batch, or non-critical work | Eventual consistency, retries, idempotency, and monitoring are required |

### Architecture trade-offs

| Feature | Monolith | Modular monolith | Microservices |
| --- | --- | --- | --- |
| Deployment | Single | Single | Multiple |
| Scaling | Whole application | Whole application | Per service |
| Communication | In-process | In-process across explicit module APIs | Network |
| Team autonomy | Low to medium | Medium | High when ownership is real |
| Operational complexity | Low | Low to medium | High |
| Infrastructure cost | Low | Low | High |
| Independent deployment | No | No | Yes |

Microservices solve organizational, independent-deployment, and independent-scaling problems; they do not automatically simplify an application. Monoliths can scale, and a modular monolith can provide a safer extraction path when service boundaries become justified.

## Best Practices

- Document the requirement, alternatives, trade-offs, and evidence behind a decision.
- Prefer the simplest architecture that satisfies current reliability and ownership needs.
- Measure the real workload before making generic performance claims.
- Treat team expertise, hiring, testing, monitoring, and incident response as design inputs.
- Revisit a decision when assumptions change; do not migrate because a technology is fashionable.

## Common Mistakes

- Asking for the universally “best” framework.
- Assuming a framework makes a system scalable.
- Choosing microservices before independent ownership or deployment is needed.
- Treating Node.js as incapable of scale or Java as always faster.
- Treating GraphQL as a replacement for REST or as automatically faster.
- Adding versions, layers, queues, or custom exceptions without a concrete requirement.

## Real-world examples

These are **illustrative scenarios**, not verified descriptions of any company's internal architecture:

- A small team ships an MVP as a monolith, then enforces catalog, ordering, and billing module boundaries as the product grows.
- A Java team chooses Spring Boot for transaction-heavy workflows, while a TypeScript team chooses Express for a small I/O-heavy integration service.
- Internal services expose cacheable REST resources, while a frontend-facing GraphQL layer composes customer and order views.
- A report endpoint accepts work synchronously, returns a job identifier, and lets durable workers generate the report.

## Interview Questions

1. Why choose Spring Boot instead of Express.js, NestJS, or FastAPI—and when would you make the opposite choice?
2. What are Java's advantages over Node.js, and when is Node.js the stronger fit?
3. When would you choose REST over GraphQL? What problems does GraphQL solve, and can both coexist?
4. Why might a modular monolith be a better starting point than microservices?
5. When should a company extract services, and what distributed-systems costs follow?
6. Why use layered architecture, dependency injection, constructor injection, and global exception handling?
7. Where should request, business, and integrity validation happen?
8. What belongs in production logs, and how is logging different from monitoring?
9. When should an API be versioned, and how should an old version be deprecated?
10. When should work move to a background job? How do retries, dead-letter queues, and idempotency interact?
11. How do you choose technology, evaluate trade-offs, and revise an architectural decision?
12. What would you investigate first in a slow backend, and how do you design for scalability and maintainability?

## Related foundations

- [Computer Science](../01-Computer-Science/README.md)
- [Networking](../01-Computer-Science/Networking/README.md)
- [Databases](../01-Computer-Science/Database/README.md)
- [Concurrency](../01-Computer-Science/Concurrency/README.md)

## Provenance

- **Source-derived:** framework-selection factors and process, framework/runtime/API/architecture comparisons, decision matrices, engineering principles, scenarios, misconceptions, and interview prompts were restored from `01-Backend.md`.
- **Editorial additions:** the unified matrix, explicit requirement-first workflow, and links to canonical topic guides organize the source material without changing its decisions.
- **Professional corrections:** absolute performance and scalability claims were made workload-dependent; microservices are framed as an organizational and operational trade-off; all company-like examples are explicitly illustrative rather than claims about verified internals.
