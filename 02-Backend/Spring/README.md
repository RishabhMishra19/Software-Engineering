# Spring

## Overview

Spring is a Java application ecosystem that acts like a workshop manager: instead of every component finding and building its own tools, a shared container creates components, connects their required collaborators, and manages their lifetimes. Technically, Spring centers on dependency injection, modular configuration, data access, transactions, web development, security, messaging, and operations. Spring Boot adds opinionated defaults, auto-configuration, dependency management, embedded servers, and production features.

**Prerequisites:** Java is a programming language and runtime platform. A class describes objects; an object contains state and behavior. A dependency is another object a component needs. A web request commonly arrives through Hypertext Transfer Protocol (HTTP), and an application programming interface (API) defines how software communicates. A database preserves durable state. Create, read, update, and delete (CRUD) names four common data operations. Input/output (I/O) means communication with external resources; a central processing unit (CPU) executes instructions. No prior Spring knowledge is assumed.

## Why do we need it?

Production services repeatedly need configuration, object lifecycle management, validation, transactions, authentication, observability, and infrastructure integration. Spring supplies compatible abstractions and conventions so teams can focus on domain behavior while retaining control over implementation.

Choose Spring when domain logic, transactions, security, long-term maintenance, or a broad Java ecosystem matter. A small utility, startup-sensitive function, or simple prototype may be better served by a lighter runtime.

## How does it work?

### Container and dependency injection

The application context discovers or registers bean definitions, constructs objects, resolves dependencies, and manages lifecycle. Prefer constructor injection: required dependencies remain explicit, instances can be immutable, and unit tests need no container. Setter injection suits optional dependencies; field injection hides dependencies and is best avoided.

Dependency injection (DI) is one implementation of inversion of control (IoC), the broader idea that creation and lifecycle control move outside the object. DI does not inherently require a framework.

### Auto-configuration

Spring Boot evaluates the classpath, configuration properties, and existing beans to configure sensible defaults conditionally. Explicit application beans generally override defaults. Auto-configuration accelerates setup but engineers must understand which beans and settings are active.

### Layering and boundaries

A conventional flow is controller → application/service → repository. Controllers translate HTTP concerns; services enforce use cases and transaction boundaries; repositories handle persistence. Layering is useful when it protects responsibilities, but mechanical layers around trivial CRUD add ceremony. Organize code around cohesive business modules and keep module internals private.

### Data, transactions, and validation

Spring Data integrates repositories; Spring's transaction abstraction defines atomic boundaries. Request-shape validation belongs at the transport boundary, domain rules in services or domain objects, and final integrity constraints in the [database](../../01-Computer-Science/Database/README.md).

For example, an order request reaches a controller, which checks that required fields have valid shapes. An order service checks stock and business rules inside a short transaction. A repository writes the order, while database constraints remain the final protection against concurrent invalid data. Spring commits only if the operation succeeds; a configured failure causes rollback.

### Errors and operations

Centralized exception handling should map known failures to stable API errors and avoid exposing stack traces. Log an exception once at the handling boundary with structured context. Actuator exposes health, metrics, and management endpoints; secure and separate sensitive endpoints.

Long-running work can use schedulers, queues, or workflow engines. Production jobs need idempotency, bounded retries with backoff, dead-letter handling, monitoring, and graceful shutdown.

### Trade-offs

- A mature integrated ecosystem reduces integration risk but increases runtime and conceptual weight.
- Reflection, proxies, scanning, and an embedded server add startup and memory overhead.
- Framework abstractions improve consistency but can hide transaction, proxy, or lifecycle behavior.
- A modular monolith usually retains simpler deployment and debugging than premature microservices; extract services when independent ownership, deployment, or scaling is required.

### Edge cases and production behavior

- Spring often applies transactions, security, and caching through proxy objects. A method calling another advised method on the same object can bypass that proxy.
- A transaction annotation is not a distributed rollback mechanism: an email, message, or remote payment does not automatically undo when the database rolls back.
- A singleton bean is shared by concurrent requests, so mutable request-specific fields inside it can race.
- Readiness and liveness have different meanings. Readiness controls whether traffic should arrive; liveness asks whether the process should be restarted.
- Production teams inspect startup time, heap memory, garbage collection, thread pools, connection pools, request latency, error rate, and dependency saturation before changing framework settings.

