# Load Balancing

> Load balancing sends each new unit of work to an eligible instance so no single instance carries avoidable load.

## Overview

Think of a dispatcher assigning incoming jobs to available workers. The dispatcher needs a current worker list, a way to recognize unavailable workers, and a selection rule.

Load balancing distributes traffic across healthy service instances to improve capacity and availability. **Layer 4 (L4)** balancers route using transport information such as Transmission Control Protocol (TCP) or User Datagram Protocol (UDP) addresses and ports. **Layer 7 (L7)** balancers understand application protocols such as Hypertext Transfer Protocol (HTTP), including hosts, paths, headers, and cookies.

In this context, a **distributed system** is a set of independent networked processes, and each process or machine is a **node**. **Latency** is the duration of one request; **throughput** is completed requests per unit of time; **availability** is the ability to serve promised work. **Replication** creates multiple service or data copies, while a **network partition** prevents nodes from communicating. A balancer routes among replicas but does not itself provide data **consistency**, meaning rules about which value a read observes. A **cache** holds temporary reusable data, a **broker** routes messages, and a **rate limit** controls admitted work. An **application programming interface (API)** is a contract through which software clients make requests.

## Why do we need it?

A single instance has finite capacity and is a failure domain. A load balancer presents a stable endpoint, spreads work, removes unhealthy instances, and enables rolling or canary deployments. It cannot fix slow code or make unhealthy dependencies highly available.

## How does it work?

The balancer obtains eligible endpoints from static configuration, the Domain Name System (DNS), or service discovery. It then follows this flow:

1. Receive a connection or request at a stable endpoint.
2. Remove targets that are not ready or have been ejected by passive health checks.
3. Apply the routing policy and select an eligible target.
4. Forward the work and enforce connection, request, and idle timeouts.
5. Record latency, errors, and saturation so routing and health decisions can be evaluated.

Common selection policies are:

- **Round robin:** like dealing cards in turn; simple for similar short-lived requests.
- **Least connections/requests:** like choosing the shortest checkout line; useful for varied or long work.
- **Weighted routing:** like giving stronger workers more turns; supports unequal capacity and gradual releases.
- **Hash/consistent hash:** like assigning a customer to a stable service desk; preserves affinity or cache locality while limiting remapping.
- **Power of two choices:** like sampling two lines and joining the shorter one; it approximates load without checking every endpoint.

Health checks should distinguish **liveness** (restart needed), **readiness** (safe for traffic), and dependency health. Passive outlier detection complements active probes. Connection draining lets in-flight requests finish during deployments.

**Consistency and delivery guarantees.** A balancer does not make replicated application data consistent. Requests routed to different replicas can observe stale state unless the data tier provides the required guarantee. Retries can also deliver the same request more than once after an ambiguous timeout, so mutating operations need idempotency when automatic retries are enabled.

**Production failure modes and practices**

- Deep health checks make every instance fail when one shared dependency fails; report readiness only when serving would be harmful.
- Round robin overloads slow instances because equal request counts are not equal work.
- Sticky sessions hide statefulness and create uneven load; prefer external session storage when possible.
- Failover causes a thundering herd on survivors; retain capacity headroom and ramp traffic gradually.
- Misconfigured timeouts, keep-alives, retries, or **Transport Layer Security (TLS)** termination cause connection storms and tail latency.
- The balancer itself must be redundant. Observe per-target latency, errors, saturation, ejections, retries, and imbalance.
- Common mistake: relying on DNS alone for rapid failover without accounting for resolver and client caching.

## Advantages

- Horizontal scale behind one endpoint.
- Failure detection and traffic removal.
- Safer rolling, blue-green, and canary releases.
- TLS termination and L7 routing can be centralized.

## Limitations

- Adds a network hop, configuration surface, and failure domain.
- Algorithms infer load imperfectly.
- Session affinity reduces distribution quality.
- High availability still depends on applications, data, network, and capacity.

## Real-world examples

- A public **Amazon Web Services (AWS) Application Load Balancer (ALB)** routes `/api` and `/static` to different target groups.
- Kubernetes Services distribute connections while an ingress controller performs host/path routing.
- A canary receives 5% weighted traffic, then expands only if **service-level objective (SLO)** metrics remain healthy.

## Interview Questions

1. Layer 4 versus Layer 7 balancing?
   **Key points:** L4 uses connection metadata and has less application awareness. L7 can route and apply policy using HTTP content, but adds parsing, configuration, and processing cost.
