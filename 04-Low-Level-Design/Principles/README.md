# Low-Level Design Principles

## Overview

Low-level design turns requirements into objects, responsibilities, relationships, interfaces, and interactions that can evolve safely. Start with the domain and its invariants, then introduce abstractions or [design patterns](../Design-Patterns/README.md) only for observed variation or complexity. Good design favors high cohesion, deliberate coupling, explicit ownership, and understandable trade-offs over a large class diagram.

## Why do we need it?

Code written directly from nouns or use cases often accumulates:

- data-only entities and “god” services;
- accidental ownership and invalid object states;
- deep inheritance hierarchies;
- business logic coupled to frameworks or vendors;
- pattern-driven overengineering;
- unexamined concurrency and failure behavior.

A repeatable modelling process catches these problems before implementation and gives a reviewer or interviewer evidence for each design decision.

## How does it work?

### Object-oriented foundations

An object combines state with behavior that protects its invariants. A class defines the structure and behavior; an object is a runtime instance. The four commonly discussed ideas are:

- **Abstraction:** expose a useful capability while hiding irrelevant implementation detail. It is a boundary and model choice, not simply an interface.
- **Encapsulation:** keep representation and state transitions behind operations that preserve invariants. `private` fields alone are insufficient if setters permit invalid state.
- **Polymorphism:** invoke a shared contract while different implementations provide behavior. Subtypes must remain behaviorally substitutable.
- **Inheritance:** specialize a genuine “is-a” relationship under a stable contract. It couples subclasses to a base class and is not a default reuse mechanism.

```java
final class Account {
    private Money balance;

    void withdraw(Money amount) {
        if (amount.isNegativeOrZero() || balance.isLessThan(amount)) {
            throw new IllegalArgumentException("Invalid withdrawal");
        }
        balance = balance.minus(amount);
    }
}
```

The operation, not field visibility alone, protects the account invariant.

### Object modelling from requirements

Use nouns only to generate candidates; not every noun deserves a class. UI words such as “button” are not domain objects unless the domain itself concerns UI construction.

1. **Clarify use cases and constraints.** Identify actors, success criteria, failure cases, scale, persistence, and concurrency.
2. **Build a domain vocabulary.** Extract candidate entities, value objects, policies, services, and events.
3. **Separate identity from value.** Entities have continuity and identity; value objects are defined by immutable values.
4. **Find invariants.** Ask what must always be true—for example, a seat cannot be held by two active bookings.
5. **Assign behavior.** Place behavior with the information and invariant it governs; use a domain service only when the operation does not naturally belong to one object.
6. **Model relationships and cardinalities.** Ask who references whom, who owns lifecycle, and whether navigation is needed in one or both directions.
7. **Walk concrete scenarios.** Trace success, rejection, retry, cancellation, and concurrent requests.

For seat booking, `ShowSeat` is often more precise than a reusable physical `Seat`: availability belongs to a particular show. A `Booking` can own transitions such as `confirm()` and `cancel()`, while an application service coordinates locking, payment, and persistence.

### Association, aggregation, and composition

These terms describe modelling semantics, not whether Java uses a field.

- **Association** is any meaningful relationship or reference.
- **Shared aggregation** is UML's weak whole–part relationship: a part may be shared and has an independent lifecycle. Its semantics are intentionally weak, so a plain association is often clearer.
- **Composition** is exclusive whole–part ownership: a part belongs to at most one composite at a time and is normally created, moved, or destroyed with that whole.

`Ride` referencing an existing `Driver` and `Vehicle` is usually association, not composition: the ride does not own their lifecycles. `Order` and its `OrderLine` values are a stronger composition candidate. “Has-a” does not automatically mean composition, and deletion in a database is not the sole test; use domain lifecycle and ownership.

### Composition versus inheritance

Favor composition when behavior should vary independently, at runtime, or along multiple dimensions. A `FareCalculator` can receive a `FarePolicy`; it does not need subclasses for every vehicle × city × promotion combination.

Choose inheritance when:

- the subtype is genuinely substitutable for the base type;
- the base contract and invariants are stable;
- shared implementation is cohesive and intended for extension;
- the hierarchy remains shallow and understandable.

