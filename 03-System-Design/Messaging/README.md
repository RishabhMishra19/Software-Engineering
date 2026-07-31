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

## Source coverage supplement

```text
             Producer
                 │
                 ▼
         Message Broker
          ┌──────────┐
          │ Queue    │
          │ Topic    │
          └──────────┘
          /          \
         ▼            ▼
    Consumer A   Consumer B
```

The broker enables loose coupling, asynchronous processing, persistence, independent consumer scaling, and parallel work distribution, at the cost of infrastructure, an extra hop, eventual consistency, and harder tracing.

| Feature | Queue | Topic |
| --- | --- | --- |
| Consumption | One consumer normally processes each message | Independent subscribers receive the publication |
| Model | Point-to-point | Publish-subscribe |
| Best fit | Background jobs | Event broadcasting |

| Feature | RabbitMQ-style broker | Kafka-style log |
| --- | --- | --- |
| Primary model | Message queue and routing | Distributed event streaming |
| Ordering | Per queue | Per partition |
| Throughput | High | Very high |
| Retention | Commonly until acknowledged/expired | Configurable retention |
| Replay | Limited compared with a retained log | Native by resetting consumer position |
| Best fit | Task processing and flexible routing | Streaming, analytics, and retained events |

RabbitMQ examples include email/notification services, image processing, invoice generation, payment work, and scheduling where low-latency routed delivery matters. Kafka examples include activity tracking, financial-market data, recommendations, IoT, event sourcing, log aggregation, and real-time analytics at very high volume. Neither universally replaces the other, and not every asynchronous system needs Kafka.

### Delivery guarantees

| Guarantee | Meaning | Advantage | Cost | Representative uses |
| --- | --- | --- | --- | --- |
| At-most-once | Zero or one delivery attempt | Lowest overhead; no broker redelivery duplicates | Work may be lost | Loss-tolerant logs, metrics, monitoring, analytics |
| At-least-once | One or more delivery attempts | Reliable and widely supported | Duplicates; idempotent consumer required | Orders, payments, email, inventory |
| Exactly-once semantics | One committed effect within a defined boundary | Avoids duplicate committed effects in that boundary | Coordination and lower performance | Carefully scoped financial, trading, billing workflows |

Idempotency means replaying the same operation produces the same final effect. Examples include setting an order to `Delivered`, recording a payment by unique transaction ID, and creating a resource with an idempotency key.

Retries recover from transient network, database, or API failures. Use exponential backoff, delayed retries, and a finite attempt limit so a dependency is not overwhelmed. A dead-letter queue isolates messages that exceed that policy because of invalid format, business validation, corruption, or permanent downstream failure. A poison message repeatedly fails regardless of retry count; a DLQ contains it for investigation and controlled replay but does not repair it.

Choose at-most-once only when loss is acceptable and throughput dominates; at-least-once when reliability matters and duplicates can be handled; exactly-once facilities only when the scoped correctness benefit justifies coordination. Duplicate delivery is expected under at-least-once and is not itself a bug. Permanent failure is not fixed by retry.

## Expanded interview questions

1. Why use a broker instead of direct calls, and how does it improve scalability?
2. Queue versus topic; Kafka versus RabbitMQ; when would you choose each?
3. What happens when a consumer fails while processing?
4. Compare delivery guarantees and explain why at-least-once is common.
5. What is idempotency, how do idempotent consumers work, and which guarantee fits payments?
6. What are exponential backoff, a DLQ, and a poison message?

## Professional correction

- Exactly-once is not a universal end-to-end promise that “duplicates are not produced or processed.” It is a scoped transactional semantic; external effects still require idempotency or atomic integration.
- At-most-once does not inherently mean “fastest” for every implementation, and at-least-once does not make loss merely “rare.” Concrete durability, acknowledgement, replication, and producer-confirm settings determine failure behavior.
- A queue can have competing consumers, and a topic may use consumer groups so one member of each group handles a record. “One versus many” describes the logical subscription model, not a fixed consumer count.

## Provenance

- **Source-derived:** Broker/queue diagram, queue-topic and RabbitMQ-Kafka comparisons, delivery details, examples, trade-offs, misconceptions, and expanded questions were restored from `03-Architecture.md`.
- **Editorial:** Production failure guidance, modern semantic qualifications, references, cross-links, and `Professional correction` refine the source without removing its choices.