2. Round robin versus least connections versus consistent hashing?
   **Key points:** round robin fits similar short work, least connections approximates load for long-lived work, and consistent hashing preserves affinity while limiting remapping when membership changes.
3. How should readiness checks behave when a database is degraded?
   **Key points:** fail readiness only when the instance cannot safely serve its intended traffic. If every instance depends on the same degraded database, ejecting all of them can worsen the outage; degraded responses or admission control may be safer.
4. Load balancer versus API gateway versus service discovery?
   **Key points:** discovery lists eligible instances, a load balancer selects one, and an API gateway applies client-facing routing and API policy.
5. **Interview tip:** discuss health, algorithm, state, overload, failover capacity, and what happens during deployment.

## References

- [Google Cloud: Load Balancing Overview](https://cloud.google.com/load-balancing/docs/load-balancing-overview)
- [AWS Elastic Load Balancing User Guide](https://docs.aws.amazon.com/elasticloadbalancing/latest/userguide/what-is-load-balancing.html)
- [Kubernetes: Services, Load Balancing, and Networking](https://kubernetes.io/docs/concepts/services-networking/)
- [Related: Scalability](../Scalability/README.md)

## Source coverage supplement

The diagram shows the basic dispatch path. One stable balancer endpoint fans traffic out to several instances of the same service.

```text
             Client
                │
                ▼
         Load Balancer
        ┌───────┼────────┐
        ▼       ▼        ▼
   Service A Service A Service A
   Instance1 Instance2 Instance3
```

Load balancing prevents one healthy instance from taking all work and redirects traffic when an instance fails. It supports horizontal growth, high availability, and zero-downtime rollouts for e-commerce, banking, SaaS, video streaming, and cloud-native systems.

Source routing guidance:

- Round robin fits similar server capacity and even, short work.
- Least connections fits long-lived or uneven work.
- Weighted round robin fits unequal hardware and gradual upgrades.
- **Internet Protocol (IP)** hash can provide session affinity for stateful applications, with the distribution costs described above.
- Layer 4 uses TCP/UDP addresses and ports; Layer 7 can route by HTTP **uniform resource locator (URL)**, host, method, header, or cookie.

Alternatives range from one server for development/small applications and DNS balancing for basic distribution to hardware appliances in enterprise data centers and software balancers in cloud-native environments.

A balancer adds infrastructure, configuration, and latency and can itself be a single point of failure without redundancy. Server-side sessions may require affinity or external storage. Health checks, routing, TLS termination, and traffic policy need explicit configuration. It does not make slow application code fast or guarantee availability when the application, database, network, or capacity is unhealthy. Service discovery identifies eligible instances; balancing decides which receives a request.

## Expanded interview questions

1. Why is balancing important and which problems does it solve?
   **Key points:** it combines instance capacity, removes failed targets, and provides controlled deployment traffic. It does not repair application or data-tier failures.
2. Layer 4 versus Layer 7; round robin versus least connections?
   **Key points:** choose the lowest layer that supports the routing policy, then choose an algorithm based on request duration, target capacity, and observed saturation.
3. What are sticky sessions and why are they discouraged in distributed systems?
   **Key points:** they bind a client to one target, which can preserve local session state but creates imbalance and weak failover. External state is usually easier to scale.
4. How do health checks work?
   **Key points:** active probes and passive error observations update eligibility. Separate liveness from readiness and allow connection draining before removal.
5. Load balancer versus API gateway versus service discovery?
   **Key points:** discovery finds candidates, balancing chooses a target, and the gateway exposes client-facing API policy.

## Professional correction

- “Incoming requests are distributed evenly” is not necessarily desirable or achievable: equal request counts can represent very unequal work. Select an algorithm from load shape and observed saturation.
- IP hashing provides affinity, not durable session correctness; proxy or **network address translation (NAT)** address changes and membership changes can remap clients.
- Zero-downtime deployment also requires readiness, connection draining, compatible application/data changes, spare capacity, and rollback—not merely a load balancer.

## Provenance

- **Source-derived:** Core diagram, benefits, limitations, algorithm fits, L4/L7 distinction, alternatives, examples, misconceptions, and questions were restored from `03-Architecture.md`.
- **Editorial:** Advanced algorithms, health semantics, production failure guidance, references, and `Professional correction` extend and qualify the source.
