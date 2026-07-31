# Node.js

## Overview

Node.js is a JavaScript runtime built on V8. It combines a single JavaScript event loop with operating-system asynchronous I/O, a libuv worker pool for selected operations, and optional worker threads or child processes for parallel work. Frameworks such as Express, Fastify, and NestJS add HTTP and application structure.

## Why do we need it?

Node.js is effective for network services that coordinate many concurrent I/O operations, especially when teams use JavaScript or TypeScript across the stack. Its small core and package ecosystem support rapid delivery and flexible architecture.

Choose it for I/O-heavy APIs, gateways, streaming, and real-time systems when the team can establish production conventions. CPU-heavy processing, strict latency isolation, or a requirement for an integrated enterprise stack may favor another runtime or separate workers.

## How does it work?

The event loop runs JavaScript callbacks and advances queued work through phases. Network I/O is generally handled through non-blocking operating-system facilities. Some filesystem, DNS, compression, and cryptographic operations use libuv's worker pool. Promise continuations and `process.nextTick` use high-priority queues that can starve normal I/O when abused.

CPU-bound JavaScript blocks the event loop. Partition such work, move it to worker threads or separate services, or use native implementations. Multiple processes can use multiple cores and improve fault isolation.

Minimal frameworks provide routing and middleware while leaving validation, dependency injection, logging, security, and project structure to the application. This flexibility speeds small systems but demands explicit conventions as the codebase grows. Structured frameworks trade some simplicity for modules and dependency injection.

Background work should use durable queues when it must survive process failure. Handlers must be idempotent, retries bounded, and queue lag monitored.

### Trade-offs

- One language across client and server reduces context switching but does not remove backend-specific expertise.
- Non-blocking I/O supports many connections; event-loop blocking creates broad latency spikes.
- A large package ecosystem accelerates delivery but increases supply-chain and upgrade exposure.
- Minimal frameworks maximize choice; opinionated frameworks improve consistency for larger teams.
- TypeScript improves static checks but does not validate untrusted runtime input.

## Advantages

- Efficient orchestration of I/O-bound workloads.
- Fast startup and a lightweight deployment model.
- JavaScript and TypeScript ecosystem reuse.
- Strong support for HTTP, streaming, WebSockets, and tooling.
- Flexible framework and architectural choices.

## Limitations

- CPU-heavy work can block all requests on an event-loop thread.
- Dynamic runtime behavior still requires input validation and disciplined error handling.
- Minimal frameworks do not supply a production architecture.
- Deep dependency trees increase maintenance and security work.
- Unhandled promise rejections, leaked handles, and unbounded queues can destabilize a process.

## Best Practices

- Use maintained Node.js releases and pin dependencies with a lockfile.
- Prefer TypeScript for substantial services and validate every trust boundary.
- Keep request handlers non-blocking; monitor event-loop delay.
- Set timeouts, body limits, connection limits, and graceful-shutdown behavior.
- Use structured logs and propagate request or trace context.
- Bound concurrency and queues; apply backpressure to streams.
- Separate durable jobs from the web process and make consumers idempotent.

## Common Mistakes

- Calling synchronous filesystem or cryptographic APIs in request paths.
- Assuming asynchronous code automatically runs in parallel.
- Starting promises without awaiting or observing failures.
- Trusting TypeScript types for network input.
- Installing packages for trivial functionality without evaluating provenance and maintenance.
- Using in-memory jobs, sessions, or rate limits across multiple instances.

## Real-world examples

- An API gateway concurrently calls several upstream services and combines their responses.
- A WebSocket service maintains many mostly idle connections.
- A streaming endpoint pipes data with backpressure instead of buffering it entirely.
- An image-processing API enqueues CPU-intensive work for isolated workers.

## Express.js decision guide

Express.js is a minimal Node.js web framework. It supplies routing, middleware composition, request/response handling, and HTTP utilities while leaving project structure, persistence, validation, authentication, dependency injection, caching, and logging to the application.

### Advantages

- **Lightweight:** a small core is quick to start and understand.
- **Flexible:** teams choose their folder structure, ORM, validation, authentication, and other libraries.
- **Fast development:** minimal setup supports prototypes, REST APIs, and small services.
- **Large ecosystem:** middleware exists for common web concerns.
- **I/O-oriented runtime:** Node.js suits APIs, chat, real-time connections, and streaming when handlers remain non-blocking.

### Limitations

- Architectural freedom can produce inconsistent large codebases unless conventions are enforced.
- Long middleware chains are difficult to trace when poorly organized.
- A production service usually combines many packages whose compatibility, updates, provenance, and security must be maintained.
- Express can support a large enterprise application, but it does not provide enterprise architecture by itself; a structured framework such as NestJS may reduce the amount a team must invent.

Choose Express for lightweight APIs, MVPs, simple microservices, WebSocket services, JavaScript/TypeScript teams, and situations where flexibility and delivery speed outweigh built-in conventions. Prefer a more opinionated or integrated stack when complex business rules, many teams, strong modularity, or extensive production features require consistent defaults.

