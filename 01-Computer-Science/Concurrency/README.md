# Concurrency

> **Provenance:** Editorial addition.

## Overview

Concurrency is the coordination of multiple tasks whose execution overlaps. Parallelism is simultaneous execution on multiple processing units. Concurrent programs must preserve correctness under nondeterministic scheduling, shared state, cancellation, and failure.

## Why do we need it?

Concurrency keeps systems responsive while waiting for I/O, uses multiple CPU cores, supports many independent requests, and structures background work. It is essential in servers, user interfaces, operating systems, databases, and distributed systems.

## How does it work?

### Execution models

- **Threads:** share memory and coordinate with synchronization primitives.
- **Processes:** isolate memory and communicate through IPC.
- **Event loops:** multiplex many I/O operations on a small number of threads.
- **Async tasks:** suspend at declared points while awaiting operations.
- **Message passing:** transfer ownership or immutable messages between workers.

### Correctness

A race condition occurs when an outcome depends on timing. A data race is unsynchronized conflicting memory access. Atomic operations, mutexes, semaphores, condition variables, channels, and immutable data establish safe coordination and ordering.

### Liveness

Deadlock prevents a set of tasks from progressing. Livelock keeps tasks active without useful progress. Starvation indefinitely denies a task access to resources. Bounded queues and backpressure prevent producers from overwhelming consumers.

### Memory visibility

Compilers and processors may reorder operations. A language memory model defines when writes by one execution context become visible to another. Locks, atomics, and documented synchronization primitives establish happens-before relationships.

### Trade-offs

- Shared memory is fast but requires careful synchronization; message passing improves isolation but adds copying and queueing.
- Coarse locks simplify invariants but reduce parallelism; fine-grained locks increase throughput and complexity.
- Optimistic control performs well under low contention; pessimistic control avoids repeated conflicts under high contention.
- Unbounded concurrency maximizes intake briefly but risks memory exhaustion and cascading failure.

## Advantages

- Higher throughput for parallelizable or I/O-bound workloads.
- Better CPU utilization and application responsiveness.
- Natural modeling of independent activities.
- Isolation options through actors, processes, or message passing.
- Scalable request and background-job processing.

## Limitations

- Nondeterministic failures are difficult to reproduce.
- Synchronization adds latency and contention.
- Parallel speedup is limited by serial work and coordination overhead.
- Cancellation, timeout, and error propagation complicate control flow.
- Incorrect resource bounds can cause overload rather than speedup.

## Best Practices

- Minimize shared mutable state and make ownership explicit.
- Protect invariants, not individual lines of code.
- Define lock ordering and keep critical sections short.
- Bound worker pools and queues; propagate backpressure.
- Use structured concurrency so child work is cancelled and awaited.
- Test with race detectors, stress tests, and deterministic fakes where available.
- Make concurrent retries and jobs idempotent.

## Common Mistakes

- Check-then-act operations without atomicity.
- Holding a lock during network or disk I/O.
- Using thread-safe collections while leaving multi-step invariants unsafe.
- Swallowing worker exceptions or losing cancellation.
- Assuming `volatile` or equivalent makes compound operations atomic.
- Increasing worker count without measuring contention or downstream capacity.

## Real-world examples

- A web server multiplexes sockets and dispatches CPU work to a bounded pool.
- A database uses isolation and locking to coordinate transactions.
- A producer-consumer pipeline uses a bounded queue to absorb bursts.
- Optimistic version checks prevent two editors from silently overwriting the same record.

## Interview Questions

1. **What is the difference between concurrency and parallelism?** Concurrency structures overlapping work; parallelism executes work simultaneously.
2. **What is a race condition?** Program correctness depends on an uncontrolled ordering of concurrent operations.
3. **How does a mutex differ from a semaphore?** A mutex provides exclusive ownership; a semaphore tracks a configurable number of permits.
4. **What conditions enable deadlock?** Mutual exclusion, hold-and-wait, no preemption, and circular wait.
5. **Why use a bounded thread pool?** It limits resource usage, controls contention, and enables backpressure.
6. **When is optimistic locking appropriate?** When conflicts are uncommon and retry cost is lower than holding locks.
7. **What is a happens-before relationship?** A memory-model guarantee that effects of one action are visible to another.

## Interview Tips

State the invariant, identify shared state, name the synchronization boundary, and explain liveness and performance implications. For code exercises, consider failure, cancellation, queue bounds, and cleanup—not only the happy path.

## References

- [Java Language Specification: Threads and Locks](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [Go memory model](https://go.dev/ref/mem)
- [Python `asyncio`](https://docs.python.org/3/library/asyncio.html)
- [Rust concurrency](https://doc.rust-lang.org/book/ch16-00-concurrency.html)
- [Oracle Java concurrency tutorial](https://docs.oracle.com/javase/tutorial/essential/concurrency/)
