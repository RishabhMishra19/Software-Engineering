# Networking

> **Provenance:** Editorial addition.

## Overview

Computer networking moves data between processes across unreliable, heterogeneous links. Layered protocols separate concerns: link technologies carry local frames, IP routes packets, transport protocols connect processes, and application protocols define meaning.

## Why do we need it?

Networks let independently deployed systems communicate, share resources, scale horizontally, and survive localized failures. Standard protocols make clients and services interoperable across hardware, operating systems, and administrative domains.

## How does it work?

### Layered communication

- **Link layer:** local delivery using technologies such as Ethernet and Wi-Fi.
- **Internet layer:** IP addressing and best-effort routing across networks.
- **Transport layer:** TCP provides ordered reliable byte streams; UDP provides connectionless datagrams; QUIC provides secure multiplexed streams over UDP.
- **Application layer:** DNS, HTTP, TLS, SMTP, and other domain protocols.

### Addressing and routing

DNS resolves names to records such as IP addresses. Routers forward packets toward the destination according to routing tables. NAT translates addresses at network boundaries. Ports identify transport endpoints on a host.

### Reliability and flow

TCP establishes connection state, numbers bytes, acknowledges delivery, retransmits loss, and controls flow and congestion. Reliability does not make an application operation exactly once: a response can be lost after the server commits the operation.

### Secure transport

TLS authenticates peers—normally the server with an X.509 certificate—negotiates keys, and encrypts traffic with integrity protection. HTTPS is HTTP over TLS.

### Trade-offs

- TCP favors reliable ordered delivery; UDP favors minimal overhead and application-controlled semantics.
- Persistent connections reduce handshake cost but consume resources and require lifecycle management.
- Caching reduces latency and origin load but introduces invalidation and freshness concerns.
- Retries improve resilience to transient faults but can amplify overload and duplicate side effects.

## Advantages

- Interoperability through open protocols.
- Independent scaling and deployment of services.
- Geographic distribution and fault isolation.
- Efficient resource sharing and remote access.
- Multiple reliability, latency, and security options.

## Limitations

- Latency, loss, reordering, and partitions are unavoidable.
- Bandwidth and connection capacity are finite.
- Distributed calls fail in more ways than local calls.
- Encryption protects transport, not compromised endpoints.
- DNS, caches, proxies, and NAT can obscure current state.

## Best Practices

- Set connect, request, and idle timeouts explicitly.
- Retry only transient failures with exponential backoff, jitter, and a bounded budget.
- Make retryable state-changing operations idempotent.
- Use TLS, validate certificates, and protect private keys.
- Pool connections and apply backpressure.
- Propagate correlation identifiers and measure latency by dependency.

## Common Mistakes

- Retrying every error, including validation failures.
- Omitting timeouts and allowing calls to hang indefinitely.
- Assuming a network acknowledgement proves an end-to-end business outcome.
- Ignoring DNS caching and TTL behavior during failover.
- Sending secrets in URLs, where intermediaries and logs may record them.

## Real-world examples

- A browser resolves a hostname, establishes a secure transport connection, and exchanges HTTP messages.
- A load balancer terminates TLS and distributes requests across healthy service instances.
- Video conferencing commonly uses latency-sensitive datagrams and tolerates some loss.
- A service mesh adds mutual TLS, routing, metrics, and retry policy between services.

## Interview Questions

1. **What is the difference between TCP and UDP?** TCP provides a reliable ordered stream with congestion and flow control; UDP sends independent datagrams without delivery or ordering guarantees.
2. **What happens when a URL is opened?** DNS resolution, route selection, transport and TLS setup, an HTTP exchange, then content processing and additional requests.
3. **Why are timeouts necessary?** A remote dependency may fail silently; a timeout bounds resource use and response latency.
4. **What is the difference between a switch and a router?** A switch forwards local link-layer frames; a router forwards IP packets between networks.
5. **How does TLS protect a connection?** It authenticates identities, negotiates keys, encrypts data, and detects tampering.
6. **Why can retries be dangerous?** They can duplicate non-idempotent work and intensify an overloaded dependency.

## Interview Tips

Describe networking failures explicitly: timeout, refusal, reset, loss, partial response, stale DNS, and partition. Tie each mitigation to its failure mode and acknowledge that exactly-once delivery is not supplied by TCP.

## References

- [RFC 8200: IPv6](https://www.rfc-editor.org/rfc/rfc8200)
- [RFC 9293: TCP](https://www.rfc-editor.org/rfc/rfc9293)
- [RFC 768: UDP](https://www.rfc-editor.org/rfc/rfc768)
- [RFC 8446: TLS 1.3](https://www.rfc-editor.org/rfc/rfc8446)
- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110)
- [Cloudflare Learning Center: What is DNS?](https://www.cloudflare.com/learning/dns/what-is-dns/)
