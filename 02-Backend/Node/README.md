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

## Interview Questions

1. **Is Node.js single-threaded?** JavaScript normally runs on one event-loop thread, while the runtime uses OS async I/O, a worker pool, and optional worker threads.
2. **What blocks the event loop?** Long synchronous JavaScript, synchronous APIs, large serialization tasks, or excessive high-priority microtasks.
3. **When should worker threads be used?** For substantial CPU-bound JavaScript that benefits from parallel execution.
4. **Why is Node.js effective for I/O-bound services?** A small number of threads can multiplex many waiting operations.
5. **Express or NestJS?** Express is minimal and flexible; NestJS supplies modules, dependency injection, and conventions at additional complexity.
6. **How do streams handle large data?** They process chunks incrementally and use backpressure to control producer speed.

## Interview Tips

Do not summarize Node.js as merely “single-threaded.” Explain the event loop, OS I/O, worker pool, worker threads, and the workload implications. Compare runtimes using measured workload, team, and operations constraints—not generic speed claims.

## References

- [Node.js documentation](https://nodejs.org/docs/latest/api/)
- [Node.js event loop guide](https://nodejs.org/en/learn/asynchronous-work/event-loop-timers-and-nexttick)
- [Node.js worker threads](https://nodejs.org/api/worker_threads.html)
- [Node.js streams](https://nodejs.org/api/stream.html)
- [Express documentation](https://expressjs.com/)
- [Node.js security best practices](https://nodejs.org/en/learn/getting-started/security-best-practices)
