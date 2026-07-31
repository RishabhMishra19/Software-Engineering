# Messaging

## Overview

Messaging lets producers communicate with consumers through a broker. A queue usually gives one worker each message; publish-subscribe gives independent subscribers a copy; a durable log or stream retains ordered records for replay. An event reports something that happened, while a message may also be a command, request, or reply.

## Why do we need it?

Asynchronous messaging decouples availability and processing time, buffers bursts, enables fan-out, and lets consumers scale independently. Synchronous calls remain better when a user needs an immediate result or strong request-time validation; production systems commonly use both.

## How does it work?

A producer publishes to a queue, topic, or partitioned log. The broker persists and routes data; consumers process it and acknowledge or commit progress.

- **At-most-once:** fastest, but a failure can lose work.
- **At-least-once:** retries minimize loss but duplicates are expected; make consumers idempotent.
- **Exactly-once semantics:** require a precisely scoped transactional boundary. They do not magically make arbitrary external side effects exactly once.
- Ordering is generally guaranteed only within a queue, session, or partition; key related events consistently.

RabbitMQ-style brokers suit work queues, acknowledgements, and flexible routing. Kafka-style logs suit high-throughput streams, retention, replay, and consumer groups. Choose from semantics and workload, not popularity.

**Production failure modes and practices**

- Poison messages retry forever; cap retries, use exponential backoff with jitter, then send to a dead-letter queue with replay tooling and ownership.
- Slow consumers create lag and expired retention; alert on oldest-message age and scale or shed work.
- A crash after side effect but before acknowledgement duplicates work; use idempotency keys, deduplication records, or transactional outbox/inbox patterns.
- Schema changes break hidden consumers; use backward-compatible evolution and a schema registry where appropriate.
- Common mistakes: using one global ordering bottleneck, publishing database changes non-atomically, treating a DLQ as resolution, and retrying permanent validation errors.
- Propagate correlation IDs and trace context; monitor publish failures, lag, retries, duplicates, DLQ depth, and processing latency.

## Advantages

- Loose temporal coupling and burst absorption.
- Independent consumer scaling and fan-out.
- Durable retry and replay capabilities.
- Better isolation of long-running work.

## Limitations

- Eventual consistency and delayed feedback.
- Harder tracing, testing, schema evolution, and operations.
- Duplicate, reordered, delayed, or lost messages must be considered.
- The broker becomes critical infrastructure.

## Real-world examples

- Order-created events independently trigger inventory, notifications, analytics, and fraud checks.
- Image uploads enqueue CPU-heavy transformations.
- Kafka retains clickstream events for several consumer groups; RabbitMQ routes background jobs by capability.

## Interview Questions

1. Queue versus topic versus durable event log?
2. Compare at-most-once, at-least-once, and exactly-once semantics.
3. How do outbox and idempotent consumer patterns prevent inconsistency?
4. Kafka versus RabbitMQ: what workload facts drive the choice?
5. **Interview tip:** discuss failure windows—producer crash, broker outage, consumer crash, duplicate side effect—and define recovery for each.

## References

- [Apache Kafka Design](https://kafka.apache.org/documentation/#design)
- [RabbitMQ Reliability Guide](https://www.rabbitmq.com/docs/reliability)
- [AWS Builders' Library: Avoiding Insurmountable Queue Backlogs](https://aws.amazon.com/builders-library/avoiding-insurmountable-queue-backlogs/)
- [CloudEvents Specification](https://cloudevents.io/)
- [Related: Distributed Systems](../Distributed-Systems/README.md)