“Favor composition over inheritance” is guidance, not a ban. Template Method, framework extension points, and closed taxonomies can be reasonable inheritance uses. Composition also has costs: more collaborators, wiring, and runtime interaction.

### Cohesion and coupling

**Cohesion** measures how strongly a module's responsibilities belong together. **Coupling** measures how much one module depends on another's decisions. Aim for high functional cohesion and only the coupling the domain requires.

Useful questions:

- Do the methods protect the same invariant or serve the same actor?
- Does a small change force edits or tests across unrelated modules?
- Does the consumer depend on a stable capability or on provider details?
- Is temporal coupling hidden—must methods be called in an undocumented order?
- Is data coupling excessive—does a method receive a large object to use one field?

Low coupling does not mean no dependencies. A clear direct dependency is often better than a generic event bus or service locator that hides the relationship.

### Interfaces and abstract classes

Use an interface for a capability required by clients, especially when implementations vary or a boundary must isolate infrastructure. Do not create `IUserService` solely because `UserService` exists. A concrete class is sufficient when there is no useful substitution boundary.

Use an abstract class when related implementations need shared state, construction rules, protected hooks, or a partially implemented invariant. In Java, interfaces can define abstract, `default`, `static`, and private methods and constants, but not per-instance mutable fields; a class can implement multiple interfaces and extend one class.

Design contracts around client needs. `ChargePort` and `RefundPort` may be safer than a broad `PaymentGateway` that forces unsupported methods.

### Dependency inversion, injection, and IoC

A dependency is any collaborator a component needs. **Dependency inversion** says policy should rely on stable abstractions rather than volatile details. **Dependency injection** supplies collaborators from outside instead of constructing them inside business logic. **Inversion of control** is broader: a framework or external mechanism controls part of program flow or construction.

```java
final class CheckoutService {
    private final PaymentPort payments;

    CheckoutService(PaymentPort payments) {
        this.payments = payments;
    }
}
```

Prefer constructor injection for required dependencies: it makes valid construction explicit and supports immutable fields and unit tests. Setter injection can represent a genuinely optional or reconfigurable dependency but permits partially configured objects. Field injection hides requirements and makes isolated construction harder.

DI does not require a container, and injecting a concrete class can still be useful. Conversely, injecting an interface does not automatically satisfy dependency inversion if that interface mirrors a vendor API or is owned by the wrong layer.

### Recurring design decisions

| Decision | Prefer the first option when | Prefer the second option when |
| --- | --- | --- |
| Entity vs domain service | behavior protects one entity/aggregate's invariant | behavior spans concepts and belongs to none naturally |
| Interface vs abstract class | clients need a capability contract or multiple implementations | related subclasses need shared state, hooks, or implementation |
| Composition vs inheritance | dimensions vary independently or substitutability is doubtful | a stable, genuine subtype hierarchy exists |
| Factory vs Builder | choose or centralize which product to create | assemble one complex product with optional parts and validation |
| Strategy vs State | a client/configuration selects an algorithm | a context's lifecycle changes its permitted behavior |
| Observer vs brokered pub/sub | in-process publisher-to-subscriber notification is acceptable | producers and consumers need broker-mediated temporal or network decoupling |
| Adapter vs Facade | translate an incompatible contract | provide a simpler entry point to a subsystem |

These are continuums, not keyword rules. For example, an application service may be stateful within a unit of work, and an entity may legitimately delegate complex policy.

### Pattern selection

Select a pattern only after identifying the pressure:

1. Describe the changing or difficult part without pattern names.
2. Implement the simplest design that satisfies current requirements.
3. Identify expected variation, lifecycle, integration, hierarchy, or workflow pressure.
4. Compare the pattern with a simpler function, data-driven table, or direct composition.
5. State both the benefit and the new indirection or operational cost.

Use the [Design Patterns guide](../Design-Patterns/README.md) for selection signals and mechanics.

### Interview workflow

1. **Requirements:** clarify scope, actors, core use cases, exclusions, scale, consistency, and failure expectations.
2. **Scenarios:** write a happy path and important failure/follow-up flows.
3. **Model:** identify entities, value objects, responsibilities, invariants, relationships, and cardinalities.
4. **APIs:** derive operations from use cases rather than inventing getters and services.
5. **Interactions:** use a [sequence diagram](../UML/README.md) for the critical flow and a state diagram for lifecycle-heavy objects.
6. **Patterns:** introduce only where a concrete design pressure warrants one.
7. **Concurrency and persistence:** identify shared mutable resources, transaction boundaries, idempotency, locks/version checks, retries, and uniqueness constraints.
8. **Trade-offs and follow-ups:** explain alternatives and how the design accommodates likely changes.

