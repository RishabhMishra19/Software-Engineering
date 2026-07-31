# Caching

> Caching reuses a previously obtained result to avoid repeating slower or more expensive work.

## Overview

Think of keeping frequently used tools on a desk instead of walking to a storage room each time. The desk is faster but smaller, and its copy can become outdated.

Caching stores reusable data closer to consumers in a faster layer. A **cache hit** finds the requested value. A **cache miss** must load or compute it from the source of truth. Caches may live in a process, distributed store, reverse proxy, browser, or content delivery network (CDN).

For the system around the cache, a **distributed system** is a group of independent networked processes, and each participating process or machine is a **node**. **Latency** is how long one operation takes; **throughput** is completed work per unit of time; **availability** is whether promised work can be served. **Consistency** defines which version a read may see after a write. **Replication** keeps copies, while a **network partition** separates nodes that cannot communicate. A **load balancer** selects a service node, a **broker** routes messages such as invalidations, and a **rate limit** bounds admitted work.

## Why do we need it?

Caching lowers latency, database and dependency load, infrastructure cost, and the blast radius of a slow backend. It is most valuable for expensive, frequently reused, tolerance-for-staleness data—not as a substitute for indexes or efficient code.

## How does it work?

Mental models for the main patterns:

- **Cache-aside** is checking the desk, fetching from storage when absent, then leaving a copy on the desk.
- **Read-through** gives the desk attendant responsibility for fetching missing tools.
- **Write-through** updates the desk copy during the source update; **write-behind** records the desk update now and sends it to storage later.
- **Refresh-ahead** replaces a popular item before it expires. **Invalidation** removes a copy known to be obsolete; **eviction** frees space even when the value is still valid.
- **Least recently used (LRU)** removes the item untouched for the longest time.
  **Least frequently used (LFU)** removes the least-used item.
  **First in, first out (FIFO)** removes the item placed there earliest.

- **Cache-aside:** application reads cache, loads on miss, then writes the cache. Flexible and common, but misses and invalidation are application concerns.
- **Read-through:** cache loads missing data through a configured provider.
- **Write-through:** update source and cache synchronously; fresher reads cost slower writes.
- **Write-behind:** acknowledge cached writes before persistence; high throughput risks loss and reordering.
- **Refresh-ahead:** refresh popular entries before expiration.

Freshness uses **time to live (TTL)** expiration, explicit invalidation after writes, or change events. Capacity uses eviction such as **least recently used (LRU)**, **least frequently used (LFU)**, **first in, first out (FIFO)**, or random removal. Pick from measured access patterns. Cache keys must include every input that affects the value, including tenant, locale, authorization scope, and schema version.

**Cache-aside read flow**

1. Build a key from every input that can change the result.
2. Read the cache.
3. On a hit, return the cached value if its staleness is acceptable.
4. On a miss, read the source of truth.
5. Populate the cache with a bounded TTL, then return the value.

**Consistency and write guarantees.** A TTL bounds how long an entry may remain without refresh; it does not guarantee that the entry matches the database. Cache-aside and event invalidation are normally eventually consistent because a read can occur before invalidation arrives. Write-through is strongly consistent only if cache and source changes share an atomic boundary or failures are reconciled. Write-behind can acknowledge a write before durable storage, so a cache failure may lose accepted data.

**Production failure modes and practices**

- **Stampede:** many callers rebuild one key; use request coalescing, single-flight locks, stale-while-revalidate, and bounded concurrency.
- **Avalanche:** synchronized expirations overload the origin; jitter TTLs and degrade gracefully.
- **Penetration:** repeated misses for absent keys; negative-cache briefly or use a Bloom filter.
- Cache outage shifts full load to the database; size the origin for fallback or shed load.
- Common mistakes include caching sensitive data under shared keys, deleting before a database commit, relying on unbounded keys, and treating TTL as a consistency guarantee.
- Monitor hit ratio, miss latency, evictions, memory, hot keys, stale-read incidents, and origin load—not hit ratio alone.

## Advantages

- Very low read latency and reduced backend work.
- Absorbs read bursts and protects dependencies.
- Supports graceful degradation with explicitly stale data.
- CDNs move public content closer to users.

## Limitations

- Stale or inconsistent data is inherent unless coordination is added.
- Invalidation, key design, and warming add complexity.
- Distributed caches add network hops and infrastructure.
- Write-behind can lose acknowledged data; caches are usually not the source of truth.

## Real-world examples

- Product details use cache-aside with short jittered TTLs and event-based invalidation.
- Browser and CDN caches serve versioned immutable assets.
- A recommendations endpoint returns a recent cached result when its dependency is unavailable.

## Interview Questions

1. Compare cache-aside, write-through, and write-behind.
   **Key points:** cache-aside leaves misses and invalidation to the application; write-through updates the cache on the write path; write-behind acknowledges before durable persistence and therefore trades durability and ordering for throughput.
2. How would you prevent stampede, avalanche, and penetration?
   **Key points:** coalesce rebuilds for one hot key, jitter bulk expirations, and briefly negative-cache absent values or use a maintained Bloom filter.
3. When is stale data unacceptable?
   **Key points:** when it can violate authorization, money, inventory, safety, or contractual rules. Read from the authoritative store or use a protocol with a defined consistency boundary.
4. How would you invalidate data across services?
   **Key points:** publish durable, versioned change events after the source transaction, make invalidation idempotent, monitor lag, and retain TTL as a repair bound.
