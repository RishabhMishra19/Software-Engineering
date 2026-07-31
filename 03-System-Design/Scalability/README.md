# Scalability

## Overview

Scalability is a system's ability to sustain growth in traffic, data, or work while meeting latency, availability, and cost objectives. Scale vertically by using a larger machine or horizontally by adding instances; most mature systems combine both.

## Why do we need it?

Capacity limits eventually appear in CPU, memory, connections, storage, network bandwidth, or a downstream dependency. Planning around measured demand prevents slowdowns and outages without paying indefinitely for unused capacity.

## How does it work?

1. Define service-level objectives and estimate peak requests, storage growth, read/write ratio, payload size, and headroom.
2. Measure saturation and locate the actual bottleneck.
3. Remove waste first: efficient queries, indexes, batching, compression, and bounded concurrency.
4. Scale stateless compute behind [load balancers](../Load-Balancing/README.md); externalize sessions.
5. Reduce repeated work with [caching](../Caching/README.md) and decouple bursts with [messaging](../Messaging/README.md).
6. Scale data using read replicas, partitioning/sharding, and fit-for-purpose stores.

Replication improves read capacity and availability but introduces lag and failover complexity. Partitioning increases write/storage capacity but makes rebalancing, cross-shard queries, hot keys, and transactions harder. Autoscaling reacts to useful leading signals—queue depth, concurrency, or latency—not only average CPU.

**Production failure modes and practices**

- Retry storms and unbounded queues turn overload into collapse; apply budgets, backpressure, timeouts, and admission control.
- A hot partition defeats nominal horizontal scale; choose high-cardinality keys and test skew.
- Replication lag causes stale reads and read-after-write surprises; route consistency-sensitive reads deliberately.
- Autoscaling starts too late when startup is slow; retain headroom, pre-warm, and load-test failover.
- Common mistake: adding distributed components before profiling. Complexity is a recurring operational cost.

## Advantages

- Supports growth while preserving performance targets.
- Improves availability through redundancy and fault isolation.
- Allows expensive tiers to scale independently.
- Creates explicit capacity and cost controls.

## Limitations

- No component scales infinitely; bottlenecks move.
- Horizontal scale introduces coordination, consistency, and observability costs.
- Overprovisioning wastes money; aggressive elasticity can oscillate.
- Some operations require serialization and resist parallelization.

## Real-world examples

- A product catalog uses CDN and Redis caching for reads, replicas for search, and partitions orders by customer or region.
- A video service stores immutable media in object storage, serves it through a CDN, and asynchronously transcodes uploads.
- A flash sale uses waiting rooms and [rate limits](../Rate-Limiting/README.md) to protect inventory and payment systems.

## Interview Questions

1. How would you estimate capacity and identify the first bottleneck?
2. Vertical scaling versus horizontal scaling: when is each appropriate?
3. Replication versus partitioning: what problem does each solve?
4. How do you detect and mitigate hot partitions?
5. **Interview tip:** state workload and SLO assumptions, find the bottleneck, then justify each scaling mechanism and its failure mode.

## References

- [Google SRE: Handling Overload](https://sre.google/sre-book/handling-overload/)
- [AWS Well-Architected: Performance Efficiency](https://docs.aws.amazon.com/wellarchitected/latest/performance-efficiency-pillar/welcome.html)
- [Azure Architecture Center: Data Partitioning](https://learn.microsoft.com/azure/architecture/best-practices/data-partitioning)
- [Related: Distributed Systems](../Distributed-Systems/README.md)