For a ticketing system, explicitly discuss concurrent seat claims. An in-memory synchronized method is not enough across processes; a database constraint, conditional update, pessimistic lock, or version check may enforce the invariant. Define lock expiry and payment failure compensation.

### Common mistakes and corrections

- **Starting with classes:** first establish requirements, invariants, and scenarios.
- **Turning every noun into a class:** retain domain concepts with identity, value, behavior, or an important role.
- **Anemic entities plus a god service:** move invariant-preserving behavior toward the owning model without forcing all orchestration into entities.
- **Pattern-first design:** state the pressure and compare the simplest alternative.
- **Large type/status conditionals:** consider polymorphism only if the variation is open and behavior-rich; an enum or table may be clearer for closed data.
- **Inheritance for reuse:** validate substitutability and prefer delegation for independent variation.
- **Interfaces everywhere:** create contracts at meaningful client, test, or integration boundaries.
- **Ignoring concurrency:** identify shared state and enforce correctness at the authoritative store.
- **Assuming DI solves architecture:** dependency direction and contract ownership still require design.
- **God facade or generic manager:** split by cohesive use case or invariant, not arbitrary size.
- **No trade-off discussion:** every abstraction adds types, indirection, and debugging cost.
- **Overengineering:** defer speculative extension points and refactor when evidence appears.

## Advantages

- Produces models that communicate domain intent.
- Keeps invariants close to the state they govern.
- Makes variation and integration boundaries testable.
- Reduces hierarchy explosion and accidental vendor coupling.
- Provides a disciplined interview and review process.

## Limitations

- Domain boundaries and expected variation require judgment; noun extraction is only a starting aid.
- More objects and indirection can obscure simple workflows.
- Rich models can be awkward across persistence or serialization boundaries.
- Composition and DI increase wiring and lifecycle management.
- LLD cannot solve cross-service consistency, capacity, or operational concerns by itself.

## Real-world examples

- **Ticketing:** `ShowSeat` owns per-show availability; booking orchestration uses an atomic store operation to prevent double booking.
- **Payments:** checkout depends on a client-shaped `PaymentPort`; provider adapters translate external APIs.
- **Ride pricing:** a composed `FarePolicy` supports runtime variation without subclass combinations.
- **Order lifecycle:** explicit transitions preserve rules such as “delivered orders cannot be cancelled”; use State only when behavior is complex enough to justify classes.
- **Document processing:** a stable workflow with a few hooks can use Template Method; independently selectable workflows may favor Strategy.

## Interview Questions

1. How do you move from requirements to candidate objects without creating a class for every noun?
2. What is the difference between association, shared aggregation, and composition?
3. When is inheritance preferable to composition?
4. How do high cohesion and low coupling influence responsibility assignment?
5. When should behavior live on an entity versus a domain or application service?
6. Compare interface and abstract class semantics in modern Java.
7. Explain DI, DIP, and IoC with one example.
8. How would you prevent two users from booking the same seat?
9. Which design patterns would you avoid until a concrete variation appears?
10. **Interview tip:** narrate assumptions and invariants, walk one end-to-end scenario, then justify each abstraction with a requirement and a trade-off.

## References

- [Eric Evans, *Domain-Driven Design Reference*](https://www.domainlanguage.com/ddd/reference/)
- [Martin Fowler: UML Aggregation and Composition](https://martinfowler.com/bliki/AggregationAndComposition.html)
- [Martin Fowler: Dependency Injection](https://martinfowler.com/articles/injection.html)
- [Oracle Java Tutorials: Interfaces and Inheritance](https://docs.oracle.com/javase/tutorial/java/IandI/index.html)
- [SEI CERT: Java Concurrency Guidelines](https://wiki.sei.cmu.edu/confluence/display/java/Concurrency)
- [Related: SOLID](../SOLID/README.md)
- [Related: UML](../UML/README.md)
- [Related: Design Patterns](../Design-Patterns/README.md)