## Advantages

- Broad, interoperable ecosystem for security, data, validation, caching, messaging, and observability.
- Explicit architectural conventions for large codebases.
- Strong transaction and enterprise integration support.
- Dependency injection improves replacement, testing, and lifecycle management.
- Mature tooling, documentation, and Java interoperability.

## Limitations

- Steeper learning curve around the container, proxies, transactions, and security.
- Higher startup time and memory use than minimal frameworks in typical configurations.
- Auto-configuration can make behavior difficult to trace.
- Excessive annotations and abstractions can obscure control flow.
- Blocking stacks require careful capacity management for I/O-heavy workloads.

## Best Practices

- Use constructor injection and immutable configuration properties.
- Keep transaction boundaries at cohesive service operations and transactions short.
- Prefer feature modules with enforced boundaries over package-by-layer sprawl.
- Validate transport shape, domain invariants, and database constraints at their proper boundaries.
- Return consistent errors; never expose stack traces or secrets.
- Use structured logs with request or trace identifiers, and instrument latency, errors, saturation, and dependency health.
- Test business logic without the container; use focused slice and integration tests where framework behavior matters.

## Common Mistakes

- Self-invoking proxied methods and expecting transaction or security advice to run.
- Catching exceptions broadly and committing partial work.
- Placing business logic in controllers or repositories.
- Enabling unrestricted management endpoints.
- Logging passwords, tokens, personal data, or the same exception repeatedly.
- Assuming Spring Boot makes an application scalable without sound database and infrastructure design.

## Real-world examples

- A transactional order service validates stock, records an order, and writes an outbox event in one transaction.
- A modular monolith isolates catalog, ordering, and billing behind explicit module APIs.
- A consumer processes queue messages idempotently and sends permanent failures to a dead-letter queue.
- Actuator metrics feed an observability system while readiness checks protect traffic during startup.

## Spring Boot decision guide

Spring Boot is an opinionated layer over the Spring ecosystem for production Java applications. Its strength is the integration of Spring Security, Spring Data, Bean Validation, caching, scheduling, Actuator, Kafka, and Spring Cloud—not a claim that one framework feature makes a system scalable.

### Benefits and costs

- Convention, auto-configuration, and compatible modules reduce setup and integration work.
- The IoC container supports modular design, replacement, lifecycle management, and testing.
- Security, validation, transactions, caching, management, and configuration have mature support.
- Maven, Gradle, containers, Kubernetes, PostgreSQL, Redis, Kafka, Prometheus, and Grafana fit its common tooling ecosystem.
- Bean management, classpath scanning, reflection, proxies, metadata, auto-configuration, and an embedded server add memory, initialization, and startup cost.
- Engineers must understand bean lifecycles, proxy boundaries, transactions, configuration, and the active application context.
- Java and framework conventions generally require more structure than a minimal JavaScript or Python service.

Choose Spring Boot for long-lived products, complex business rules, security and transaction requirements, multi-developer systems, or a team invested in Java. Avoid defaulting to it for a throwaway prototype, tiny CRUD service, Python-centered AI/ML workload, highly startup-sensitive function, or a team without Java capability.

| Framework | Strong fit | Caution |
| --- | --- | --- |
| Spring Boot | Enterprise rules, integrated security and transactions, long-term ownership | Small prototypes and startup-sensitive deployments |
| Express.js | Lightweight APIs and rapid flexible delivery | Large systems need conventions and integrations |
| NestJS | Structured TypeScript backends | Requires TypeScript and adds framework abstraction |
| FastAPI | Python, artificial intelligence and machine learning (AI/ML), data-centric APIs | Different ecosystem from enterprise Java |
| Quarkus | Cloud-native Java and fast startup | Less direct fit for Spring-invested teams |

