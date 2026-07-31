# Caching

## Overview

Caching stores reusable data closer to consumers in a faster layer. A cache hit avoids work at the source of truth; a miss loads or computes the value. Caches may live in a process, distributed store, reverse proxy, browser, or CDN.

## Why do we need it?

Caching lowers latency, database and dependency load, infrastructure cost, and the blast radius of a slow backend. It is most valuable for expensive, frequently reused, tolerance-for-staleness data—not as a substitute for indexes or efficient code.

## How does it work?

- **Cache-aside:** application reads cache, loads on miss, then writes the cache. Flexible and common, but misses and invalidation are application concerns.
- **Read-through:** cache loads missing data through a configured provider.
- **Write-through:** update source and cache synchronously; fresher reads cost slower writes.
- **Write-behind:** acknowledge cached writes before persistence; high throughput risks loss and reordering.
- **Refresh-ahead:** refresh popular entries before expiration.

Freshness uses TTLs, explicit invalidation after writes, or change events. Capacity uses eviction such as LRU, LFU, FIFO, or random; pick from measured access patterns. Cache keys must include every input that affects the value, including tenant, locale, authorization scope, and schema version.

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
2. How would you prevent stampede, avalanche, and penetration?
3. When is stale data unacceptable?
4. How would you invalidate data across services?
5. **Interview tip:** define ownership, consistency tolerance, key shape, TTL, eviction, and behavior when the cache fails.

## References

- [AWS Prescriptive Guidance: Caching Patterns](https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/cache-aside.html)
- [Redis: Cache Eviction](https://redis.io/docs/latest/develop/reference/eviction/)
- [RFC 9111: HTTP Caching](https://www.rfc-editor.org/rfc/rfc9111)
- [Related: Scalability](../Scalability/README.md)

## Source coverage supplement

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

| Strategy | Read performance | Write performance | Consistency tendency | Representative fit |
| --- | --- | --- | --- | --- |
| Cache-aside | High after warm-up | High | Eventual | Read-heavy catalogs and profiles |
| Read-through | High after warm-up | Moderate | Eventual | Cache provider owns loading |
| Write-through | High | Moderate/slower | Fresher, not inherently atomic | Frequently read data needing coordinated writes |
| Write-behind | Very high | Very high | Eventual with durability risk | High write throughput and buffering |

Invalidation may expire entries by TTL, update source and cache on a write, delete a cache-aside entry after a source change, or publish a change event so distributed consumers invalidate locally. TTL fits catalogs, news, weather, and public APIs; explicit cache-aside invalidation fits many read-heavy web applications; coordinated writes fit stricter freshness; events fit microservices.

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
2. Cache-aside versus write-through; when would write-behind be justified?
3. What data should not be cached, and when should Redis replace process memory?
4. How do expiration, explicit invalidation, and events prevent stale data?
5. Why is invalidation hard; LRU versus LFU; and which Redis eviction policy fits a given workload?
6. How do stampede, penetration, avalanche, and warming differ?

## Professional correction

- Write-through provides fresher cache contents only when failure handling is defined; two independent writes do not create strong consistency by themselves.
- Write-behind acknowledgement before durable persistence can lose accepted writes and should not be described only as a performance optimization.
- Bloom filters can reduce penetration but have false positives and lifecycle costs; short negative caching is often simpler.

## Provenance

- **Source-derived:** Cache lookup diagram, strategy and eviction comparisons, use cases, alternatives, invalidation choices, failure modes, misconceptions, and expanded questions were restored from `03-Architecture.md`.
- **Editorial:** Key-shape/security guidance, operational telemetry, references, cross-links, and `Professional correction` add production context.
