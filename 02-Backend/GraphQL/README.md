# GraphQL

## Overview

GraphQL is a query language and execution specification for typed APIs. A schema defines object types and operations; clients select fields, and resolvers obtain the requested data. It commonly exposes one HTTP endpoint but is not limited to HTTP.

## Why do we need it?

GraphQL addresses clients with diverse data requirements, deeply related views, and frequent frontend evolution. It can reduce over-fetching, under-fetching, and client-managed request orchestration while making capabilities discoverable through a strongly typed schema.

Use it when client flexibility and data composition justify the operational cost. [REST](../REST/README.md) is often simpler for straightforward resource APIs, HTTP caching, file transfer, and independently exposed service boundaries.

## How does it work?

### Schema and execution

The schema definition language declares types, fields, arguments, queries, mutations, and subscriptions. The server parses and validates a document, selects an operation, then executes fields through resolvers. Nullability is part of the contract; a non-null field failure propagates to the nearest nullable parent.

### Data loading

Naive nested resolvers can produce N+1 database or service calls. Request-scoped batching and caching—commonly the DataLoader pattern—combine lookups without leaking data between users. Resolvers should delegate domain rules rather than become a business-logic layer.

### Security and resource control

Authentication establishes the principal; authorization must be enforced at domain or field boundaries, not merely at the endpoint. Limit depth, aliases, breadth, and estimated cost; apply timeouts, pagination, rate limits, and persisted or allowlisted operations where risk warrants.

### Caching and evolution

Generic HTTP caches cannot identify arbitrary query semantics as easily as resource URLs. Clients often use normalized entity caches, while servers cache resolver results or persisted responses. Add fields compatibly, deprecate old fields, measure usage, and remove them only after consumers migrate.

### Errors

A response may contain both `data` and `errors`, enabling partial results. Put stable machine-readable codes in error extensions and avoid exposing internal exceptions. Transport status and GraphQL execution errors serve different layers and should be monitored separately.

### Trade-offs

- Client-selected fields reduce payload waste but move complexity and cost control to the server.
- One graph simplifies consumption but can become an organizational coupling point.
- Schema evolution often avoids explicit versions but does not eliminate breaking-change management.
- Partial results improve resilience for some views but complicate client logic and observability.

## Advantages

- Precise client-selected responses.
- Typed, introspectable contract and strong tooling.
- Efficient composition of related data.
- Additive schema evolution and field-level deprecation.
- One client contract can aggregate multiple backing services.

## Limitations

- Query cost, authorization, and caching are more involved than in typical REST APIs.
- N+1 resolver behavior can overload dependencies.
- A single endpoint obscures operation-level metrics unless operation names are tracked.
- File uploads and simple cacheable resources often fit standard HTTP APIs better.
- Federation and distributed schema ownership add governance and reliability concerns.

## Best Practices

- Design a domain graph, not a direct mirror of database tables.
- Require named operations and collect operation-level latency and error metrics.
- Use request-scoped loaders for batching and caching.
- Apply cursor pagination to unbounded collections.
- Enforce authorization near protected data and prevent cross-principal cache leakage.
- Set complexity budgets, timeouts, and input size limits.
- Deprecate fields with measured migration before removal.

## Common Mistakes

- Returning unbounded lists.
- Querying the database independently in every nested resolver.
- Authorizing only the top-level query.
- Using global DataLoader caches across users or requests.
- Assuming GraphQL is automatically faster than REST.
- Treating HTTP `200` as proof that the GraphQL operation succeeded.

## Real-world examples

- A mobile client requests a compact product card while a web client requests additional inventory and merchandising fields.
- A gateway composes customer, order, and shipment data behind one graph.
- A subscription publishes status changes while queries remain the source for current state.
- Persisted operations constrain production clients to reviewed query documents.

## Interview Questions

1. **What problem does GraphQL solve?** It lets clients request typed, precise, related data through a schema-driven contract.
2. **What is the N+1 problem?** Resolving each parent independently triggers repeated downstream calls; batching combines them.
3. **How is GraphQL authorized?** Authenticate the request, then enforce permissions for each protected domain operation or field.
4. **Does GraphQL eliminate versioning?** It supports additive evolution and deprecation, but breaking changes still require migration.
5. **How do you prevent expensive queries?** Bound depth and breadth, estimate cost, paginate, rate-limit, timeout, and use persisted operations.
6. **When should REST and GraphQL coexist?** GraphQL can serve client composition while REST remains useful for service APIs and cacheable resources.

## Interview Tips

Discuss the full execution path: parse, validate, authorize, plan, resolve, batch, and observe. A strong answer covers cost control, N+1 prevention, nullability, partial errors, and schema governance—not only flexible responses.

## References

- [GraphQL Specification](https://spec.graphql.org/)
- [GraphQL Learn](https://graphql.org/learn/)
- [GraphQL over HTTP](https://graphql.github.io/graphql-over-http/draft/)
- [GraphQL security guidance](https://graphql.org/learn/security/)
- [DataLoader](https://github.com/graphql/dataloader)
