# Design Patterns

## Overview

Design patterns are named, reusable arrangements of responsibilities and collaborations. They are not copy-paste implementations or goals by themselves. Start with a requirement and design pressure, use the simplest solution that works, and introduce a pattern when its benefits exceed its extra types and indirection.

This guide consolidates 15 patterns:

- **Creational:** [Factory](#factory), [Abstract Factory](#abstract-factory), [Builder](#builder), [Singleton](#singleton)
- **Structural:** [Adapter](#adapter), [Facade](#facade), [Decorator](#decorator), [Proxy](#proxy), [Composite](#composite)
- **Behavioral:** [Strategy](#strategy), [State](#state), [Observer](#observer), [Chain of Responsibility](#chain-of-responsibility), [Command](#command), [Template Method](#template-method)

## Why do we need it?

Patterns provide a shared vocabulary and tested starting points for recurring pressures: selecting algorithms, controlling object creation, translating third-party APIs, composing optional behavior, representing trees, or managing lifecycles. Their value is in making trade-offs explicit. Applying a pattern without its pressure creates accidental complexity.

## How does it work?

### Selection map

| Requirement signal | Consider | Verify before choosing |
| --- | --- | --- |
| Select one interchangeable algorithm | Strategy | Is variation open and behavior-rich? |
| Behavior and valid operations change with lifecycle | State | Would an enum/table remain clearer? |
| One in-process event has multiple listeners | Observer | Are delivery guarantees or a broker required? |
| Choose which concrete product to construct | Factory | Is creation actually complex or scattered? |
| Create compatible families of products | Abstract Factory | Must products remain family-consistent? |
| Construct one complex value with optional fields | Builder | Would a constructor or named factory suffice? |
| Exactly one instance per defined scope | Singleton | Can lifecycle be owned by DI instead? |
| Translate an incompatible external contract | Adapter | Is translation, not simplification, the problem? |
| Offer a simpler workflow over a subsystem | Facade | Will it become a god orchestrator? |
| Stack optional behavior around one contract | Decorator | Does wrapper order affect semantics? |
| Stand in for an object to control access | Proxy | Is access/lifecycle control the primary intent? |
| Treat leaves and nested groups uniformly | Composite | Is the domain genuinely recursive? |
| Let ordered handlers process or forward | Chain of Responsibility | Must every step always execute? |
| Store, queue, schedule, retry, or undo an action | Command | Does the action need first-class identity/state? |
| Fix an algorithm skeleton and vary selected steps | Template Method | Is inheritance an acceptable coupling? |

Large conditionals are a symptom, not a diagnosis. A closed mapping from enum to value may be clearer than polymorphism. “Third party,” “notification,” or “workflow” alone does not prove a pattern.

### Factory

**Selection signals:** callers should not select concrete classes; creation depends on configuration or input; constructor and wiring logic is scattered.

**Mechanics:** a creator function or object returns a product through a stable contract. “Factory” is an umbrella term; a simple factory centralizes selection, while GoF Factory Method lets subclasses override a creation step.

```java
interface PaymentGateway { Receipt charge(Charge request); }

final class GatewayFactory {
    PaymentGateway create(Provider provider) {
        return switch (provider) {
            case STRIPE -> new StripeAdapter(new StripeClient());
            case RAZORPAY -> new RazorpayAdapter(new RazorpayClient());
        };
    }
}
```

**Production examples:** payment or notification provider selection, parser selection by media type, cloud-storage clients, JDBC driver-created connections.

**Trade-offs:** centralizes construction and shields clients, but can become a growing switch or service locator. Registration maps or DI configuration may be better for plugin ecosystems.

**Common mistakes:** creating a factory for one trivial implementation; putting business behavior in the factory; claiming that merely moving a conditional satisfies OCP; confusing Factory with Builder.

**Interview question:** When would you use Factory rather than direct construction?  
**Answer:** when selection or construction is volatile enough that callers should depend only on the product contract.

### Abstract Factory

**Selection signals:** a system must create several related products from one provider, platform, or theme, and mixing families would be invalid.

**Mechanics:** an abstract factory exposes one creation operation per product role; each concrete factory supplies a compatible family.

```java
interface PaymentSuite {
    PaymentGateway gateway();
    RefundService refunds();
    WebhookVerifier webhooks();
}
```

A `StripeSuite` returns Stripe-compatible products; a `RazorpaySuite` returns Razorpay-compatible products.

**Production examples:** provider SDK suites, cross-platform UI widgets, database-specific connection/transaction/query components.

**Trade-offs:** enforces family consistency and makes switching families easy, but adding a new product role changes every factory. It is strongest when families vary more often than product roles.

**Common mistakes:** using it for one product; grouping unrelated objects; leaking concrete family types to clients; calling a single-product factory “Abstract Factory.”

**Interview question:** Factory versus Abstract Factory?  
**Answer:** a factory selects or constructs a product; Abstract Factory coordinates a family of related product roles.

### Builder

**Selection signals:** construction has many optional or named values, ordering constraints, staged setup, validation, or multiple representations.

**Mechanics:** a builder accumulates construction state through fluent or staged operations; `build()` validates invariants and returns a complete product. Prefer producing an immutable object and avoid returning the mutable builder's internal object directly.

```java
SearchRequest request = SearchRequest.builder("laptop")
    .category("electronics")
    .priceRange(Money.of(500), Money.of(1500))
    .sortBy(PRICE_ASC)
    .build();
```

**Production examples:** HTTP requests, immutable configuration, search criteria, protocol messages, test fixtures.

**Trade-offs:** improves readability and centralizes validation but adds boilerplate and can hide required fields unless the API or type system enforces them.

**Common mistakes:** builders for tiny values; allowing `build()` to create invalid objects; reusing a mutable builder unsafely; confusing construction steps with choosing a product family.

**Interview question:** Why is Builder common in Java?  
**Answer:** it compensates for the lack of named/default parameters and supports readable construction of immutable values.

### Singleton

**Selection signals:** the domain or runtime requires one instance within an explicitly defined scope and ownership of that lifecycle must be controlled.

**Mechanics:** restrict construction and provide one instance. In Java, an enum singleton gives safe initialization and serialization for process-wide instances:

```java
enum ConfigurationRegistry {
    INSTANCE;
}
```

Often the better production implementation is a single application-scoped object owned by a DI container and injected into consumers—without global lookup.

**Production examples:** process-level registries or immutable configuration snapshots. Loggers, pools, and caches may be singletons within a container scope, but that is a deployment choice rather than an inherent property.

**Trade-offs:** controlled lifecycle can prevent duplicate resources, but global access hides dependencies, complicates isolation and tests, and does not mean one instance across processes, class loaders, tenants, or a cluster.

**Common mistakes:** treating services, users, orders, or mutable domain state as singletons; unsafe lazy initialization; using double-checked locking without `volatile`; a global god object; claiming a Spring singleton is globally unique.

**Interview question:** How would you make a thread-safe Java singleton?  
**Answer:** prefer enum or initialization-on-demand holder; use `volatile` with correct double-checked locking only when lazy construction specifically requires it.

### Adapter

**Selection signals:** existing or third-party code cannot be changed and exposes incompatible methods, data, errors, or protocols.

**Mechanics:** an adapter implements the client's target contract, delegates to the adaptee, and translates requests, responses, errors, and semantic differences.

```java
final class StripeAdapter implements PaymentPort {
    private final StripeClient stripe;

    public Receipt charge(Charge request) {
        StripeResponse response = stripe.createCharge(toStripe(request));
        return toReceipt(response);
    }
}
```

**Production examples:** payment gateways, cloud providers, legacy repositories, anti-corruption layers, external messaging APIs.

**Trade-offs:** isolates vendor change and keeps business language clean, but translation can be lossy and adapters require contract and integration tests.

**Common mistakes:** leaking vendor DTOs through the target interface; changing third-party code; swallowing error semantics; placing conversion logic in business services; confusing translation with Facade simplification.

**Interview question:** Adapter versus Facade?  
**Answer:** Adapter makes an incompatible interface conform to a target contract; Facade provides a simpler entry point to a subsystem.

### Facade

**Selection signals:** a client coordinates many subsystem calls for one cohesive use case or needs a stable, simplified entry point.

**Mechanics:** a facade exposes use-case-oriented operations and orchestrates existing subsystem interfaces. Subsystems may remain available to advanced clients.

```java
final class CheckoutFacade {
    CheckoutResult checkout(Cart cart) {
        inventory.reserve(cart);
        Payment payment = payments.authorize(cart.total());
        Shipment shipment = shipping.create(cart);
        return new CheckoutResult(payment, shipment);
    }
}
```

**Production examples:** checkout, travel booking, SDK entry points, application-service APIs, `JdbcTemplate`-style wrappers over lower-level APIs.

**Trade-offs:** reduces client coupling and centralizes workflow, but can become a bottleneck or god facade. Transaction, compensation, and partial-failure semantics must be explicit.

**Common mistakes:** moving all domain logic into the facade; exposing a grab bag of unrelated methods; constructing dependencies internally; treating a network API gateway as automatically equivalent to the GoF pattern.

**Interview question:** Can a Facade coexist with direct subsystem access?  
**Answer:** yes; it offers a convenient boundary and does not necessarily prohibit specialized access.

### Decorator

**Selection signals:** optional responsibilities must be combined at runtime around the same contract without a subclass for every combination.

**Mechanics:** a decorator implements the component contract, contains another component, and adds behavior before or after delegation.

```java
PaymentProcessor processor =
    new MetricsProcessor(
        new RetryProcessor(
            new BasicPaymentProcessor()));
```

**Production examples:** Java I/O streams, HTTP client middleware, metrics/logging/retry wrappers, notification enrichment.

**Trade-offs:** supports flexible composition and focused wrappers, but order can change behavior, object graphs become hard to inspect, and identity/equality can be surprising.

**Common mistakes:** decorators that do not preserve the component contract; using retry around non-idempotent actions without safeguards; putting unrelated business logic in cross-cutting wrappers; confusing Decorator with Proxy.

**Interview question:** Decorator versus inheritance?  
**Answer:** Decorator composes responsibilities per object and at runtime; inheritance fixes them in the class hierarchy.

### Proxy

**Selection signals:** a stand-in must control access, remote communication, lazy creation, caching, authorization, or lifecycle while presenting the real subject's contract.

**Mechanics:** client and proxy share a subject interface; the proxy decides when and how to delegate to the real subject.

```java
final class CachingProductProxy implements ProductCatalog {
    public Product get(ProductId id) {
        return cache.get(id).orElseGet(() -> loadAndCache(id));
    }
}
```

Common variants include virtual, protection, remote, and caching proxies.

**Production examples:** ORM lazy-loading proxies, RPC stubs, authorization wrappers, cache fronts, framework-generated transactional proxies.

**Trade-offs:** centralizes access policy and can defer expensive work, but adds latency and hidden behavior. Caching introduces invalidation and consistency concerns; remote proxies cannot make network calls behave like local calls.

**Common mistakes:** business logic in the proxy; transparent retries that duplicate side effects; equality or serialization surprises from generated proxies; stacking opaque proxy layers.

**Interview question:** Proxy versus Decorator?  
**Answer:** their structure is similar; Proxy primarily controls access or lifecycle, while Decorator intentionally composes additional responsibilities.

### Composite

**Selection signals:** the domain is a recursive part–whole hierarchy and clients should perform the same operation on a leaf or a group.

**Mechanics:** leaf and composite implement a common component contract; composites contain components and recursively delegate or aggregate results.

```java
interface FileSystemNode { long size(); }

final class Folder implements FileSystemNode {
    private final List<FileSystemNode> children;
    public long size() {
        return children.stream().mapToLong(FileSystemNode::size).sum();
    }
}
```

**Production examples:** file trees, UI component trees, organization hierarchies, product categories, expression trees.

**Trade-offs:** simplifies recursive client code, but a common interface may expose operations meaningless for leaves. Very deep or cyclic graphs require iterative traversal or cycle protection.

**Common mistakes:** using Composite for a flat collection; exposing mutable child collections; allowing cycles in a structure assumed to be a tree; forcing child-management methods onto leaves without a deliberate safe/transparent design choice.

**Interview question:** Why do leaf and composite share an interface?  
**Answer:** so recursive clients can treat individual and grouped objects uniformly.

### Strategy

**Selection signals:** one task has interchangeable algorithms or policies selected by a client, configuration, or runtime context.

**Mechanics:** a context delegates the varying operation to a strategy contract; selection occurs outside the strategy, often through configuration or a factory.

```java
interface FarePolicy { Money calculate(Ride ride); }

final class FareCalculator {
    private final FarePolicy policy;
    Money calculate(Ride ride) { return policy.calculate(ride); }
}
```

**Production examples:** ride pricing, discounts, payment routing, tax calculation, matching, compression.

**Trade-offs:** isolates and tests algorithms independently, but increases objects and requires a selection mechanism. Lambdas or functions may be sufficient when strategies have little state or lifecycle.

**Common mistakes:** one strategy with no expected variation; selecting via type checks inside every strategy; copying shared algorithm steps; assuming a strategy removes all conditionals.

**Interview question:** Strategy versus State?  
**Answer:** a client/configuration chooses Strategy to accomplish a task; State represents a context's current lifecycle condition and usually drives transitions internally.

### State

**Selection signals:** an object's allowed operations and behavior vary substantially by lifecycle state, and status checks are distributed.

**Mechanics:** a context delegates state-specific behavior to a state object; transitions replace the current state explicitly. Keep transition rules visible and preserve aggregate invariants.

```java
interface OrderState {
    void cancel(Order context);
    void dispatch(Order context);
}
```

**Production examples:** orders, bookings, vending machines, documents, ATM sessions.

**Trade-offs:** localizes complex state behavior and removes scattered conditionals, but creates classes and may obscure the full transition graph. Persisting polymorphic state objects can complicate mapping; a status plus transition table may be better for simple workflows.

**Common mistakes:** a class per status when behavior does not differ; state classes plus the original conditionals; transitions available from everywhere; no handling of invalid transitions or concurrent updates.

**Interview question:** Who changes the context's current state?  
**Answer:** either the state or context can coordinate it; choose one consistent policy and make allowed transitions explicit.

### Observer

**Selection signals:** one in-process event has zero-to-many listeners that may subscribe dynamically, and the publisher should not name each concrete reaction.

**Mechanics:** a subject maintains observer registrations and invokes a callback when an event occurs. Define subscription lifecycle, ordering, exception isolation, reentrancy, and synchronous versus asynchronous delivery.

```java
interface OrderListener { void onConfirmed(OrderConfirmed event); }
```

**Production examples:** UI event listeners, domain events within one application, change listeners, Spring application events.

**Trade-offs:** decouples the publisher from reactions, but synchronous observers extend the publisher's latency and transaction. Asynchronous delivery adds ordering, retry, duplicate, and eventual-consistency concerns.

**Common mistakes:** no unsubscribe mechanism causing memory leaks; mutating subscriptions during notification unsafely; one observer failure blocking all others; calling distributed brokered messaging “Observer” without discussing delivery semantics.

**Interview question:** Observer versus publish/subscribe?  
**Answer:** Observer usually involves direct registration and notification; pub/sub uses an intermediary topic or broker, often adding temporal and network decoupling.

### Chain of Responsibility

**Selection signals:** a request passes through configurable ordered handlers, where each may handle, reject, enrich, stop, or forward it.

**Mechanics:** each handler shares a request contract and references or is composed into the next handler. The chain owner assembles order explicitly.

```java
interface PaymentCheck {
    CheckResult check(Payment payment, NextCheck next);
}
```

**Production examples:** servlet and Spring Security filters, middleware, validation pipelines, support escalation, approval limits.

**Trade-offs:** handlers stay focused and can be reordered, but control flow becomes indirect and success may be ambiguous if no handler processes the request.

**Common mistakes:** circular chains; unclear stop/continue semantics; mutable requests changed unpredictably; using a chain when every fixed step must always run—a straightforward pipeline may be clearer.

**Interview question:** Can a handler stop the chain?  
**Answer:** yes; handle-or-forward is central, although pipeline variants may deliberately invoke every step.

### Command

**Selection signals:** an action needs first-class identity or state so it can be queued, scheduled, logged, retried, authorized, stored, or undone.

**Mechanics:** a command encapsulates parameters and invokes a receiver; an invoker executes commands without knowing receiver details. Undo requires capturing enough prior state or defining a compensating command.

```java
interface Command<R> { R execute(); }

record GenerateReportCommand(ReportId id, ReportService receiver)
        implements Command<Report> {
    public Report execute() { return receiver.generate(id); }
}
```

**Production examples:** editor undo/redo, job queues, task schedulers, UI actions, transactional outbox work items.

**Trade-offs:** decouples invocation from execution and enables history, but adds command types and serialization/versioning concerns. Retried commands must be idempotent or deduplicated; undo is not automatically possible.

**Common mistakes:** a command class for every trivial local call; business logic in the invoker; storing non-serializable receiver objects in durable jobs; assuming retry is safe; treating event facts as commands—commands request, events report.

**Interview question:** What additional design is needed for queued commands?  
**Answer:** serialization/versioning, idempotency, retry and dead-letter policy, authentication context, observability, and receiver availability.

### Template Method

**Selection signals:** related processes share a stable algorithm skeleton while a few well-defined steps vary.

**Mechanics:** a base class defines a final template operation, implements invariant steps, and exposes abstract or optional hooks to subclasses.

```java
abstract class DocumentProcessor {
    public final Result process(Document document) {
        validate(document);
        Result result = transform(document);
        audit(result);
        return result;
    }
    protected abstract Result transform(Document document);
}
```

**Production examples:** framework lifecycle hooks, batch read-transform-write workflows, parsers, test fixtures. `JdbcTemplate` embodies a related callback-based template approach even though clients often provide composition-style callbacks rather than subclasses.

**Trade-offs:** enforces ordering and removes duplication, but inheritance tightly couples subclasses to base implementation and can produce fragile hooks. Strategy is preferable when algorithms must be selected or composed dynamically.

**Common mistakes:** making every step overridable; allowing subclasses to violate sequence invariants; using it when workflows differ substantially; deep template inheritance.

**Interview question:** Template Method versus Strategy?  
**Answer:** Template Method fixes a skeleton through inheritance and varies hooks; Strategy delegates an interchangeable algorithm through composition.

### Common confusions

| Patterns | Deciding distinction |
| --- | --- |
| Factory vs Builder | choose/create a product vs assemble one complex product |
| Factory vs Abstract Factory | a product selection vs a compatible product family |
| Strategy vs State | externally selected policy vs lifecycle-driven behavior |
| Adapter vs Facade | translate a contract vs simplify a subsystem |
| Decorator vs Proxy | compose responsibility vs control access/lifecycle |
| Observer vs pub/sub | direct registration vs broker-mediated communication |
| Chain vs Decorator | pass a request among handlers vs wrap one component contract |
| Command vs event | request an action vs record that something happened |
| Template Method vs Strategy | inheritance skeleton vs composed algorithm |

### Pattern selection workflow

1. State the requirement, invariant, and likely change.
2. Locate the responsibility that currently absorbs that change.
3. Consider a direct implementation, function, data table, or composition first.
4. Select a pattern only if its collaboration addresses the pressure.
5. Walk a production scenario, including errors, concurrency, and lifecycle.
6. Name the added cost and how the design can be simplified later.

Patterns commonly collaborate: a Factory selects a Strategy; an Abstract Factory supplies provider-specific Adapters; a Facade orchestrates Commands; a Composite may be traversed by a Strategy. Combining patterns is justified only by combining pressures.

## Advantages

- Supplies a precise vocabulary for recurring collaborations.
- Reuses design knowledge and makes extension points recognizable.
- Helps isolate volatile creation, integration, behavior, and workflow concerns.
- Supports focused testing around stable contracts.
- Improves design interview communication when tied to requirements.

## Limitations

- Adds indirection, types, allocation, wiring, and debugging cost.
- Pattern names can conceal an inaccurate domain model.
- Language features such as functions, records, modules, or algebraic data types may provide simpler solutions.
- Distributed versions of local patterns require explicit consistency and delivery semantics.
- Premature use optimizes for imagined change and can make current behavior harder to understand.

## Real-world examples

- A checkout facade receives a payment adapter selected by a factory and a pricing strategy.
- An HTTP client composes retry, metrics, and authentication decorators; retry policy accounts for idempotency.
- A document lifecycle uses State, while observers publish in-process audit reactions.
- A job system stores versioned commands and passes execution through authorization and validation handlers.
- A file tree uses Composite and computes size using recursive uniform operations.

## Interview Questions

1. Given a large conditional, how do you determine whether Strategy, State, Factory, or no pattern is appropriate?
2. Why is Abstract Factory resistant to new product roles but friendly to new families?
3. When should DI-managed scope replace a globally accessed Singleton?
4. How do Adapter, Facade, Decorator, and Proxy differ despite similar delegation?
5. What delivery and failure decisions are missing from a basic Observer implementation?
6. When is a fixed pipeline clearer than Chain of Responsibility?
7. What makes Command retry and undo safe?
8. When does Template Method create a fragile base class?
9. Which patterns favor composition, and which commonly rely on inheritance?
10. **Interview tip:** identify the pressure first, sketch participants and ownership, walk one production flow, and state why a simpler alternative is insufficient.

## References

- [Refactoring.Guru: Design Patterns Catalog](https://refactoring.guru/design-patterns/catalog)
- [Java Design Patterns](https://java-design-patterns.com/patterns/)
- [Oracle Java Tutorials: Enum Types](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html)
- [Oracle Java Tutorials: Aggregate Operations and Streams](https://docs.oracle.com/javase/tutorial/collections/streams/)
- [Martin Fowler: Event Collaboration](https://martinfowler.com/eaaDev/EventCollaboration.html)
- [Microsoft: Cloud Design Patterns](https://learn.microsoft.com/azure/architecture/patterns/)
- [Related: Principles](../Principles/README.md)
- [Related: SOLID](../SOLID/README.md)
- [Related: UML](../UML/README.md)