Spring Boot is not inherently slow, enterprise-only, or automatically scalable. Database access, network latency, application logic, caching, data design, and infrastructure usually matter more than framework overhead. It can fit a startup with Java expertise, but scale still requires sound architecture and operations.

## Layered and modular architecture

A conventional layered request path is:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

- **Controller:** translates HTTP requests and responses, validates request shape, and calls a use case. It should contain minimal business logic.
- **Service/application layer:** enforces business rules, coordinates repositories and external systems, and owns cohesive transaction boundaries.
- **Repository:** performs persistence operations and queries; it should not decide business policy.
- **Database:** stores data and provides final integrity guarantees through keys and constraints.

The same service logic can serve REST, GraphQL, scheduled work, and message consumers because it is independent of transport. Controllers can be API-tested, services unit-tested with test doubles, and repositories integration-tested.

Layering improves separation, maintainability, testability, reuse, and team collaboration. Its costs are boilerplate, ceremony for trivial CRUD, “anemic” pass-through services, and layer leakage when controllers bypass services. More layers are not automatically better, and layered architecture is not identical to MVC. Use layers where they protect meaningful responsibilities.

For application-level architecture:

- A **monolith** is one codebase and deployable unit: simple to build, run, debug, and scale as a whole, but potentially difficult to partition as teams and domains grow.
- A **modular monolith** remains one deployable unit while enforcing private internals and explicit module APIs. It preserves in-process calls and lower operational cost but cannot deploy modules independently.
- **Microservices** provide independent ownership, deployment, and scaling, but introduce network latency, partial failure, distributed data, service discovery, gateways, messaging, tracing, and higher operating cost.

Start with the simplest option that fits. Extract services when autonomous ownership, independent deployment, fault isolation, or materially different scaling is an existing requirement—not because microservices are fashionable.

## Dependency injection choices

Dependency injection supplies an object's dependencies externally; inversion of control is the broader transfer of lifecycle and creation control. DI can be manual, although Spring automates construction, resolution, scopes, and lifecycle.

### Constructor injection

Required dependencies arrive through the constructor. This makes them explicit, supports immutable fields, prevents partially initialized instances, and allows tests to instantiate the class without Spring.

### Setter injection

Setters can represent optional or replaceable dependencies, but introduce mutability and permit incomplete objects.

### Field injection

Annotation-injected fields reduce visible boilerplate but hide requirements, obstruct plain unit tests, and prevent final fields. Avoid it in normal application code.

| Property | Constructor | Setter | Field |
| --- | --- | --- | --- |
| Recommended | Yes | Sometimes, for optional dependencies | No |
| Required dependencies | Yes | No | No |
| Immutability | Supported | Not supported | Not supported |
| Plain unit testing | Excellent | Good | Poor |
| Hidden dependencies | No | No | Yes |

DI improves coupling, replacement, reuse, lifecycle management, and testability; it is not a performance optimization. Container-driven construction can also make object creation harder to trace, and a DI framework is unnecessary for a very small script.

## Validation strategy

Validation protects data integrity, security, user experience, and deeper code paths. It adds rule maintenance and a small processing cost, and duplication becomes a risk when the same rule is copied across layers.

### Validation layers

- **Client:** required fields, email shape, password feedback, and length checks improve user experience but are untrusted and bypassable.
- **API boundary:** Bean Validation such as `@Valid`, `@NotNull`, `@Email`, `@Size`, and `@Pattern` checks request shape, types, ranges, patterns, and enum values.
- **Service/domain:** rules such as unique email, minimum age, non-negative balance, stock availability, or “cannot cancel after shipment” belong with the use case or domain.
- **Database:** primary and foreign keys, unique and check constraints, and `NOT NULL` provide the final concurrency-safe integrity boundary.

| Validation | Recommended boundary |
| --- | --- |
| Required fields, format, and request structure | API |
| Business rules and transitions | Service/domain |
| Referential integrity | Database |
| Duplicate prevention | Service plus a database uniqueness constraint |

Database constraints complement rather than replace useful application errors. Authorization is different from validation: validation asks whether input is acceptable; authorization asks whether the principal may perform the action.

## Exception handling strategy

