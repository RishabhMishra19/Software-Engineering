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
