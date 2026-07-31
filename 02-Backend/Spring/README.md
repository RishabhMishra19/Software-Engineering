# Spring

## Overview

Spring is a Java application ecosystem centered on dependency injection, modular configuration, data access, transactions, web development, security, messaging, and operations. Spring Boot adds opinionated defaults, auto-configuration, dependency management, embedded servers, and production features.

## Why do we need it?

Production services repeatedly need configuration, object lifecycle management, validation, transactions, authentication, observability, and infrastructure integration. Spring supplies compatible abstractions and conventions so teams can focus on domain behavior while retaining control over implementation.

Choose Spring when domain logic, transactions, security, long-term maintenance, or a broad Java ecosystem matter. A small utility, startup-sensitive function, or simple prototype may be better served by a lighter runtime.

## How does it work?

### Container and dependency injection

The application context discovers or registers bean definitions, constructs objects, resolves dependencies, and manages lifecycle. Prefer constructor injection: required dependencies remain explicit, instances can be immutable, and unit tests need no container. Setter injection suits optional dependencies; field injection hides dependencies and is best avoided.

Dependency injection is one implementation of inversion of control and does not inherently require a framework.

### Auto-configuration

Spring Boot evaluates the classpath, configuration properties, and existing beans to configure sensible defaults conditionally. Explicit application beans generally override defaults. Auto-configuration accelerates setup but engineers must understand which beans and settings are active.

### Layering and boundaries

A conventional flow is controller → application/service → repository. Controllers translate HTTP concerns; services enforce use cases and transaction boundaries; repositories handle persistence. Layering is useful when it protects responsibilities, but mechanical layers around trivial CRUD add ceremony. Organize code around cohesive business modules and keep module internals private.

### Data, transactions, and validation

Spring Data integrates repositories; Spring's transaction abstraction defines atomic boundaries. Request-shape validation belongs at the transport boundary, domain rules in services or domain objects, and final integrity constraints in the [database](../../01-Computer-Science/Database/README.md).

### Errors and operations

Centralized exception handling should map known failures to stable API errors and avoid exposing stack traces. Log an exception once at the handling boundary with structured context. Actuator exposes health, metrics, and management endpoints; secure and separate sensitive endpoints.

Long-running work can use schedulers, queues, or workflow engines. Production jobs need idempotency, bounded retries with backoff, dead-letter handling, monitoring, and graceful shutdown.

### Trade-offs

- A mature integrated ecosystem reduces integration risk but increases runtime and conceptual weight.
- Reflection, proxies, scanning, and an embedded server add startup and memory overhead.
- Framework abstractions improve consistency but can hide transaction, proxy, or lifecycle behavior.
- A modular monolith usually retains simpler deployment and debugging than premature microservices; extract services when independent ownership, deployment, or scaling is required.

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

## Interview Questions

1. **Why Spring Boot?** It combines Java with opinionated configuration and an integrated ecosystem for common production concerns.
2. **What is the difference between IoC and DI?** IoC is the broader transfer of control; DI supplies an object's dependencies externally.
3. **Why constructor injection?** It makes required dependencies explicit, supports immutability, and simplifies testing.
4. **How does auto-configuration work?** Conditional configuration reacts to the classpath, properties, and existing beans.
5. **Where should transactions begin?** Around cohesive application operations that must succeed or fail atomically.
6. **Why can `@Transactional` self-invocation fail?** A call within the same object may bypass the proxy that applies transaction advice.
7. **Spring MVC or WebFlux?** MVC fits blocking stacks; WebFlux fits end-to-end non-blocking workloads when its complexity is justified.

## Interview Tips

Explain Spring decisions in terms of domain complexity, team capability, runtime constraints, and operations. Demonstrate understanding beneath annotations: bean lifecycle, proxies, transaction propagation, thread usage, and failure handling.

## References

- [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/)
- [Spring Data](https://spring.io/projects/spring-data)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/reference/actuator/)