Centralized exception handling keeps controllers and services focused, produces predictable contracts, and prevents internal details from reaching clients. It requires a restrained exception hierarchy; broad catches, swallowed failures, and a custom exception for every condition make a system harder to diagnose.

### Exception categories

| Category | Examples | Typical response |
| --- | --- | --- |
| Request validation | Missing field, malformed payload, invalid email, negative quantity | `400 Bad Request` |
| Business conflict | Existing user, insufficient balance, shipped order, out-of-stock product | Depends on semantics, commonly `400`, `409`, or another documented `4xx` |
| Authentication | Missing, invalid, or expired credential | `401 Unauthorized` |
| Authorization | Authenticated principal lacks permission | `403 Forbidden` |
| Unexpected system failure | Database outage, external timeout, unavailable Redis/Kafka | `500 Internal Server Error` or a deliberate gateway/service status |

Throw meaningful domain or application failures where the rule is enforced. Catch only where recovery, translation, compensation, or added context is possible. Map failures globally, log once at the handling boundary, and never return stack traces, SQL, credentials, or sensitive implementation details.

An illustrative error contract:

```json
{
  "timestamp": "2026-07-21T10:15:30Z",
  "status": 400,
  "error": "Validation Failed",
  "message": "Email is required.",
  "path": "/api/users"
}
```

JavaScript Object Notation (JSON) is the text data format shown above. The exact schema should use stable machine-readable error codes and field details where useful. Exceptions are not normal control flow, and they should usually propagate to the centralized handler when the current layer cannot recover.

## Logging strategy

Logging records significant application events for diagnosis, production visibility, audit analysis, and incident reconstruction. It costs CPU, I/O, indexing, retention, and human attention, so useful context matters more than volume.

| Level | Intended use | Illustrative events |
| --- | --- | --- |
| TRACE | Very detailed temporary diagnostics | Framework or internal flow details |
| DEBUG | Developer troubleshooting | Intermediate state needed during diagnosis |
| INFO | Normal meaningful lifecycle or business events | Application start, order created, payment completed |
| WARN | Unexpected but recoverable conditions | Retry, deprecated API, high latency, optional configuration absent |
| ERROR | Failure requiring attention | Database outage, external failure, unhandled exception |

Use structured fields such as request identifier (ID), trace or correlation ID, safe user ID, order ID, and service name. Never log passwords, JSON Web Tokens (JWTs), API keys, payment-card data, session identifiers, or unnecessary personal information; mask or omit sensitive values.

Logging does not replace monitoring. Logs record events, while metrics and alerts track latency, traffic, errors, saturation, CPU, memory, availability, and queue health. More logs are not automatically better, DEBUG is normally inappropriate as a blanket production level, and an exception should not be logged independently by every layer through which it propagates.

## Background jobs

Background jobs execute outside the request-response cycle so the API can respond before long-running, scheduled, retryable, or batch work completes.

### Appropriate uses

- Email, Short Message Service (SMS), and notifications.
- Uploaded-file, image, or video processing.
- Reports, imports, exports, and large data synchronization.
- Scheduled cleanup, invoice generation, and cache refresh.
- Long-running workflows whose result need not be immediate.

Keep work synchronous when the caller cannot proceed without the result or the operation is fast and bounded. Asynchrony improves request responsiveness, not the amount of work required.

### Patterns

- **Fire-and-forget:** enqueue a welcome email, notification, or audit event and return.
- **Scheduled:** run daily reports, cleanup, invoices, or refreshes at defined times.
- **Retry:** retry transient network, external API, or database failures with bounded exponential backoff and jitter.
- **Batch:** process comma-separated values (CSV) imports, millions of records, or bulk campaigns in controlled chunks.
- **Workflow:** persist long-running multi-step business state, timers, compensation, and human interaction.

| Technology | Typical fit |
| --- | --- |
| Cron | Simple host-level schedules |
| Spring Scheduler | Simple in-application schedules |
| Quartz | Persistent and advanced scheduling |
| Redis + BullMQ | Node.js job queues |
| RabbitMQ | Reliable message queues and routing |
| Kafka | Durable event streams and event-driven processing |
| Temporal | Long-running durable workflows |

