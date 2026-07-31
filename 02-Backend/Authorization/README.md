# Authorization

## Overview

Authorization answers “May this identity do this action to this thing right now?” Think of a hotel key card: proving a guest's identity does not make every room accessible, and access may depend on the assigned room, time, and staff role. Technically, authorization decides whether a principal may perform a specific action on a specific resource in the current context. It follows [authentication](../Authentication/README.md) but must not rely solely on a client-provided role or hidden user-interface control.

**Prerequisites:** A principal is an authenticated human, service, or device. A resource is the protected object, such as one document or payment. A policy is a rule that permits or denies an action. A tenant is one customer or organization whose data must remain isolated from others. Representational State Transfer (REST) and GraphQL are two application programming interface styles through which protected operations may enter.

## Why do we need it?

Authenticated users and services should not automatically access all data or operations. Authorization enforces least privilege, tenant isolation, separation of duties, ownership, regulatory boundaries, and safe delegation.

## How does it work?

An authorization decision combines:

- **Principal:** authenticated user, service, or device.
- **Action:** read, create, approve, delete, administer, or another domain operation.
- **Resource:** concrete object and its ownership, tenant, or classification.
- **Context:** time, network, authentication strength, request purpose, or risk.
- **Policy:** the rule that permits or denies the combination.

### Policy models

- **Role-based access control (RBAC):** roles group permissions. It is understandable and efficient but can cause role explosion when context and object ownership vary.
- **Attribute-based access control (ABAC):** policies evaluate principal, resource, action, and environment attributes. It is expressive but harder to reason about and test.
- **Relationship-based access control:** permissions derive from relationships such as owner, editor, parent organization, or group membership.
- **Access-control lists (ACLs):** resources list principals or groups and allowed operations; direct but expensive to manage at very large scale.

Real systems often combine models: roles grant broad capability, while attributes and relationships restrict the concrete resource.

For example, a support agent's role may grant the general ability to view orders, but an attribute rule restricts access to the agent's tenant and a relationship rule restricts it to assigned cases. The server loads trusted order ownership, evaluates all rules, defaults to denial if none permit the action, and records a safe audit event.

### Enforcement

Enforce policy server-side at every entry point, ideally near the domain operation so REST, GraphQL, jobs, and message consumers share the same rule. Default deny when no policy matches. Filter collections and authorize each object; checking only the route commonly causes broken object-level authorization.

Centralized policy decision points improve consistency, while local enforcement points retain request and resource context. Cache decisions only when invalidation, policy version, principal, tenant, action, and resource are part of the design.

### Audit and administration

Record sensitive decisions and administrative policy changes with actor, action, resource, result, and correlation context without logging secrets. Policy changes need review, versioning, and safe rollout. Emergency access should be time-bound, monitored, and separately audited.

### Trade-offs

- Coarse RBAC is easy to operate but may overgrant; fine-grained policies improve control at greater complexity.
- Central policy services improve consistency but add latency and availability dependencies.
- Cached decisions reduce latency but risk stale permissions.
- Denormalized relationship data speeds checks but requires reliable synchronization.

### Edge cases and production behavior

- A user can lose a role while an older token still contains it. Token lifetime and policy lookup determine the revocation delay.
- Listing resources requires filtering before pagination; authorizing only the returned page can leak counts or create short pages unpredictably.
- “Resource does not exist” and “resource is forbidden” responses can reveal sensitive identifiers, so public error behavior may deliberately be indistinguishable.
- A central policy service can fail. The system must define whether each action fails closed, uses a bounded cache, or has an exceptional emergency path.
- Production teams record policy version and relevant inputs so a later audit can explain why a decision was made.

## Advantages

- Enforces least privilege and tenant isolation.
- Separates identity verification from policy.
- Enables consistent policy across interfaces.
- Supports auditing and separation of duties.
- Fine-grained models can represent ownership and delegation.

## Limitations

- Policy complexity grows with resources, relationships, and exceptions.
- Revocation propagation and cache invalidation are difficult.
- Incorrect object scoping can expose data despite route-level checks.
- External policy engines become critical dependencies.
- Audit logs explain decisions only if policy inputs and versions are captured.

## Best Practices

- Default deny and grant the minimum actions and resource scope.
- Authorize the concrete domain action, not only an endpoint or screen.
- Derive tenant and ownership scope from trusted server-side data.
- Keep policy definitions reviewable, versioned, and covered by positive and negative tests.
- Re-evaluate high-risk operations with fresh attributes or stronger authentication.
- Design revocation latency explicitly and avoid long-lived embedded permissions.
- Audit sensitive grants, denials, and policy changes.

## Common Mistakes

- Confusing successful authentication with permission.
- Trusting a role, tenant identifier, or resource owner sent by the client.
- Checking list access but not filtering each returned object.
- Enforcing policy only in the user interface.
- Embedding long-lived roles in tokens without a freshness or revocation strategy.
- Scattering inconsistent permission checks across controllers and resolvers.
- Returning detailed denial messages that reveal sensitive resource existence.

## Real-world examples

- An order owner may view an order; a support role may view it only within an assigned tenant.
- A payment creator cannot approve the same payment, enforcing separation of duties.
- A document inherits access from its workspace while an explicit restriction narrows access.
- A service account can publish to one message topic but cannot administer the broker.

## Interview Questions

1. **RBAC versus ABAC?** RBAC assigns permissions through roles; ABAC evaluates attributes and context for more granular decisions.
2. **What is least privilege?** Grant only the actions and resource scope required for the task and duration.
3. **Where should authorization be enforced?** Server-side at every entry path, close to the domain operation and concrete resource.
4. **What is broken object-level authorization?** A caller changes an object identifier and accesses a resource without an ownership or scope check.
5. **How do you handle permission caching?** Key by all decision inputs, use short lifetimes or invalidation, and define acceptable revocation delay.
6. **Why can roles in JSON Web Tokens (JWTs) become unsafe?** The claims remain stale until token expiry unless introspection, short lifetimes, or revocation mechanisms intervene.
7. **How do you test policy?** Cover allowed and denied combinations, tenant boundaries, ownership changes, defaults, and policy-version transitions.

## Interview Tips

Answer with principal, action, resource, context, and policy. Include default-deny behavior, object-level checks, tenant isolation, revocation latency, and auditability. Distinguish where policy is decided from where it is enforced.

## References

- [National Institute of Standards and Technology (NIST) Special Publication (SP) 800-162: Attribute Based Access Control](https://csrc.nist.gov/pubs/sp/800/162/upd2/final)
- [NIST role-based access control (RBAC) project](https://csrc.nist.gov/projects/role-based-access-control)
- [Open Worldwide Application Security Project (OWASP) Authorization Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html)
- [OWASP application programming interface (API) Security Top 10](https://owasp.org/API-Security/)
- [Cedar policy language documentation](https://docs.cedarpolicy.com/)
- [Open Policy Agent documentation](https://www.openpolicyagent.org/docs/latest/)
