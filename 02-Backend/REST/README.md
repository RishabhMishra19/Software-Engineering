# REST

## Overview

Representational State Transfer (REST) is an architectural style for networked systems. A RESTful HTTP API exposes resource representations through uniform HTTP semantics, keeps requests self-contained, and allows intermediaries to cache responses where permitted.

## Why do we need it?

REST aligns application APIs with standardized, widely implemented web semantics. It offers a simple contract for heterogeneous clients, strong tooling, transparent intermediaries, and effective HTTP caching. It is often a good fit for resource-oriented CRUD, public APIs, and independently owned services.

## How does it work?

### Resources and methods

Identify resources with stable URIs and use methods according to their semantics:

- `GET` retrieves a representation and is safe and idempotent.
- `POST` submits processing or creates a subordinate resource; it is not inherently idempotent.
- `PUT` replaces or creates the state at a known URI and is idempotent.
- `PATCH` applies a partial modification; idempotency depends on the patch format.
- `DELETE` requests removal and is idempotent by defined intent.

Use status codes consistently: `2xx` success, `3xx` redirection, `4xx` client-side conditions, and `5xx` server failures. `401` means authentication is required or invalid; `403` means the request is understood but not permitted.

### Representations and evolution

Representations are negotiated through media types. Preserve backward compatibility by adding optional fields, using tolerant readers, and publishing deprecation policy. Introduce a new version only for breaking contract or semantic changes. URI versioning is visible and easy to route; media-type or header versioning keeps resource identifiers stable but is less obvious.

### Caching and concurrency

`Cache-Control`, validators such as `ETag`, and conditional requests reduce latency and origin load. Conditional updates with `If-Match` can prevent lost updates. Cache keys must account for representation variants and authorization boundaries.

### Reliability

Clients should set timeouts and retry only transient failures. Safe and idempotent methods are easier to retry; state-changing operations can use idempotency keys where duplicate submission is possible. Pagination must define stable ordering; cursor pagination avoids drift and deep-offset cost for changing large datasets.

### Error contracts

Return machine-readable, stable error types with safe detail and correlation metadata. Centralize mapping from domain failures to HTTP; do not expose stack traces, SQL, or credentials. RFC 9457 Problem Details is a standard representation.

### Trade-offs

- Multiple focused endpoints are cacheable and observable but can require extra round trips.
- Coarse representations reduce calls but increase coupling and payload size.
- URI versioning is explicit but duplicates routes; negotiation-based versioning is cleaner but harder to inspect.
- Offset pagination is simple; cursor pagination is more stable and scalable but cannot jump arbitrarily.

## Advantages

- Uses mature HTTP infrastructure, semantics, caches, and tooling.
- Simple to understand and debug through distinct resource endpoints.
- Supports independent clients and services.
- Strong fit for cacheable and resource-oriented operations.
- Broad support in gateways, monitoring, documentation, and testing tools.

## Limitations

- Fixed representations can over-fetch data.
- Related views may require multiple requests or purpose-built endpoints.
- Breaking changes require migration and sometimes explicit versions.
- Poor resource design degrades into action-heavy RPC over HTTP.
- Exactly-once business execution is not guaranteed by HTTP.

## Best Practices

- Design around domain resources and standard method semantics.
- Document schemas and behavior with OpenAPI.
- Validate requests at the boundary and enforce domain rules in services.
- Use consistent status codes and Problem Details errors.
- Support conditional requests, caching, and idempotency where applicable.
- Define pagination limits, ordering, and filtering rules.
- Authenticate every protected request and authorize the concrete resource action.

## Common Mistakes

- Using `POST` for every operation or encoding verbs in every URI.
- Returning `200 OK` for errors.
- Retrying non-idempotent requests without a deduplication strategy.
- Versioning for every additive change.
- Leaking internal exception text.
- Caching personalized responses without correct `private`, `Vary`, and authorization handling.

## Real-world examples

- `GET /orders/{id}` retrieves an order with an `ETag`.
- `PUT /profiles/{id}` replaces a known profile representation.
- `POST /payments` accepts an idempotency key to deduplicate retried submissions.
- `GET /events?after=<cursor>&limit=50` provides stable cursor pagination.

## Interview Questions

1. **What makes an API RESTful?** Resource identification, uniform HTTP semantics, self-contained requests, representations, cacheability, and layered components.
2. **What is idempotency?** Repeating a request has the same intended server state effect as making it once.
3. **PUT or PATCH?** PUT replaces a complete resource state; PATCH applies a partial change.
4. **When should an API be versioned?** When a breaking contract or semantic change cannot be introduced compatibly.
5. **REST or GraphQL?** REST favors HTTP semantics and caching; [GraphQL](../GraphQL/README.md) favors flexible client-selected data.
6. **How do ETags prevent lost updates?** A client submits the previously observed validator with `If-Match`; stale updates fail precondition checks.

## Interview Tips

Use precise HTTP terminology. Explain method safety separately from idempotency, distinguish authentication failures from permission failures, and discuss retries, caching, and compatibility as end-to-end behaviors.

## References

- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110)
- [RFC 9111: HTTP Caching](https://www.rfc-editor.org/rfc/rfc9111)
- [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)
- [RFC 5789: PATCH Method](https://www.rfc-editor.org/rfc/rfc5789)
- [OpenAPI Specification](https://spec.openapis.org/oas/latest.html)
- [Roy Fielding's REST dissertation chapter](https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm)