Production jobs require durable submission where loss is unacceptable, idempotent handlers, bounded retries, dead-letter handling, timeout and cancellation policy, queue-backlog and age metrics, alerting, graceful shutdown, and a way to inspect or replay failures. Jobs can fail and require more—not less—monitoring.

## Interview Questions

1. **Why Spring Boot?** It combines Java with opinionated configuration and an integrated ecosystem for common production concerns.
2. **What is the difference between IoC and DI?** IoC is the broader transfer of control; DI supplies an object's dependencies externally.
3. **Why constructor injection?** It makes required dependencies explicit, supports immutability, and simplifies testing.
4. **How does auto-configuration work?** Conditional configuration reacts to the classpath, properties, and existing beans.
5. **Where should transactions begin?** Around cohesive application operations that must succeed or fail atomically.
6. **Why can `@Transactional` self-invocation fail?** A call within the same object may bypass the proxy that applies transaction advice.
7. **Spring MVC or WebFlux?** Spring Model-View-Controller (MVC) fits conventional blocking stacks; Spring WebFlux fits end-to-end non-blocking workloads when its complexity is justified.
8. **Why Spring Boot instead of Express.js, NestJS, or FastAPI?** Choose the integrated Java ecosystem for complex long-lived systems; choose a lighter or language-specific alternative when its workload and team fit are stronger.
9. **Why use layered architecture, and why should controllers not access repositories directly?** Layers protect transport-independent business rules and persistence boundaries; bypassing services couples HTTP directly to data access.
10. **Where should business logic live?** In services or domain objects, independent of controllers and repositories.
11. **Can layered architecture exist inside microservices?** Yes; deployment topology and internal code organization are separate decisions.
12. **Why is constructor injection preferred?** Required dependencies are explicit, immutable, and easy to test without a container.
13. **Where should validation happen?** At the client for feedback, API for shape, service/domain for business rules, and database for final integrity.
14. **Why are database constraints still required?** Application checks can be bypassed and race; the database is the final concurrent authority.
15. **Why use global exception handling?** It centralizes safe, consistent translation and prevents repeated controller-level `try`/`catch`.
16. **What belongs in an error response?** Stable code/type, HTTP status, safe message, timestamp, path or instance, correlation metadata, and optional field errors—never internals.
17. **What should be logged in production?** Meaningful operational and business events, warnings, and failures with safe structured context.
18. **Logging versus monitoring?** Logs preserve events; monitoring uses metrics, checks, and alerts to track system health.
19. **When should background jobs be used?** For work that may complete asynchronously and benefits from isolation, retries, scheduling, or batching.
20. **How are failed jobs handled?** Idempotency, bounded backoff, dead-letter storage, monitoring, alerting, and deliberate replay.
21. **What is a dead-letter queue?** A destination for messages that cannot be processed after policy-defined attempts.
22. **How do you make a job idempotent?** Use a stable operation key, atomic state transition or deduplication record, and side effects designed for safe replay.

## Interview Tips

Explain Spring decisions in terms of domain complexity, team capability, runtime constraints, and operations. Demonstrate understanding beneath annotations: bean lifecycle, proxies, transaction propagation, thread usage, and failure handling.

## References

- [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/)
- [Spring Data](https://spring.io/projects/spring-data)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/)

## Provenance

- **Source-derived:** Spring Boot selection and alternatives, runtime overhead, layered/modular architecture, DI modes and matrix, validation layers, exception categories and JSON example, logging levels and practices, background-job patterns and technologies, misconceptions, scenarios, and interview questions were restored from `01-Backend.md`.
- **Editorial additions:** existing Spring-specific proxy, transaction, Actuator, module-boundary, observability, and job-reliability guidance was retained and used to connect the source chapters into one production-oriented guide.
- **Professional corrections:** business exceptions are not universally `400`; system dependency failures may warrant deliberate `5xx` gateway/service statuses; setters are limited to genuinely optional dependencies; retries are bounded and idempotent; brand and product names identify technologies only, while scenarios are illustrative rather than verified company internals.
