# Concurrency

> **Provenance:** Editorial addition.

## Overview

Concurrency means coordinating tasks whose lifetimes overlap. Imagine one cook alternating between chopping vegetables and checking an oven: both jobs are in progress, although the cook performs only one action at an instant. Parallelism is several cooks performing actions at the same instant. In a computer, the cooks are execution resources such as central processing unit (CPU) cores. A concurrent system may use parallelism, but it does not have to.

The central challenge is simple: the program must remain correct even when task order changes. Shared state, cancellation, and failure make that challenge harder.

**Prerequisites:** A task is a unit of work. State is information a program remembers. Mutable state can change. Input/output (I/O) means communication with slower external resources such as disks, networks, or keyboards. The [Operating System guide](../Operating-System/README.md) explains processes, threads, scheduling, and memory.

## Why do we need it?

Concurrency keeps systems responsive while waiting for input/output (I/O), uses multiple central processing unit (CPU) cores, supports many independent requests, and structures background work. It is essential in servers, user interfaces, operating systems, databases, and distributed systems.

## How does it work?

### Execution models

- **Threads:** share memory and coordinate with synchronization primitives.
- **Processes:** isolate memory and communicate through inter-process communication (IPC).
- **Event loops:** multiplex many I/O operations on a small number of threads.
- **Async tasks:** suspend at declared points while awaiting operations.
- **Message passing:** transfer ownership or immutable messages between workers.

### Correctness

A race condition occurs when an uncontrolled task order changes the outcome. A data race is the narrower case in which concurrent execution contexts access the same memory without required synchronization and at least one access writes.

Coordination tools solve different parts of this problem. Atomic operations make a defined operation indivisible. A mutual-exclusion lock (mutex) allows one owner into a critical section. Semaphores limit access with permits. Condition variables let tasks wait for state changes. Channels transfer values between tasks. Immutable data avoids in-place changes.

For example, suppose two cashiers read a stock count of `1`, each subtracts one, and each writes `0`. The store has sold two items while recording only one reduction. A transaction, lock, or atomic conditional update must make “check stock and reserve it” one protected operation.

### Liveness

Liveness asks whether useful work continues:

- **Deadlock:** a set of tasks waits in a cycle, so none can proceed.
- **Livelock:** tasks keep reacting to one another but make no useful progress.
- **Starvation:** a task repeatedly loses access to the resource it needs.

Bounded queues and backpressure prevent producers from overwhelming consumers. Backpressure makes producers slow down or reject work when downstream capacity is exhausted.

### Memory visibility

Compilers and processors may reorder operations while preserving single-threaded behavior. A language memory model defines when one execution context must observe another context's writes.

Locks, atomic operations, and other documented synchronization primitives establish a **happens-before** relationship: an ordering guarantee that makes earlier effects visible to later work.

### Trade-offs

- Shared memory is fast but requires careful synchronization; message passing improves isolation but adds copying and queueing.
- Coarse locks simplify invariants but reduce parallelism; fine-grained locks increase throughput and complexity.
- Optimistic control performs well under low contention; pessimistic control avoids repeated conflicts under high contention.
- Unbounded concurrency maximizes intake briefly but risks memory exhaustion and cascading failure.

### Edge cases and production behavior

- Cancellation is cooperative in many systems: code must reach a cancellation-aware operation and release files, locks, and connections.
- A task can fail while sibling tasks continue. Structured concurrency ties their lifetimes together so the parent can cancel, await, and account for every child.
- Lock-free does not mean wait-free. An algorithm may avoid locks while one unlucky task repeatedly retries and starves.
- Thread-safe components do not automatically make a multi-step business rule safe; the invariant across those steps still needs one synchronization boundary.
- Production tuning starts with queue time, throughput, lock contention, event-loop delay, CPU saturation, and downstream capacity. More workers can reduce performance when coordination or a dependency is already saturated.

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
- Make concurrent retries and jobs idempotent. An idempotent operation has the same intended state effect when repeated as when run once.

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
