# Scalability

> Scalability is the practice of adding capacity without losing required speed, reliability, or cost control.

## Overview

Use a restaurant as a mental model. A larger kitchen is **vertical scaling**: one machine gains more **central processing unit (CPU)** capacity, memory, or storage. Opening more kitchens is **horizontal scaling**: more instances share the work. Both approaches help only when they address the actual bottleneck.

Scalability is a system's ability to sustain growth in traffic, data, or work while meeting latency, availability, and cost objectives. Most mature systems combine vertical and horizontal scaling.

Beginner vocabulary:

- A **distributed system** has independent processes or computers cooperating over a network; each participating process or machine is a **node**.
- **Latency** is the time one operation takes, while **throughput** is completed work per unit of time.
- **Availability** is whether the service can perform promised work when requested. **Consistency** defines which data versions reads may observe after a write.
- **Replication** keeps copies on multiple nodes. **Partitioning** divides data among owners; a **network partition** instead means nodes cannot communicate.
- A **cache** keeps faster temporary copies, a **broker** stores and routes messages, a **load balancer** selects an eligible node, and a **rate limit** controls admitted work over time.

## Why do we need it?

Capacity limits eventually appear in CPU, memory, connections, storage, network bandwidth, or a downstream dependency. Planning around measured demand prevents slowdowns and outages without paying indefinitely for unused capacity.

## How does it work?

1. Define **service-level objectives (SLOs)**: measurable reliability targets such as latency and availability. Estimate peak requests, storage growth, read/write ratio, payload size, and spare capacity.
2. Measure saturation and locate the actual bottleneck.
3. Remove waste first: efficient queries, indexes, batching, compression, and bounded concurrency.
4. Scale stateless compute behind [load balancers](../Load-Balancing/README.md); externalize sessions.
5. Reduce repeated work with [caching](../Caching/README.md) and decouple bursts with [messaging](../Messaging/README.md).
6. Scale data using read replicas, partitioning (also called sharding), and fit-for-purpose stores.

Replication keeps copies of data. It can improve read capacity and availability, but replicas may lag behind the primary and return stale values. A failover can also lose recently acknowledged writes unless the replication guarantee prevents it.

Partitioning divides data among owners. It increases write and storage capacity, but makes rebalancing, cross-partition queries, hot keys, and transactions harder. Autoscaling should react to useful leading signals, such as queue depth, concurrency, or latency, rather than only average CPU usage.

As a mental model, replication gives several librarians copies of the same catalog, which helps readers but requires updates to reach every copy. Partitioning gives each librarian different shelves, which increases total capacity but makes requests spanning several shelves harder. A hot partition is one librarian receiving most visitors while the others remain idle.

**Consistency and delivery guarantees.** A read replica commonly provides eventual consistency: if writes stop, replicas eventually converge. Applications that require read-your-writes must route those reads to an up-to-date owner or wait for a replication position. Messaging used to absorb bursts must define whether work can be lost, duplicated, or replayed; scaling consumers does not change that delivery guarantee.

**Concrete product-read flow.** A client request reaches a load balancer, which chooses an application node. The node checks a cache; a hit returns immediately, while a miss reads a database replica and fills the cache. Writes go to the primary data owner and then replicate. This raises production questions: a replica may lag, a hot cache key may overload one node, failover may lose an acknowledged write, and adding application nodes cannot fix a saturated database. Measure each hop and scale the constrained resource.

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

- A product catalog uses a **content delivery network (CDN)** and Redis caching for reads, replicas for search, and partitions orders by customer or region.
- A video service stores immutable media in object storage, serves it through a CDN, and asynchronously transcodes uploads.
- A flash sale uses waiting rooms and [rate limits](../Rate-Limiting/README.md) to protect inventory and payment systems.

## Interview Questions

1. How would you estimate capacity and identify the first bottleneck?
   **Key points:** state SLOs and peak workload assumptions, estimate each resource, load test, and use saturation and latency measurements to find the first constrained dependency.
2. Vertical scaling versus horizontal scaling: when is each appropriate?
   **Key points:** vertical scaling is simpler but bounded by machine size and one failure domain. Horizontal scaling adds capacity and redundancy but requires partitioning, coordination, and stateless or deliberately managed state.
3. Replication versus partitioning: what problem does each solve?
   **Key points:** replication copies data for read scale and availability. Partitioning divides data for write and storage scale; both add consistency and operational costs.
4. How do you detect and mitigate hot partitions?
   **Key points:** measure per-partition traffic and latency, then redesign or salt the key, split the hot range, cache hot reads, or isolate exceptional tenants.
5. **Interview tip:** state workload and SLO assumptions, find the bottleneck, then justify each scaling mechanism and its failure mode.

## References

- [Google Site Reliability Engineering (SRE): Handling Overload](https://sre.google/sre-book/handling-overload/)
- [Amazon Web Services (AWS) Well-Architected: Performance Efficiency](https://docs.aws.amazon.com/wellarchitected/latest/performance-efficiency-pillar/welcome.html)
- [Azure Architecture Center: Data Partitioning](https://learn.microsoft.com/azure/architecture/best-practices/data-partitioning)
- [Related: Distributed Systems](../Distributed-Systems/README.md)

## Architecture-source selection guidance

The architecture source maps growing traffic to horizontal scaling, database-load reduction to caching, independently scaled reads to **command query responsibility segregation (CQRS)**, independently scaled services to microservices, and changing infrastructure to service discovery. Treat these as candidates to evaluate after measuring the bottleneck, not automatic one-to-one prescriptions. Monoliths can scale vertically and horizontally; microservices are justified when independent capability scaling and deployment outweigh network and operational costs.

For availability, the source pairs instance failures with load balancing, service failures with circuit breakers, transient outages with retry, resource isolation with bulkheads, and continued reduced service with fallback. These mechanisms complement capacity planning and do not individually guarantee availability.

Relevant source scenarios ask how to handle 100 million reads per day, a 50:1 read/write ratio, database CPU above 90%, zero-downtime deployment, and dynamically changing service instances. A sound answer states workload and SLO assumptions, profiles first, optimizes queries/indexes, then evaluates caching, replicas/CQRS, horizontal compute, discovery, and traffic control.

## Professional correction

- High read volume or a 50:1 ratio alone does not justify CQRS; replicas, indexes, caching, and simpler read models may solve the actual bottleneck.
- Horizontal scaling does not remove state, consistency, partition skew, or downstream limits.
- Microservices are not a general scaling prerequisite; a replicated monolith may satisfy substantial traffic.

## Provenance

- **Source-derived:** Scalability/availability decision mappings, monolith-scaling misconception, and scenario prompts were restored from `03-Architecture.md`.
- **Editorial:** Capacity workflow, failure analysis, references, cross-links, and `Professional correction` add production criteria.