5. **Interview tip:** define ownership, consistency tolerance, key shape, TTL, eviction, and behavior when the cache fails.

## References

- [Amazon Web Services (AWS) Prescriptive Guidance: Caching Patterns](https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/cache-aside.html)
- [Redis: Cache Eviction](https://redis.io/docs/latest/develop/reference/eviction/)
- [Request for Comments (RFC) 9111: Hypertext Transfer Protocol (HTTP) Caching](https://www.rfc-editor.org/rfc/rfc9111)
- [Related: Scalability](../Scalability/README.md)

## Source coverage supplement

The diagram explains cache-aside behavior: hits return immediately, while misses visit the database and populate the cache before returning.

```text
           Client
              │
              ▼
         Application
              │
        Cache Lookup
         ┌────┴────┐
         │         │
    Cache Hit  Cache Miss
         │         │
         ▼         ▼
    Return Data  Database
                     │
                     ▼
              Update Cache
                     │
                     ▼
               Return Data
```

Caching is most useful when reads dominate writes, source queries are expensive, latency matters, data changes infrequently enough to tolerate a freshness policy, or a backend is under load. Product catalogs, profiles, configuration, sessions, search results, and frequently viewed content are common candidates. Alternatives include no cache for small systems, process memory for a single instance, a distributed cache for scaled applications, and a CDN for public/static content.

The following table compares where each strategy performs work and what freshness or durability cost follows.

| Strategy | Read performance | Write performance | Consistency tendency | Representative fit |
| --- | --- | --- | --- | --- |
| Cache-aside | High after warm-up | High | Eventual | Read-heavy catalogs and profiles |
| Read-through | High after warm-up | Moderate | Eventual | Cache provider owns loading |
| Write-through | High | Moderate/slower | Fresher, not inherently atomic | Frequently read data needing coordinated writes |
| Write-behind | Very high | Very high | Eventual with durability risk | High write throughput and buffering |

Invalidation may expire entries by TTL, update source and cache on a write, delete a cache-aside entry after a source change, or publish a change event so distributed consumers invalidate locally. TTL fits catalogs, news, weather, and public **application programming interfaces (APIs)**; explicit cache-aside invalidation fits many read-heavy web applications; coordinated writes fit stricter freshness; events fit microservices.

The eviction table helps select which entry to remove when cache capacity is full.

| Eviction | Removes | Representative fit |
| --- | --- | --- |
| LRU | Least recently accessed | General-purpose data, sessions, catalogs |
| LFU | Least frequently accessed | Reused datasets and recommendations |
| FIFO | Oldest inserted | Simple workloads |
| Random | Arbitrary entry | Very simple implementations |

Invalidation improves accuracy and controls memory but causes more misses and source load; aggressive invalidation trades cache effectiveness for freshness. Warming preloads likely-hot data at startup, on a schedule, or in the background.

The source’s complete failure set is: stampede (many requests rebuild one key; coalesce, lock, refresh early/background), penetration (repeated absent keys; Bloom filter or negative cache), avalanche (many simultaneous expirations; randomized/staggered TTLs or multilevel caching), and cold-cache warming.

Caching is not mandatory, more cache is not always faster, cached values are not automatically correct, and cache does not replace indexes, query design, or schema design. Invalidation may update rather than delete. Longer TTL increases stale-data risk; LRU is not universally best; a stampede can happen whenever requests converge on one expired key, not only at globally high traffic.

## Expanded interview questions

1. What are a cache hit, miss, invalidation, and eviction?
   **Key points:** a hit returns cached data, a miss consults the source, invalidation marks known data obsolete, and eviction removes data to free capacity.
2. Cache-aside versus write-through; when would write-behind be justified?
   **Key points:** use cache-aside for common read-heavy workloads, write-through for a coordinated write path, and write-behind only when throughput outweighs acknowledged-write loss and reordering risk.
3. What data should not be cached, and when should Redis replace process memory?
   **Key points:** avoid unsafe shared copies of sensitive or correctness-critical data. Use a distributed cache when instances need shared entries, capacity, or coordinated expiry.
4. How do expiration, explicit invalidation, and events prevent stale data?
   **Key points:** expiration limits lifetime, explicit invalidation reacts to a known write, and events distribute that reaction. None is instantaneous unless the consistency boundary says so.
5. Why is invalidation hard; LRU versus LFU; and which Redis eviction policy fits a given workload?
   **Key points:** writes race with reads and messages can lag. LRU favors recent reuse; LFU favors repeated popularity. Measure the access distribution before selecting a policy.
6. How do stampede, penetration, avalanche, and warming differ?
   **Key points:** stampede rebuilds one key concurrently, penetration repeatedly requests absent keys, avalanche expires many keys together, and warming preloads expected hot data.

## Professional correction

- Write-through provides fresher cache contents only when failure handling is defined; two independent writes do not create strong consistency by themselves.
- Write-behind acknowledgement before durable persistence can lose accepted writes and should not be described only as a performance optimization.
- Bloom filters can reduce penetration but have false positives and lifecycle costs; short negative caching is often simpler.

## Provenance

- **Source-derived:** Cache lookup diagram, strategy and eviction comparisons, use cases, alternatives, invalidation choices, failure modes, misconceptions, and expanded questions were restored from `03-Architecture.md`.
- **Editorial:** Key-shape/security guidance, operational telemetry, references, cross-links, and `Professional correction` add production context.
