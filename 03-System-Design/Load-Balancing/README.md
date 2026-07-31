# Load Balancing

## Overview

Load balancing distributes traffic across healthy service instances to improve capacity and availability. Layer 4 balancers route using transport metadata; Layer 7 balancers understand HTTP details such as host, path, headers, and cookies.

## Why do we need it?

A single instance has finite capacity and is a failure domain. A load balancer presents a stable endpoint, spreads work, removes unhealthy instances, and enables rolling or canary deployments. It cannot fix slow code or make unhealthy dependencies highly available.

## How does it work?

The balancer obtains eligible endpoints from static configuration, DNS, or service discovery, evaluates health, and selects a target:

- **Round robin:** simple for similar short-lived requests.
- **Least connections/requests:** useful for varied or long work.
- **Weighted routing:** supports unequal capacity and gradual releases.
- **Hash/consistent hash:** preserves affinity or cache locality while limiting remapping.
- **Power of two choices:** samples endpoints and chooses the less loaded one.

Health checks should distinguish **liveness** (restart needed), **readiness** (safe for traffic), and dependency health. Passive outlier detection complements active probes. Connection draining lets in-flight requests finish during deployments.

**Production failure modes and practices**

- Deep health checks make every instance fail when one shared dependency fails; report readiness only when serving would be harmful.
- Round robin overloads slow instances because equal request counts are not equal work.
- Sticky sessions hide statefulness and create uneven load; prefer external session storage when possible.
- Failover causes a thundering herd on survivors; retain capacity headroom and ramp traffic gradually.
- Misconfigured timeouts, keep-alives, retries, or TLS termination cause connection storms and tail latency.
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

- A public ALB routes `/api` and `/static` to different target groups.
- Kubernetes Services distribute connections while an ingress controller performs host/path routing.
- A canary receives 5% weighted traffic, then expands only if SLO metrics remain healthy.

## Interview Questions

1. Layer 4 versus Layer 7 balancing?
2. Round robin versus least connections versus consistent hashing?
3. How should readiness checks behave when a database is degraded?
4. Load balancer versus API gateway versus service discovery?
5. **Interview tip:** discuss health, algorithm, state, overload, failover capacity, and what happens during deployment.

## References

- [Google Cloud: Load Balancing Overview](https://cloud.google.com/load-balancing/docs/load-balancing-overview)
- [AWS Elastic Load Balancing User Guide](https://docs.aws.amazon.com/elasticloadbalancing/latest/userguide/what-is-load-balancing.html)
- [Kubernetes: Services, Load Balancing, and Networking](https://kubernetes.io/docs/concepts/services-networking/)
- [Related: Scalability](../Scalability/README.md)

## Source coverage supplement

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
- IP hash can provide session affinity for stateful applications, with the distribution costs described above.
- Layer 4 uses TCP/UDP addresses and ports; Layer 7 can route by HTTP URL, host, method, header, or cookie.

Alternatives range from one server for development/small applications and DNS balancing for basic distribution to hardware appliances in enterprise data centers and software balancers in cloud-native environments.

A balancer adds infrastructure, configuration, and latency and can itself be a single point of failure without redundancy. Server-side sessions may require affinity or external storage. Health checks, routing, TLS termination, and traffic policy need explicit configuration. It does not make slow application code fast or guarantee availability when the application, database, network, or capacity is unhealthy. Service discovery identifies eligible instances; balancing decides which receives a request.

## Expanded interview questions

1. Why is balancing important and which problems does it solve?
2. Layer 4 versus Layer 7; round robin versus least connections?
3. What are sticky sessions and why are they discouraged in distributed systems?
4. How do health checks work?
5. Load balancer versus API gateway versus service discovery?

## Professional correction

- “Incoming requests are distributed evenly” is not necessarily desirable or achievable: equal request counts can represent very unequal work. Select an algorithm from load shape and observed saturation.
- IP hashing provides affinity, not durable session correctness; proxy/NAT address changes and membership changes can remap clients.
- Zero-downtime deployment also requires readiness, connection draining, compatible application/data changes, spare capacity, and rollback—not merely a load balancer.

## Provenance

- **Source-derived:** Core diagram, benefits, limitations, algorithm fits, L4/L7 distinction, alternatives, examples, misconceptions, and questions were restored from `03-Architecture.md`.
- **Editorial:** Advanced algorithms, health semantics, production failure guidance, references, and `Professional correction` extend and qualify the source.
