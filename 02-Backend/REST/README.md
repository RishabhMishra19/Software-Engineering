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

## REST versus GraphQL decision guide

REST exposes resource representations through HTTP methods and multiple resource-oriented endpoints. [GraphQL](../GraphQL/README.md) exposes a typed schema through which clients select fields, commonly at one endpoint.

### When REST is a strong fit

- Standard CRUD or resource-oriented APIs.
- Simple contracts with mature HTTP tooling and intermediary caching.
- Independently owned service APIs and clients with broadly similar data needs.
- Operations where distinct endpoints improve monitoring and debugging.

REST may over-fetch a fixed representation or require several requests to assemble related data. Purpose-built representations can reduce calls, but they increase API surface and coupling.

### When GraphQL is a strong fit

- Web and mobile clients need different subsets of related data.
- A frontend contract aggregates several backend sources.
- Reducing client-managed round trips and payload waste materially improves the product.
- Schema discovery and additive field evolution justify additional server controls.

GraphQL query validation, cost control, authorization, caching, monitoring, and error semantics are more involved. It does not automatically make an API faster.

| Feature | REST | GraphQL |
| --- | --- | --- |
| Interface | Multiple resource endpoints | Commonly one endpoint with named operations |
| Response shape | Server-defined representation | Client-selected fields |
| Over/under-fetching | Possible | Reduced when schema and queries are well designed |
| Caching | Strong generic HTTP support | Usually client normalization, resolver, or persisted-response caching |
| Evolution | Compatible additions plus explicit strategy for breaking changes | Additive fields and deprecation; breaking changes still require migration |
| Learning and operations | Lower for standard HTTP APIs | Higher due to execution and query controls |
| Strong fit | CRUD, service APIs, cacheable resources | Related data, diverse frontends, aggregation |

REST and GraphQL can coexist: service boundaries can remain RESTful while a GraphQL gateway provides frontend composition. GraphQL is an alternative API style, not a universal replacement, and REST can return nested or related representations.

## API versioning strategy

Versioning manages breaking contract or semantic changes while existing consumers migrate. It supports backward compatibility, safe evolution, and client-controlled adoption, but every concurrently supported version adds implementation, testing, documentation, and deprecation cost.

### URI versioning

```http
GET /api/v1/users
GET /api/v2/users
```

It is visible, widely understood, simple to route, and easy to document. It changes resource URLs and can duplicate endpoint families.

### Header versioning

```http
API-Version: 2
```

It keeps URLs stable and separates resource identity from version selection, but is less visible and less convenient to test manually.

### Query-parameter versioning

```http
GET /users?version=2
```

It is easy to implement but uncommon and generally not preferred for a durable public API contract.

### Media-type content negotiation

```http
Accept: application/vnd.example.v2+json
```

It preserves resource URLs and uses HTTP negotiation, but is harder to understand, configure, and inspect.

| Strategy | Readability | Adoption | Typical guidance |
| --- | --- | --- | --- |
| URI | Excellent | High | Common, simple default |
| Header | Good | Medium | Useful when clean URLs matter |
| Query parameter | Fair | Low | Generally avoid for public versioning |
| Media type | Moderate | Medium | Advanced negotiation-driven APIs |

Create a new version for changes such as removing or renaming fields, changing required request shape, changing incompatible response structure or business semantics, or removing endpoints. Do not version merely for optional fields, performance work, compatible bug fixes, or internal implementation changes.

A safe deprecation process releases the replacement, documents the migration and deadline, continues old-version support for the announced period, measures client adoption, communicates with remaining consumers, and removes the old version only after policy conditions are met. Versioning is a mechanism, not automatic compatibility.

## Interview Questions

1. **What makes an API RESTful?** Resource identification, uniform HTTP semantics, self-contained requests, representations, cacheability, and layered components.
2. **What is idempotency?** Repeating a request has the same intended server state effect as making it once.
3. **PUT or PATCH?** PUT replaces a complete resource state; PATCH applies a partial change.
4. **When should an API be versioned?** When a breaking contract or semantic change cannot be introduced compatibly.
5. **REST or GraphQL?** REST favors HTTP semantics and caching; [GraphQL](../GraphQL/README.md) favors flexible client-selected data.
6. **How do ETags prevent lost updates?** A client submits the previously observed validator with `If-Match`; stale updates fail precondition checks.
7. **Why choose REST over GraphQL?** It is simpler to implement, cache, inspect, and operate for standard resource APIs.
8. **Can REST and GraphQL coexist?** Yes; use each at the boundary where its trade-offs fit.
9. **Does GraphQL eliminate versioning?** No; additive evolution reduces explicit versions, but breaking schema changes still require migration.
10. **Which API versioning strategy should you prefer?** URI versioning is a common simple default, but the contract, clients, and infrastructure determine the choice.
11. **How do you deprecate an API?** Publish a replacement and timeline, support migration, monitor adoption, communicate, and remove only after the policy deadline.

## Interview Tips

Use precise HTTP terminology. Explain method safety separately from idempotency, distinguish authentication failures from permission failures, and discuss retries, caching, and compatibility as end-to-end behaviors.

## References

- [RFC 9110: HTTP Semantics](https://www.rfc-editor.org/rfc/rfc9110)
- [RFC 9111: HTTP Caching](https://www.rfc-editor.org/rfc/rfc9111)
- [RFC 9457: Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)
- [RFC 5789: PATCH Method](https://www.rfc-editor.org/rfc/rfc5789)
- [OpenAPI Specification](https://spec.openapis.org/oas/latest.html)
- [Roy Fielding's REST dissertation chapter](https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm)

## Provenance

- **Source-derived:** REST/GraphQL definitions, advantages, limitations, selection criteria, comparison matrix, coexistence guidance, versioning benefits and costs, four strategy examples, comparison, deprecation process, misconceptions, and interview questions were restored from `01-Backend.md`.
- **Editorial additions:** existing HTTP semantics, caching, conditional requests, idempotency, pagination, and Problem Details guidance was retained to make the source decisions operational.
- **Professional corrections:** GraphQL does not categorically avoid over/under-fetching or eliminate versions; `example` replaces a company-like media-type placeholder; versioning is tied to breaking contracts rather than every behavior change, and no illustration claims verified company internals.