| Framework | Strong fit | Caution |
| --- | --- | --- |
| Express.js | Minimal APIs, MVPs, flexible services | Large applications need explicit architecture |
| NestJS | Structured TypeScript modules and dependency injection | More setup than a tiny API requires |
| Spring Boot | Complex enterprise rules, transactions, integrated Java ecosystem | Heavier for a small prototype |
| FastAPI | Python APIs, AI/ML and data services | Requires Python expertise and ecosystem fit |

### Express misconceptions

- **“Express is faster than Spring Boot.”** Express commonly has lower framework overhead, but end-to-end performance is dominated by workload, data access, caching, network calls, and design.
- **“Express does not scale.”** It can scale horizontally and serve high traffic with appropriate process isolation, load balancing, data design, and observability.
- **“Express is only for small projects.”** It can support large systems, but larger teams must add and enforce conventions that structured frameworks provide by default.

## Java versus Node.js

Java is a statically typed language and JVM platform with mature enterprise tooling, multithreading, and strong support for CPU-intensive and transaction-heavy systems. Node.js is a JavaScript runtime optimized for coordinating concurrent I/O through its event loop and asynchronous facilities.

| Feature | Java | Node.js |
| --- | --- | --- |
| Typical typing | Static | Dynamic JavaScript; static checks with TypeScript |
| Concurrency model | Multiple threads and executors | Event loop, OS async I/O, libuv pool, optional workers |
| Strong fit | Complex enterprise and CPU-heavy systems | APIs, streaming, real-time, and I/O-heavy systems |
| Startup and footprint | Common enterprise stacks are typically heavier | Typically lighter and faster to start |
| CPU-intensive work | Strong multithreaded options | Must be partitioned or moved to workers/processes |
| I/O-intensive work | Strong, with blocking or non-blocking stacks | Strong when the event loop remains unblocked |
| Ecosystem | Mature JVM and enterprise ecosystem | Large npm and web ecosystem |
| Development trade-off | More explicit structure and ceremony | Rapid iteration with more architectural choices |

Choose Java when strong type guarantees, complex long-lived business logic, mature transaction/security tooling, CPU parallelism, or large-team conventions dominate. Choose Node.js for rapid API delivery, real-time connections, shared frontend/backend language skills, and primarily I/O-bound coordination.

Java is not always faster: workload and implementation determine performance. Node.js can handle many concurrent users, but CPU-bound JavaScript can delay every request on an event-loop thread. Java is not obsolete; the language and JVM continue to evolve. Node.js is also not literally single-threaded as a runtime: JavaScript normally executes on one event-loop thread, while OS facilities, the libuv pool, worker threads, and child processes perform other work.

## Interview Questions

1. **Is Node.js single-threaded?** JavaScript normally runs on one event-loop thread, while the runtime uses OS async I/O, a worker pool, and optional worker threads.
2. **What blocks the event loop?** Long synchronous JavaScript, synchronous APIs, large serialization tasks, or excessive high-priority microtasks.
3. **When should worker threads be used?** For substantial CPU-bound JavaScript that benefits from parallel execution.
4. **Why is Node.js effective for I/O-bound services?** A small number of threads can multiplex many waiting operations.
5. **Express or NestJS?** Express is minimal and flexible; NestJS supplies modules, dependency injection, and conventions at additional complexity.
6. **How do streams handle large data?** They process chunks incrementally and use backpressure to control producer speed.
7. **Why Express instead of Spring Boot?** For a small I/O-heavy service, Express offers minimal setup and JavaScript/TypeScript reuse; Spring offers more integrated production capabilities and conventions.
8. **Is Express suitable for enterprise applications?** Yes, provided the team deliberately supplies architecture, security, validation, observability, dependency governance, and consistent operations.
9. **Which is more scalable: Java or Node.js?** Both scale; Java often fits CPU parallelism and Node.js often fits I/O multiplexing, so the answer depends on workload and architecture.
10. **Why choose Java over Node.js?** Type safety, enterprise tooling, transactions, mature libraries, and CPU-oriented concurrency may justify Java.
11. **Why choose Node.js over Java?** Rapid delivery, one language across a web stack, and efficient I/O or real-time handling may justify Node.js.

## Interview Tips

Do not summarize Node.js as merely “single-threaded.” Explain the event loop, OS I/O, worker pool, worker threads, and the workload implications. Compare runtimes using measured workload, team, and operations constraints—not generic speed claims.

## References

- [Node.js documentation](https://nodejs.org/docs/latest/api/)
- [Node.js event loop guide](https://nodejs.org/en/learn/asynchronous-work/event-loop-timers-and-nexttick)
- [Node.js worker threads](https://nodejs.org/api/worker_threads.html)
- [Node.js streams](https://nodejs.org/api/stream.html)
- [Express documentation](https://expressjs.com/)
- [Node.js security best practices](https://nodejs.org/en/learn/getting-started/security-best-practices)

## Provenance

- **Source-derived:** the Express definition, advantages, limitations, selection guidance, alternatives, misconceptions, Java/Node.js comparison, scenarios, and interview questions were restored from `01-Backend.md`.
- **Editorial additions:** runtime details already present in this guide were used to explain the source's concurrency and performance trade-offs more precisely.
- **Professional corrections:** “single-threaded” is qualified across the event loop, OS I/O, libuv pool, and workers; speed and scale claims are workload-dependent; broad claims about large-company usage were replaced with technically verifiable capability statements rather than unverified company internals.
