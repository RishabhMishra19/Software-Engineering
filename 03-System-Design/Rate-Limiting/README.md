# Rate Limiting

## Overview

Rate limiting controls how much work a client, tenant, route, or system may submit over time. It protects finite capacity, enforces product quotas, improves fairness, and complements—not replaces—authentication, autoscaling, and load shedding.

## Why do we need it?

Traffic can exceed capacity because of abuse, bugs, retries, scraping, expensive endpoints, or legitimate spikes. Without admission control, a small set of callers can exhaust threads, connections, or dependency quotas and cause a broad outage.

## How does it work?

- **Fixed window:** simple counters per interval; boundary bursts can double the intended rate.
- **Sliding log:** precise but memory-heavy.
- **Sliding-window counter:** approximates a rolling window with less state.
- **Token bucket:** tokens replenish at a fixed rate; permits controlled bursts.
- **Leaky bucket:** drains at a fixed rate; smooths output but may queue or reject bursts.
- **Concurrency limit:** caps in-flight work and often tracks overload better than requests per second.

Choose a key such as authenticated principal plus tenant and route; IP-only limits penalize NAT users and are easy to evade. In distributed enforcement, centralized atomic counters are accurate but add latency and dependency risk; local limits are fast and resilient but approximate a global quota. Hierarchical budgets can cover system, tenant, user, and endpoint.

Return HTTP `429 Too Many Requests` with useful quota headers and `Retry-After` when appropriate. Prefer early rejection at the edge, but retain service-level limits because internal traffic can bypass a gateway.

**Production failure modes and practices**

- A shared rate-limit store fails and either blocks everyone or allows overload; explicitly choose fail-open/fail-closed by risk and add local emergency limits.
- Hot keys overload one counter shard; partition carefully or use local leases.
- Limits based only on request count ignore expensive operations; weight costs or limit concurrency.
- Synchronized client retries create waves; clients need exponential backoff and jitter.
- Common mistakes: silent throttling, unlimited admin/internal paths, trusting spoofable identity headers, and treating limits as security against distributed attacks.
- Measure allowed, rejected, near-limit, store latency/error, fairness, and downstream saturation; tune from capacity tests and SLOs.

## Advantages

- Prevents overload and limits failure blast radius.
- Enforces fair use and commercial quotas.
- Controls spend on expensive dependencies.
- Makes capacity allocation explicit.

## Limitations

- Distributed accuracy adds coordination and latency.
- Static limits can reject safe traffic or admit costly traffic.
- Client identity may be ambiguous.
- Queuing instead of rejecting can increase latency and memory pressure.

## Real-world examples

- An API applies tenant token buckets plus per-endpoint weighted costs.
- Login uses strict account and IP limits alongside progressive delays and abuse detection.
- A worker pool uses adaptive concurrency limits based on observed latency.

## Interview Questions

1. Token bucket versus leaky bucket versus sliding window?
2. How would you enforce a global limit across regions?
3. Should the limiter fail open or fail closed?
4. Rate limiting versus throttling, quotas, and load shedding?
5. **Interview tip:** define identity, scope, burst policy, storage, consistency, rejection response, and limiter-failure behavior.

## References

- [RFC 6585: HTTP 429](https://www.rfc-editor.org/rfc/rfc6585#section-4)
- [IETF RateLimit Header Fields](https://www.ietf.org/archive/id/draft-ietf-httpapi-ratelimit-headers-07.html)
- [Google SRE: Handling Overload](https://sre.google/sre-book/handling-overload/)
- [AWS Builders' Library: Fairness in Multi-Tenant Systems](https://aws.amazon.com/builders-library/fairness-in-multi-tenant-systems/)
- [Related: Load Balancing](../Load-Balancing/README.md)

## Architecture-source routing context

The architecture source places rate limiting among API-gateway cross-cutting concerns alongside routing, authentication, authorization, TLS termination, request validation/transformation, aggregation, logging, monitoring, and load balancing. A gateway can protect backend services from excessive client traffic and provide one policy point, but the gateway must remain highly available and must not own business logic.

Direct service access may be adequate for small systems; an API gateway becomes useful with multiple backend services, a unified client API, or shared authentication and policy. It complements rather than replaces a load balancer: the gateway manages API semantics while the balancer distributes traffic among healthy instances. Service discovery separately locates dynamic instances.

## Professional correction

- Gateway-only rate limiting is insufficient when internal callers, queues, or alternate ingress paths can bypass it; retain service/dependency-level admission control.
- Rate limiting protects a configured capacity model but does not itself guarantee security, fairness, or availability.
- A gateway is not mandatory for every application and introduces an extra hop, operational cost, bottleneck risk, and failure domain.

## Provenance

- **Source-derived:** API-gateway rate-limiting responsibility, gateway alternatives, trade-offs, and gateway/load-balancer/discovery distinctions were restored from `03-Architecture.md`.
- **Editorial:** Algorithms, distributed enforcement, failure behavior, references, cross-links, and `Professional correction` provide canonical rate-limiting depth.
