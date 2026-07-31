# Networking

> **Provenance:** Editorial addition.

## Overview

Computer networking lets running programs exchange data across machines and links that may differ in speed, capacity, and reliability. An everyday analogy is mailing a parcel: an application writes the contents, several services label and package them, transport systems choose a route, and the recipient reverses those steps. Each service has a limited responsibility, so no single protocol—a shared set of communication rules—handles the entire journey.

Instead, protocol layers divide the work. Link technologies carry local frames, Internet Protocol (IP) routes packets between networks, transport protocols connect processes, and application protocols define what messages mean.

**Prerequisites:** A process is a running program. A byte is a small unit of digital data. A packet is a bounded piece of data sent through a network. A client asks for a service, and a server provides it. The [Operating System guide](../Operating-System/README.md) explains processes and sockets, which are operating-system communication endpoints.

## Why do we need it?

Networks let independently deployed systems communicate, share resources, scale horizontally, and survive localized failures. Standard protocols make clients and services interoperable across hardware, operating systems, and administrative domains.

## How does it work?

### Layered communication

- **Link layer:** local delivery using technologies such as Ethernet and Wi-Fi, a name for a family of wireless-network standards rather than an acronym. A frame is the link layer's local unit of data.
- **Internet layer:** IP addressing and best-effort routing across networks. Best effort means the network tries to deliver a packet but does not promise delivery, order, or timing.
- **Transport layer:** Transmission Control Protocol (TCP) provides ordered, reliable byte streams. User Datagram Protocol (UDP) provides connectionless datagrams. QUIC, a secure transport protocol, provides multiplexed streams over UDP.
- **Application layer:** protocols such as the Domain Name System (DNS), Hypertext Transfer Protocol (HTTP), and Simple Mail Transfer Protocol (SMTP). Transport Layer Security (TLS) protects application traffic.

### Addressing and routing

Addressing and routing move data toward the correct process:

1. DNS resolves a human-readable name to records such as IP addresses.
2. Routers inspect routing tables and forward each packet toward its destination.
3. Network Address Translation (NAT) may translate addresses at a network boundary.
4. A port number identifies the transport endpoint on the destination host.

For example, opening the Uniform Resource Locator (URL) `https://example.com/products` is like addressing a letter to a business and then naming a department. DNS finds an IP address for `example.com`; routers move packets toward that address; TCP or QUIC connects to the server process through a port; TLS protects the exchange; and HTTP expresses the request for `/products`.

### Reliability and flow

TCP establishes connection state and numbers the bytes in its stream. The receiver acknowledges delivered data, and the sender retransmits detected loss. Flow control protects the receiver; congestion control reduces pressure on the network.

This transport reliability does not make an application operation execute exactly once. For example, a server may commit a payment but lose its response, causing the client to retry.

### Secure transport

TLS normally authenticates the server with an X.509 public-key certificate, negotiates session keys, encrypts traffic, and detects tampering. Hypertext Transfer Protocol Secure (HTTPS) is HTTP carried over TLS.

### Trade-offs

- TCP favors reliable ordered delivery; UDP favors minimal overhead and application-controlled semantics.
- Persistent connections reduce handshake cost but consume resources and require lifecycle management.
- Caching reduces latency and origin load but introduces invalidation and freshness concerns.
- Retries improve resilience to transient faults but can amplify overload and duplicate side effects.

### Edge cases and production behavior

- Packets may arrive late, twice, out of order, or not at all. TCP repairs many of these transport-level effects, but an application still needs timeouts and retry rules.
- DNS can return several addresses, and cached records can remain after a service moves. Time to live (TTL) is the record's advertised cache duration, not a guarantee that every cache refreshes at exactly that instant.
- A connection can fail after a server performs work but before the client receives the response. The client then cannot infer whether the business operation happened.
- Large packets may exceed a link's maximum transmission unit (MTU), causing fragmentation or failure when path discovery does not work.
- Production systems measure latency percentiles, connection failures, retransmissions, DNS failures, and dependency saturation because an average alone can hide severe slow requests.

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
- A service mesh adds mutual TLS (mTLS), routing, metrics, and retry policy between services. With mTLS, both peers present and validate identities.

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

- [Request for Comments (RFC) 8200: Internet Protocol version 6 (IPv6)](https://www.rfc-editor.org/rfc/rfc8200)
- [RFC 9293: TCP](https://www.rfc-editor.org/rfc/rfc9293)
- [RFC 768: UDP](https://www.rfc-editor.org/rfc/rfc768)
- [RFC 8446: TLS 1.3](https://www.rfc-editor.org/rfc/rfc8446)
- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110)
- [Cloudflare Learning Center: What is DNS?](https://www.cloudflare.com/learning/dns/what-is-dns/)
