# Rate Limiting

> Rate limiting protects shared capacity by deciding how much work each caller may start over time.

## Overview

Think of a venue with a controlled entrance. The venue may admit a steady number of people, permit a small temporary rush, or stop admission when the building is full. A rate limiter makes the same decision for software work.

Rate limiting controls how much work a client, tenant, route, or **application programming interface (API)** may submit over time. It protects finite capacity, enforces product quotas, and improves fairness. It complements, but does not replace, authentication, autoscaling, and load shedding.

For the surrounding architecture, a **distributed system** is a group of independent networked processes, and each process or machine is a **node**. **Latency** is operation duration; **throughput** is completed work per unit of time; **availability** is whether promised work can be served. **Consistency** defines which state distributed decisions observe. **Replication** creates copies, while a **network partition** prevents nodes from communicating. A **load balancer** selects a service node, a **cache** holds temporary reusable data, and a **broker** routes messages.

## Why do we need it?

Traffic can exceed capacity because of abuse, bugs, retries, scraping, expensive endpoints, or legitimate spikes. Without admission control, a small set of callers can exhaust threads, connections, or dependency quotas and cause a broad outage.

## How does it work?

- **Fixed window:** like counting tickets sold each clock hour; simple, but traffic at the end and start of adjacent hours creates a boundary burst.
- **Sliding log:** like retaining every admission timestamp and counting the last hour exactly; precise but memory-heavy.
- **Sliding-window counter:** blends nearby time-bucket counts to approximate a rolling window with less state.
- **Token bucket:** like handing out permits at a fixed refill rate and allowing saved permits to fund a controlled burst.
- **Leaky bucket:** like a funnel draining at a fixed rate; it smooths output but may queue or reject bursts.
- **Concurrency limit:** like limiting occupied seats rather than arrivals per minute; it caps in-flight work and often tracks overload better than requests per second.

Choose a key such as authenticated principal plus tenant and route; **Internet Protocol (IP)**-only limits penalize **network address translation (NAT)** users and are easy to evade. In distributed enforcement, centralized atomic counters are accurate but add latency and dependency risk; local limits are fast and resilient but approximate a global quota. Hierarchical budgets can cover system, tenant, user, and endpoint.

**Example token-bucket flow**

1. Identify the caller and applicable system, tenant, user, and route budgets.
2. Refill tokens according to elapsed time, up to the configured burst capacity.
3. Atomically consume the request's weighted token cost.
4. Admit the request if enough tokens exist; otherwise reject it.
5. Return Hypertext Transfer Protocol (HTTP) `429 Too Many Requests` with useful quota headers and `Retry-After` when appropriate.

Prefer early rejection at the edge, but retain service-level limits because internal traffic can bypass a gateway.

**Consistency and failure policy.** A strict global quota needs coordinated state, which increases latency and can reduce availability. Regional or local counters remain available during partition but can collectively exceed the nominal global limit. Define the tolerated overshoot. Also choose explicitly whether a failed limiter is **fail-open** (admit work, preserving access but risking overload) or **fail-closed** (reject work, preserving the protected resource but risking an outage).

**Production failure modes and practices**

- A shared rate-limit store fails and either blocks everyone or allows overload; explicitly choose fail-open/fail-closed by risk and add local emergency limits.
- Hot keys overload one counter shard; partition carefully or use local leases.
- Limits based only on request count ignore expensive operations; weight costs or limit concurrency.
- Synchronized client retries create waves; clients need exponential backoff and jitter.
- Common mistakes: silent throttling, unlimited admin/internal paths, trusting spoofable identity headers, and treating limits as security against distributed attacks.
- Measure allowed, rejected, near-limit, store latency/error, fairness, and downstream saturation. Tune from capacity tests and service-level objectives (SLOs).

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
   **Key points:** token buckets allow bounded bursts, leaky buckets smooth output, and sliding windows enforce a rolling count with an accuracy-versus-state trade-off.
2. How would you enforce a global limit across regions?
   **Key points:** use a coordinated atomic store for strictness, or allocate regional token leases for lower latency and higher availability. State the permitted overshoot during partitions.
3. Should the limiter fail open or fail closed?
   **Key points:** fail open when availability is more important and downstream capacity has another guard. Fail closed for costly, security-sensitive, or contractual limits; add local emergency protection either way.
4. Rate limiting versus throttling, quotas, and load shedding?
   **Key points:** a rate limit is an admission rule, throttling delays or reduces work, a quota is an entitlement over a period, and load shedding rejects work because the system is currently saturated.
5. **Interview tip:** define identity, scope, burst policy, storage, consistency, rejection response, and limiter-failure behavior.

## References

- [Request for Comments (RFC) 6585: HTTP 429](https://www.rfc-editor.org/rfc/rfc6585#section-4)
- [Internet Engineering Task Force (IETF): RateLimit Header Fields](https://www.ietf.org/archive/id/draft-ietf-httpapi-ratelimit-headers-07.html)
- [Google Site Reliability Engineering (SRE): Handling Overload](https://sre.google/sre-book/handling-overload/)
- [Amazon Web Services (AWS) Builders' Library: Fairness in Multi-Tenant Systems](https://aws.amazon.com/builders-library/fairness-in-multi-tenant-systems/)
- [Related: Load Balancing](../Load-Balancing/README.md)

## Architecture-source routing context

The architecture source places rate limiting among API-gateway cross-cutting concerns alongside routing, authentication, authorization, **Transport Layer Security (TLS)** termination, request validation/transformation, aggregation, logging, monitoring, and load balancing. A gateway can protect backend services from excessive client traffic and provide one policy point, but the gateway must remain highly available and must not own business logic.

Direct service access may be adequate for small systems; an API gateway becomes useful with multiple backend services, a unified client API, or shared authentication and policy. It complements rather than replaces a load balancer: the gateway manages API semantics while the balancer distributes traffic among healthy instances. Service discovery separately locates dynamic instances.

## Professional correction

- Gateway-only rate limiting is insufficient when internal callers, queues, or alternate ingress paths can bypass it; retain service/dependency-level admission control.
- Rate limiting protects a configured capacity model but does not itself guarantee security, fairness, or availability.
- A gateway is not mandatory for every application and introduces an extra hop, operational cost, bottleneck risk, and failure domain.

## Provenance

- **Source-derived:** API-gateway rate-limiting responsibility, gateway alternatives, trade-offs, and gateway/load-balancer/discovery distinctions were restored from `03-Architecture.md`.
- **Editorial:** Algorithms, distributed enforcement, failure behavior, references, cross-links, and `Professional correction` provide canonical rate-limiting depth.
