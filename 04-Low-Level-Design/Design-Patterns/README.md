# Design Patterns

## Overview

Design patterns are named, reusable arrangements of responsibilities and collaborations. They are not copy-paste implementations or goals by themselves. Start with a requirement and design pressure, use the simplest solution that works, and introduce a pattern when its benefits exceed its extra types and indirection.

This guide consolidates 15 patterns:

- **Creational:** [Factory](#factory), [Abstract Factory](#abstract-factory), [Builder](#builder), [Singleton](#singleton)
- **Structural:** [Adapter](#adapter), [Facade](#facade), [Decorator](#decorator), [Proxy](#proxy), [Composite](#composite)
- **Behavioral:** [Strategy](#strategy), [State](#state), [Observer](#observer), [Chain of Responsibility](#chain-of-responsibility), [Command](#command), [Template Method](#template-method)

### Table of contents

- [Why do we need it?](#why-do-we-need-it)
- [How does it work?](#how-does-it-work)
  - [Selection map](#selection-map)
  - Creational: [Factory](#factory), [Abstract Factory](#abstract-factory), [Builder](#builder), [Singleton](#singleton)
  - Structural: [Adapter](#adapter), [Facade](#facade), [Decorator](#decorator), [Proxy](#proxy), [Composite](#composite)
  - Behavioral: [Strategy](#strategy), [State](#state), [Observer](#observer), [Chain of Responsibility](#chain-of-responsibility), [Command](#command), [Template Method](#template-method)
  - [Common confusions](#common-confusions)
  - [Think mnemonics](#think-mnemonics)
  - [Pattern selection workflow](#pattern-selection-workflow)
- [Advantages](#advantages)
- [Limitations](#limitations)
- [Real-world examples](#real-world-examples)
- [Interview Questions](#interview-questions)
- [Fast revision](#fast-revision)
- [References](#references)
- [Provenance](#provenance)

> **Editorial/professional correction:** Brand-named scenarios inherited from the learning sources are illustrative domains, not claims about those companies' internal architectures. Framework/library claims are qualified as “pattern-like” unless an authoritative reference is supplied. Source examples are also tightened where needed: a Spring singleton is one instance per container/bean definition rather than globally unique; local Observer is not a substitute for durable brokered messaging; an API gateway is not automatically a GoF Proxy; `JdbcTemplate` is callback/template-oriented rather than a literal subclass-only Template Method example; and Factory may mean a simple factory or the GoF Factory Method.

## Why do we need it?

Patterns provide a shared vocabulary and tested starting points for recurring pressures: selecting algorithms, controlling object creation, translating third-party APIs, composing optional behavior, representing trees, or managing lifecycles. Their value is in making trade-offs explicit. Applying a pattern without its pressure creates accidental complexity.

Use the reasoning order `requirements → problem/pressure → pattern`, never “I want to use pattern X.” In interviews, selecting and defending the collaboration usually matters more than reciting its definition. Start by asking “What problem am I solving?”, identify the likely change, and prefer a direct solution when no recurring pressure exists.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** in the illustrative payment-provider domain, business code directly constructs one gateway. Adding another provider forces callers to change, scatters `new ConcreteGateway()` calls, and tightly couples use cases to construction.
- **Structure and implementation:** `Client → Factory → PaymentGateway`; (1) define the common `pay()` product contract, (2) implement provider products, (3) centralize input-to-product selection in `PaymentGatewayFactory.create(type)`, and (4) let usage depend on the returned interface.
- **Why it works / before and after:** callers move from knowing which concrete class to instantiate to knowing only the factory and product contract; creation is centralized. The source's simple factory still contains a conditional, but it prevents that conditional from spreading.
- **Illustrative domains:** payment gateways; email/SMS/push providers; MySQL/PostgreSQL/Oracle drivers; S3/GCS/Azure Blob storage clients. These are domain illustrations, not implementation claims.
- **Fast revision:** object-creation complexity + concrete construction everywhere → centralize construction; many implementations and input-dependent creation → think Factory.

**Trade-offs:** centralizes construction and shields clients, but can become a growing switch or service locator. Registration maps or DI configuration may be better for plugin ecosystems.

**Common mistakes:** creating a factory for one trivial implementation; putting business behavior in the factory; claiming that merely moving a conditional satisfies OCP; confusing Factory with Builder.

**Interview question:** When would you use Factory rather than direct construction?  
**Answer:** when selection or construction is volatile enough that callers should depend only on the product contract.

**Additional source interview checks:** It solves creation complexity; returning an interface is the common approach. Factory chooses/creates an object, whereas Builder assembles a complex object step by step.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** provider-specific gateway, refund, and webhook components form a family. Creating each with repeated provider conditionals can accidentally mix, for example, one provider's gateway with another's refund implementation.
- **Structure and implementation:** `PaymentFactory` declares one creation method per product role; each concrete provider factory returns its own compatible implementations. Usage chooses one concrete factory and obtains all products through the abstract factory interface.
- **Why it works / before and after:** creation changes from several independent conditionals to one family-producing object, making family consistency explicit and preventing invalid combinations.
- **Illustrative domains:** payment-provider SDK families (`Gateway`, `Refund`, `Webhook`), database families (`Connection`, `QueryExecutor`, `TransactionManager`), and dark/light UI widget families (`Button`, `TextBox`, `Dropdown`).
- **Fast revision:** related products must stay together → create the whole family through one factory; “family of objects” is the strongest clue.

**Production examples:** provider SDK suites, cross-platform UI widgets, database-specific connection/transaction/query components.

**Trade-offs:** enforces family consistency and makes switching families easy, but adding a new product role changes every factory. It is strongest when families vary more often than product roles.

**Common mistakes:** using it for one product; grouping unrelated objects; leaking concrete family types to clients; calling a single-product factory “Abstract Factory.”

**Interview question:** Factory versus Abstract Factory?  
**Answer:** a factory selects or constructs a product; Abstract Factory coordinates a family of related product roles.

**Additional source interview checks:** Use it when several related products must be created consistently. Its primary problem is family creation, not merely hiding one constructor.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative product-search request may have keyword, category, brand, minimum/maximum price, rating, sort order, and delivery speed, many of them optional. A long positional constructor is hard to read, permits parameter mix-ups, and tends toward telescoping constructor overloads.
- **Structure and implementation:** `Client → SearchRequestBuilder → SearchRequest`; (1) hold construction fields in the builder, (2) expose named fluent methods that return the builder, (3) validate required fields and cross-field invariants in `build()`, and (4) return the finished preferably immutable request. The source's mutable internal-product sketch conveys fluent assembly, but copying values into a new immutable product is the professional correction.
- **Why it works / before and after:** positional arguments become named operations such as `.keyword(...)` and `.category(...)`; one huge constructor becomes readable, flexible, step-by-step construction.
- **Illustrative domains:** product search, Spring-style response builders, Lombok-generated builders, HTTP requests, configuration, and test fixtures. API shape alone is suggestive, not proof of the GoF pattern.
- **Fast revision:** too many constructor arguments or optional fields + constructor explosion → named step-by-step construction; many optional fields → think Builder.

**Production examples:** HTTP requests, immutable configuration, search criteria, protocol messages, test fixtures.

**Trade-offs:** improves readability and centralizes validation but adds boilerplate and can hide required fields unless the API or type system enforces them.

**Common mistakes:** builders for tiny values; allowing `build()` to create invalid objects; reusing a mutable builder unsafely; confusing construction steps with choosing a product family.

**Interview question:** Why is Builder common in Java?  
**Answer:** it compensates for the lack of named/default parameters and supports readable construction of immutable values.

**Additional source interview checks:** It solves constructor explosion; avoid it for simple objects. Builder assembles one complex object, while Factory selects or creates a product.

### Singleton

**Selection signals:** the domain or runtime requires one instance within an explicitly defined scope and ownership of that lifecycle must be controlled.

**Mechanics:** restrict construction and provide one instance. In Java, an enum singleton gives safe initialization and serialization for process-wide instances:

```java
enum ConfigurationRegistry {
    INSTANCE;
}
```

Often the better production implementation is a single application-scoped object owned by a DI container and injected into consumers—without global lookup.

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative configuration manager represents one coherent configuration/properties/environment view. Independently constructing `config1`, `config2`, and so on can produce inconsistent state.
- **Structure and implementation:** consumers share one scoped instance. The source demonstrates (1) a private constructor, (2) a static eagerly initialized instance, and (3) `getInstance()`. It also shows why an unsynchronized lazy `if (instance == null)` is unsafe: two threads can observe null and construct two instances.
- **Thread-safe options:** the source's double-checked locking requires a `volatile` instance, an outer null check, synchronization on the class, and an inner null check. Prefer enum, initialization-on-demand holder, or container ownership unless lazy double-checked locking is specifically required.
- **Why it works / before and after:** separate `ServiceA → Config1`, `ServiceB → Config2`, and `ServiceC → Config3` references become consumers of one scoped configuration source.
- **Illustrative domains:** configuration snapshots, feature flags, environment settings, loggers, cache managers, and connection pools. Logger/cache/pool cardinality is an operational scope choice; Spring's default singleton scope is per bean definition per container.
- **Fast revision:** multiple inconsistent instances → controlled construction and one scoped instance; “exactly one instance in a defined scope” → consider Singleton, then check whether DI ownership is cleaner.

**Production examples:** process-level registries or immutable configuration snapshots. Loggers, pools, and caches may be singletons within a container scope, but that is a deployment choice rather than an inherent property.

**Trade-offs:** controlled lifecycle can prevent duplicate resources, but global access hides dependencies, complicates isolation and tests, and does not mean one instance across processes, class loaders, tenants, or a cluster.

**Common mistakes:** treating services, users, orders, or mutable domain state as singletons; unsafe lazy initialization; using double-checked locking without `volatile`; a global god object; claiming a Spring singleton is globally unique.

**Interview question:** How would you make a thread-safe Java singleton?  
**Answer:** prefer enum or initialization-on-demand holder; use `volatile` with correct double-checked locking only when lazy construction specifically requires it.

**Additional source interview checks:** Thread safety matters because concurrent lazy initialization can create multiple objects. Singleton is not always good; it is frequently overused and should never model independent users, orders, or payments.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative payment aggregator wants one gateway contract, while providers expose different method names, request formats, and response formats. Calling `makePayment()`, `processPayment()`, and other provider APIs directly from business code makes integrations leak throughout the application.
- **Structure and implementation:** `PaymentService → PaymentGateway → StripeAdapter/RazorpayAdapter → provider API`; (1) define the client's target contract, (2) retain the existing adaptee unchanged, (3) implement the target in an adapter that owns the adaptee, and (4) translate and delegate from target operations to adaptee operations.
- **Why it works / before and after:** business code moves from knowing every provider API to knowing one interface; provider-specific request, response, method, and error conversion stays in adapters.
- **Illustrative domains:** payment providers, maps/SMS/email APIs, old-to-new system bridges, and old-to-new repository migration. Brand labels are illustrative only.
- **Fast revision:** incompatible API contracts that cannot be changed → translate behind a target interface; third-party or legacy integration + interface mismatch → think Adapter.

**Production examples:** payment gateways, cloud providers, legacy repositories, anti-corruption layers, external messaging APIs.

**Trade-offs:** isolates vendor change and keeps business language clean, but translation can be lossy and adapters require contract and integration tests.

**Common mistakes:** leaking vendor DTOs through the target interface; changing third-party code; swallowing error semantics; placing conversion logic in business services; confusing translation with Facade simplification.

**Interview question:** Adapter versus Facade?  
**Answer:** Adapter makes an incompatible interface conform to a target contract; Facade provides a simpler entry point to a subsystem.

**Additional source interview checks:** Adapter solves interface mismatch and permits existing code to remain unchanged. Use it when systems must collaborate but expose different contracts.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative checkout requires inventory reservation, price calculation, payment, shipment creation, and notification. If a controller coordinates every subsystem, it knows too much and every workflow change can affect clients.
- **Structure and implementation:** `Client → CheckoutFacade → Inventory/Pricing/Payment/Shipping/Notification`; (1) keep subsystem services independently usable, (2) inject them into a focused facade, (3) expose a use-case operation such as `checkout()`, and (4) coordinate the sequence and return a cohesive result.
- **Why it works / before and after:** workflow ownership moves from each client into one stable entry point; clients stop talking to every subsystem and subsystem complexity is localized.
- **Illustrative domains:** checkout, `bookTrip()` across flight/hotel/payment, `transferMoney()` across balance/fraud/transfer, and simplified JDBC access. `JdbcTemplate` is a framework utility with facade- and callback-template-like qualities, not evidence that every wrapper is a GoF Facade.
- **Fast revision:** one workflow + many subsystem calls + client knows too much → provide a focused single entry point.

**Production examples:** checkout, travel booking, SDK entry points, application-service APIs, `JdbcTemplate`-style wrappers over lower-level APIs.

**Trade-offs:** reduces client coupling and centralizes workflow, but can become a bottleneck or god facade. Transaction, compensation, and partial-failure semantics must be explicit.

**Common mistakes:** moving all domain logic into the facade; exposing a grab bag of unrelated methods; constructing dependencies internally; treating a network API gateway as automatically equivalent to the GoF pattern.

**Interview question:** Can a Facade coexist with direct subsystem access?  
**Answer:** yes; it offers a convenient boundary and does not necessarily prohibit specialized access.

**Additional source interview checks:** Facade solves complex subsystem interaction and commonly calls several services—that coordination is its point. Adapter translates; Facade simplifies.

### Decorator

**Selection signals:** optional responsibilities must be combined at runtime around the same contract without a subclass for every combination.

**Mechanics:** a decorator implements the component contract, contains another component, and adds behavior before or after delegation.

```java
PaymentProcessor processor =
    new MetricsProcessor(
        new RetryProcessor(
            new BasicPaymentProcessor()));
```

**Learning-source walkthrough:**

- **Motivation and bad design:** payment flows may independently need fraud checking, retry, audit logging, and metrics. Inheritance for every combination produces `PaymentWithRetry`, `PaymentWithAudit`, `PaymentWithRetryAndAudit`, and an exponential subclass explosion.
- **Structure and implementation:** decorators form a wrapper chain around `BasicPaymentProcessor`; (1) define the component contract, (2) implement the concrete component, (3) create a base decorator that implements the same contract and stores a component, (4) add focused concrete decorators that act before/after delegation, and (5) compose the required order at runtime.
- **Why it works / before and after:** one class per combination becomes reusable single-responsibility wrappers that can be freely combined. For example, `Metrics(Audit(Retry(Basic)))` yields all behaviors; order is semantically significant.
- **Illustrative domains:** payment checks, HTTP logging/retry/cache wrappers, `FileInputStream → BufferedInputStream → DataInputStream`, and notification retry/logging/metrics.
- **Fast revision:** optional combinable features + too many subclasses → wrap a shared contract; logging/retry/metrics/audit combinations → consider Decorator.

**Production examples:** Java I/O streams, HTTP client middleware, metrics/logging/retry wrappers, notification enrichment.

**Trade-offs:** supports flexible composition and focused wrappers, but order can change behavior, object graphs become hard to inspect, and identity/equality can be surprising.

**Common mistakes:** decorators that do not preserve the component contract; using retry around non-idempotent actions without safeguards; putting unrelated business logic in cross-cutting wrappers; confusing Decorator with Proxy.

**Interview question:** Decorator versus inheritance?  
**Answer:** Decorator composes responsibilities per object and at runtime; inheritance fixes them in the class hierarchy.

**Additional source interview checks:** Multiple decorators can be chained; this runtime composition is the primary advantage. Use meaningful wrappers—one that contributes no behavior is needless indirection.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative premium-video request may require subscription validation, authorization, logging, monitoring, or caching. Direct access to the real service bypasses those policies.
- **Structure and implementation:** `Client → VideoProxy → VideoService`, with proxy and real service implementing `VideoPlatform`; (1) define the subject interface, (2) implement the real subject, (3) implement the same interface in a proxy, (4) perform access/lifecycle policy, then (5) delegate when permitted.
- **Why it works / before and after:** the client still targets the subject contract, but a stand-in can validate, log, cache, authorize, rate-limit, or lazily initialize before forwarding. The source's cache proxy checks a map, loads on miss, stores, and returns without modifying the underlying service.
- **Illustrative domains:** premium-content authorization, permission-checked database access, cache fronts, lazy loading, and gateways that stand in front of services. A network API gateway may behave proxy-like but is not automatically the GoF pattern.
- **Fast revision:** something must happen before the real object is reached → place a same-contract stand-in in front; authorization/caching/rate limiting/lazy loading → consider Proxy.

Common variants include virtual, protection, remote, and caching proxies.

**Production examples:** ORM lazy-loading proxies, RPC stubs, authorization wrappers, cache fronts, framework-generated transactional proxies.

**Trade-offs:** centralizes access policy and can defer expensive work, but adds latency and hidden behavior. Caching introduces invalidation and consistency concerns; remote proxies cannot make network calls behave like local calls.

**Common mistakes:** business logic in the proxy; transparent retries that duplicate side effects; equality or serialization surprises from generated proxies; stacking opaque proxy layers.

**Interview question:** Proxy versus Decorator?  
**Answer:** their structure is similar; Proxy primarily controls access or lifecycle, while Decorator intentionally composes additional responsibilities.

**Additional source interview checks:** Clients often need not know that the proxy is present. Its central problem is controlled access; avoid unnecessary proxy stacks because hidden layers make debugging difficult.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** a file system contains both files and recursively nested folders. Separate `List<File>` and `List<Folder>` APIs force clients to branch with `instanceof` and handle leaves and groups differently.
- **Structure and implementation:** `FileSystemNode` is implemented by leaf `File` and composite `Folder`; a folder stores `List<FileSystemNode>`. (1) define the common operation, (2) implement it directly in a leaf, (3) let the composite own child components, and (4) recursively delegate the operation to every child.
- **Why it works / before and after:** handling `File` and `Folder` separately becomes handling every node through one abstraction. A composite can contain another composite, which is why recursion naturally expresses traversal.
- **Illustrative domains:** file explorers, CEO/manager/employee organization trees, nested product categories, and page/panel/button UI trees.
- **Fast revision:** a recursive hierarchy whose individual and group objects need uniform treatment → common component contract; tree structure → think Composite.

**Production examples:** file trees, UI component trees, organization hierarchies, product categories, expression trees.

**Trade-offs:** simplifies recursive client code, but a common interface may expose operations meaningless for leaves. Very deep or cyclic graphs require iterative traversal or cycle protection.

**Common mistakes:** using Composite for a flat collection; exposing mutable child collections; allowing cycles in a structure assumed to be a tree; forcing child-management methods onto leaves without a deliberate safe/transparent design choice.

**Interview question:** Why do leaf and composite share an interface?  
**Answer:** so recursive clients can treat individual and grouped objects uniformly.

**Additional source interview checks:** A composite may contain composites; that recursive part–whole relation is the core idea. No hierarchy means no Composite.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative ride-pricing system has Go, XL, Premier, and future Bike/Auto/Black fare rules. A string-based `if` chain grows whenever a rule is added, mixes algorithms in one class, and raises regression risk.
- **Structure and implementation:** `Client → FareCalculator(context) → PricingStrategy → concrete pricing`; (1) define `calculateFare`, (2) implement one strategy per algorithm, (3) inject a strategy into the context, and (4) delegate calculation. Client/configuration chooses the strategy and may replace it at runtime.
- **Why it works / before and after:** extending behavior changes from editing a growing conditional to adding a strategy and wiring its selection. Shared stable steps should not be duplicated across strategies; use a helper or a template when appropriate.
- **Illustrative domains:** ride pricing, credit-card/UPI/wallet fees, festival/coupon/membership discounts, jurisdictional taxes, matching, and compression.
- **Fast revision:** multiple algorithms + client/runtime choice + a large behavior conditional → separate each algorithm; externally selected behavior → think Strategy.

**Production examples:** ride pricing, discounts, payment routing, tax calculation, matching, compression.

**Trade-offs:** isolates and tests algorithms independently, but increases objects and requires a selection mechanism. Lambdas or functions may be sufficient when strategies have little state or lifecycle.

**Common mistakes:** one strategy with no expected variation; selecting via type checks inside every strategy; copying shared algorithm steps; assuming a strategy removes all conditionals.

**Interview question:** Strategy versus State?  
**Answer:** a client/configuration chooses Strategy to accomplish a task; State represents a context's current lifecycle condition and usually drives transitions internally.

**Additional source interview checks:** Strategy solves multiple algorithms for one task, can be changed at runtime, and is often paired with a Factory that performs selection. Do not introduce it before real variation exists.

### State

**Selection signals:** an object's allowed operations and behavior vary substantially by lifecycle state, and status checks are distributed.

**Mechanics:** a context delegates state-specific behavior to a state object; transitions replace the current state explicitly. Keep transition rules visible and preserve aggregate invariants.

```java
interface OrderState {
    void cancel(Order context);
    void dispatch(Order context);
}
```

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative delivery order moves through Created, Confirmed, Preparing, Out for Delivery, and Delivered; cancellation and other actions differ by state. Status strings and repeated conditionals spread transition logic and invalid-operation checks across the context.
- **Structure and implementation:** `Order(context) → OrderState → concrete states`; (1) define state-dependent operations, (2) implement behavior such as allowing cancellation in `CreatedState` and rejecting it in `DeliveredState`, (3) store the current state in the context, and (4) delegate operations and transition explicitly.
- **Transition meaning:** the simple progression `Created → Confirmed → Preparing → OutForDelivery → Delivered` is a lifecycle graph, not merely five labels. A state may request the next transition, but the aggregate/context should preserve invariants and one consistent transition policy.
- **Why it works / before and after:** one class containing every state branch becomes focused state-specific classes; behavior changes automatically when the context's state reference changes.
- **Illustrative domains:** delivery orders, ATM sessions (`Idle → CardInserted → Authenticated → Processing → Completed`), vending machines, and document workflows (`Draft → Review → Approved → Published`).
- **Fast revision:** behavior changes with current status + too many status checks → move behavior into explicit states; same object, different lifecycle behavior → think State.

**Production examples:** orders, bookings, vending machines, documents, ATM sessions.

**Trade-offs:** localizes complex state behavior and removes scattered conditionals, but creates classes and may obscure the full transition graph. Persisting polymorphic state objects can complicate mapping; a status plus transition table may be better for simple workflows.

**Common mistakes:** a class per status when behavior does not differ; state classes plus the original conditionals; transitions available from everywhere; no handling of invalid transitions or concurrent updates.

**Interview question:** Who changes the context's current state?  
**Answer:** either the state or context can coordinate it; choose one consistent policy and make allowed transitions explicit.

**Additional source interview checks:** State solves lifecycle-dependent behavior. State objects can initiate transitions, but creating classes for statuses with no behavioral difference or transitions is unnecessary.

### Observer

**Selection signals:** one in-process event has zero-to-many listeners that may subscribe dynamically, and the publisher should not name each concrete reaction.

**Mechanics:** a subject maintains observer registrations and invokes a callback when an event occurs. Define subscription lifecycle, ordering, exception isolation, reentrancy, and synchronous versus asynchronous delivery.

```java
interface OrderListener { void onConfirmed(OrderConfirmed event); }
```

**Learning-source walkthrough:**

- **Motivation and bad design:** when one publisher creates a post or an order event, several followers/reactions may need notification. Hard-coding calls to follower A/B/C means every subscriber change edits the publisher and tightly couples it to concrete recipients.
- **Structure and implementation:** `Subject/Publisher → List<Observer> → concrete observers`; (1) define `update(event)`, (2) implement subscriber reactions, (3) maintain subscribe and unsubscribe operations, (4) publish an event, and (5) iterate over a safe observer snapshot with a defined failure policy.
- **Why it works / before and after:** the publisher moves from knowing every recipient to knowing only the observer contract and registrations; new subscribers require no publisher code change and can join or leave dynamically.
- **Illustrative domains:** creator/follower and video/subscriber notifications, stock-price listeners, order-shipped customer reactions, UI listeners, and Spring application events. These are illustrative; high-scale or distributed notification commonly needs a broker such as Kafka or RabbitMQ and explicit delivery semantics.
- **Fast revision:** manually notify everyone → maintain observer registrations; one event + many listeners → think Observer.

**Production examples:** UI event listeners, domain events within one application, change listeners, Spring application events.

**Trade-offs:** decouples the publisher from reactions, but synchronous observers extend the publisher's latency and transaction. Asynchronous delivery adds ordering, retry, duplicate, and eventual-consistency concerns.

**Common mistakes:** no unsubscribe mechanism causing memory leaks; mutating subscriptions during notification unsafely; one observer failure blocking all others; calling distributed brokered messaging “Observer” without discussing delivery semantics.

**Interview question:** Observer versus publish/subscribe?  
**Answer:** Observer usually involves direct registration and notification; pub/sub uses an intermediary topic or broker, often adding temporal and network decoupling.

**Additional source interview checks:** Observer solves one event triggering multiple reactions, supports runtime subscription, and works most naturally inside one application boundary. Remember unsubscribe to avoid retained observers.

### Chain of Responsibility

**Selection signals:** a request passes through configurable ordered handlers, where each may handle, reject, enrich, stop, or forward it.

**Mechanics:** each handler shares a request contract and references or is composed into the next handler. The chain owner assembles order explicitly.

```java
interface PaymentCheck {
    CheckResult check(Payment payment, NextCheck next);
}
```

**Learning-source walkthrough:**

- **Motivation and bad design:** an illustrative payment approval may involve fraud, balance, approval, and audit handlers. One service method that hard-codes all steps tightly couples them and makes reordering or optional stopping difficult.
- **Structure and implementation:** `Request → FraudHandler → BalanceHandler → ApprovalHandler → AuditHandler`; (1) define a handler with a next reference or continuation, (2) implement focused handlers, (3) explicitly assemble order, and (4) have each handler process, reject/stop, or forward.
- **Why it works / before and after:** one giant method becomes independently testable handlers that can be inserted and reordered. “Flexible pipeline” is accurate only when stop/forward or configurable composition is required; a fixed always-run sequence may remain a plain method.
- **Illustrative domains:** Spring Security and servlet filter chains, payment validation, middleware, and manager/director/VP approval escalation.
- **Fast revision:** request passes through ordered steps and each may handle or forward → independent linked/composed handlers; “handle or forward” is the strongest clue.

**Production examples:** servlet and Spring Security filters, middleware, validation pipelines, support escalation, approval limits.

**Trade-offs:** handlers stay focused and can be reordered, but control flow becomes indirect and success may be ambiguous if no handler processes the request.

**Common mistakes:** circular chains; unclear stop/continue semantics; mutable requests changed unpredictably; using a chain when every fixed step must always run—a straightforward pipeline may be clearer.

**Interview question:** Can a handler stop the chain?  
**Answer:** yes; handle-or-forward is central, although pipeline variants may deliberately invoke every step.

**Additional source interview checks:** Chain solves routing through multiple handlers and is common in middleware and approval workflows. Order matters; circular links such as `A → B → C → A` create nontermination.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** a food-delivery order or UI action may need queuing, retry, logging, delayed execution, scheduling, or history. A direct button-to-service method call has no first-class action to store or manage.
- **Structure and implementation:** `Client → Invoker(Button) → Command → Receiver(OrderService)`; (1) define `execute`, (2) keep domain work on the receiver, (3) create one focused concrete command carrying receiver and parameters, (4) give the invoker a command, and (5) trigger it without receiver knowledge.
- **Why it works / before and after:** immediate direct invocation becomes a request object that can be stored, queued, retried, logged, scheduled, authorized, or placed in history. Durable commands should store serializable identifiers/data rather than live receiver objects.
- **Illustrative domains:** copy/paste/delete undo history, email/SMS notification jobs, midnight report generation, and order-processing messages. A message consumed from Kafka can carry a command, but transport alone does not establish the pattern.
- **Fast revision:** need to store a request or execute later → wrap the request as an object; queue/retry/schedule/undo → think Command.

**Production examples:** editor undo/redo, job queues, task schedulers, UI actions, transactional outbox work items.

**Trade-offs:** decouples invocation from execution and enables history, but adds command types and serialization/versioning concerns. Retried commands must be idempotent or deduplicated; undo is not automatically possible.

**Common mistakes:** a command class for every trivial local call; business logic in the invoker; storing non-serializable receiver objects in durable jobs; assuming retry is safe; treating event facts as commands—commands request, events report.

**Interview question:** What additional design is needed for queued commands?  
**Answer:** serialization/versioning, idempotency, retry and dead-letter policy, authentication context, observability, and receiver availability.

**Additional source interview checks:** Command decouples sender and receiver and treats requests as first-class objects. Keep invokers free of business logic and avoid a command class for a trivial call that needs none of these capabilities.

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

**Learning-source walkthrough:**

- **Motivation and bad design:** PDF, Word, and spreadsheet processing may all read, transform/process, and save. Copying the whole workflow into each processor duplicates invariant steps and permits sequence drift.
- **Structure and implementation:** `DocumentProcessor → PdfProcessor/WordProcessor`; (1) put the algorithm skeleton in a `final process()`, (2) implement invariant `read` and `save` steps in the base class, (3) expose only the varying `transform` step as abstract/protected, and (4) let subclasses supply that step.
- **Why it works / before and after:** duplicated `Read → Process → Save` workflows become one controlled `Read → Transform → Save` template; only transformation varies, so ordering stays consistent.
- **Illustrative domains:** document processing, batch `Read → Transform → Write`, ETL `Extract → Transform → Load`, framework lifecycle hooks, and test setup/test/teardown. `JdbcTemplate` and modern frameworks commonly use callbacks or annotations to achieve related inversion of control, not necessarily literal inheritance.
- **Fast revision:** most workflow code is identical but a few steps differ → keep the skeleton once and expose hooks; fixed flow + custom steps → think Template Method.

**Production examples:** framework lifecycle hooks, batch read-transform-write workflows, parsers, test fixtures. `JdbcTemplate` embodies a related callback-based template approach even though clients often provide composition-style callbacks rather than subclasses.

**Trade-offs:** enforces ordering and removes duplication, but inheritance tightly couples subclasses to base implementation and can produce fragile hooks. Strategy is preferable when algorithms must be selected or composed dynamically.

**Common mistakes:** making every step overridable; allowing subclasses to violate sequence invariants; using it when workflows differ substantially; deep template inheritance.

**Interview question:** Template Method versus Strategy?  
**Answer:** Template Method fixes a skeleton through inheritance and varies hooks; Strategy delegates an interchangeable algorithm through composition.

**Additional source interview checks:** Template Method solves workflow duplication and enforces order. Make only genuinely variable steps overridable; if workflows differ substantially or must be selected at runtime, Strategy is usually the better fit.

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

### Think mnemonics

Compact recognition labels from the source pattern-identification framework. Use them only after stating the requirement and pressure; they are clues, not automatic selections.

| Pattern | Think |
| --- | --- |
| Strategy | Choose One Behavior |
| State | Same Object / Different Behavior |
| Observer | Publish Event |
| Factory | Which Object To Create? |
| Abstract Factory | Related Objects Together |
| Builder | Complex Object Construction |
| Decorator | Add Features Dynamically |
| Adapter | Convert Interface |
| Facade | Single Entry Point |
| Proxy | Control Access |
| Composite | Treat Group And Individual The Same |
| Chain of Responsibility | Handle Or Forward |
| Command | Request As Object |
| Template Method | Fixed Flow / Custom Steps |

### Pattern selection workflow

1. State the requirement, invariant, and likely change.
2. Locate the responsibility that currently absorbs that change.
3. Consider a direct implementation, function, data table, or composition first.
4. Select a pattern only if its collaboration addresses the pressure.
5. Walk a production scenario, including errors, concurrency, and lifecycle.
6. Name the added cost and how the design can be simplified later.

Patterns commonly collaborate: a Factory selects a Strategy; an Abstract Factory supplies provider-specific Adapters; a Facade orchestrates Commands; a Composite may be traversed by a Strategy. Combining patterns is justified only by combining pressures.

### Critical learning

- Patterns solve design pressures; requirements come before pattern names.
- Knowing definitions is not enough: interviews test whether the chosen collaboration fits the change and ownership model.
- Strategy and Factory commonly collaborate, and State can look structurally similar to Strategy while solving a different lifecycle problem.
- The source emphasizes Strategy, Factory, Observer, and State as frequent LLD interview tools. **Editorial/professional correction:** frequency is context-dependent; no small set of patterns solves most designs automatically.
- Pattern selection may be easier to state than production implementation, where concurrency, failures, persistence, delivery, and observability become decisive.
- The source claims that most LLD interviews fail because of wrong pattern selection. **Professional correction:** that is an unmeasured source heuristic, not a verified statistic; treat it as a reminder to justify pattern choice from requirements. The defensible guidance is that wrong or forced pattern selection harms designs—adding indirection without matching pressure, mislabeling a simpler table or function as Strategy/Factory/Observer, or choosing a pattern before modelling usually produces brittle types and weak interview reasoning.
- Forcing one or several patterns into a simple problem is a design mistake. The simplest adequate design is preferable.

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

## Fast revision

```text
Requirements → problem/pressure → simplest adequate design → pattern (only if justified)
```

- **Strategy:** multiple algorithms; the client/configuration chooses one behavior.
- **State:** one object's behavior changes with lifecycle state; status checks are spreading.
- **Observer:** one in-process event has many dynamic listeners.
- **Factory:** input/configuration determines which product to create.
- **Abstract Factory:** create a consistent family of related products.
- **Builder:** many optional/named values or construction stages make constructors unclear.
- **Singleton:** exactly one instance is required within a defined scope; consider DI ownership first.
- **Decorator:** combine optional responsibilities by wrapping a shared contract.
- **Adapter:** translate an incompatible third-party or legacy contract.
- **Facade:** give clients one cohesive entry point to a multi-subsystem workflow.
- **Proxy:** control access or lifecycle before delegating to the real subject.
- **Composite:** treat leaves and recursively nested groups uniformly.
- **Chain of Responsibility:** an ordered handler may handle, stop, or forward a request.
- **Command:** represent an action as an object for queueing, scheduling, retry, history, or undo.
- **Template Method:** keep a workflow skeleton fixed while subclasses customize selected steps.

```text
Large conditional ≠ automatic pattern
Third-party ≠ automatic Adapter
Notification ≠ automatic Observer
Workflow ≠ automatic Chain
Problem first; pattern later
```

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

## Provenance

This single-file guide semantically consolidates the following learning sources. Repeated overview definitions, clue tables, and “problem first” reminders were merged once; unique teaching content was retained in each pattern's selection signals, mechanics, learning-source walkthrough, examples, trade-offs, mistakes, interview checks, and the global revision sections.

| Source | Restored coverage |
| --- | --- |
| `02-patterns/factory.md` | Motivation, direct-construction bad design, scattering problem, structure, four implementation stages, why centralization works, provider/notification/driver/storage domains, recognition clues, benefits, mistakes, critical learning, interview checks, fast revision |
| `02-patterns/abstract-factory.md` | Related-family motivation, mixed-family failure, family diagram meaning, product/factory/concrete-factory/usage stages, consistency explanation, before/after, provider/database/UI families, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/builder.md` | Optional search-field motivation, telescoping constructor problem, builder flow, model/builder/build/usage stages, readability explanation, positional-to-fluent comparison, search/framework/Lombok domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/singleton.md` | Configuration consistency motivation, duplicate-instance bad design, sharing structure, eager and unsafe-lazy examples, double-checked locking requirements, why sharing works, before/after, configuration/logger/cache/pool/container domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/adapter.md` | Interface-mismatch motivation, provider-specific API leakage, adapter diagram, target/adaptee/adapter/usage stages, isolation explanation, before/after, payment/API/legacy/repository domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/facade.md` | Checkout-workflow motivation, over-coordinating client, facade/subsystem structure, subsystem/facade/usage stages, ownership explanation, before/after, checkout/travel/banking/framework domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/decorator.md` | Optional-feature motivation, subclass explosion, wrapper-stack meaning, component/concrete/base/concrete-decorator/usage stages, multiple-decoration order, reuse explanation, before/after, payment/HTTP/Java I/O/notification domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/proxy.md` | Controlled-access motivation, direct-access failure, stand-in structure, subject/real subject/proxy/usage stages, pre-delegation explanation, before/after, content/gateway/database/cache domains, cache-on-miss example, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/composite.md` | Leaf/group motivation, type-checking bad design, recursive tree meaning, component/leaf/composite/usage stages, uniformity explanation, before/after, filesystem/organization/category/UI trees, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/strategy.md` | Algorithm-variation motivation, fare conditional, extension problem, strategy diagram, interface/concrete/context/usage stages, add-rather-than-edit explanation, pricing/payment/discount/tax domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/state.md` | Lifecycle motivation, status-conditional failure, state structure and transition graph, interface/concrete/context stages, per-state explanation, order/ATM/vending/document domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/observer.md` | One-to-many motivation, hard-coded recipients, registration structure, observer/concrete/publisher/usage stages, publisher-decoupling explanation, before/after, social/market/commerce/framework domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/chain-of-responsibility.md` | Multi-handler motivation, giant-method problem, chain diagram, handler/concrete/assembly stages, independent-handler explanation, before/after, security/filter/payment/approval domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/command.md` | Request-as-object motivation, direct invocation limitation, client/command/receiver structure, interface/receiver/concrete/invoker/usage stages, storage explanation, before/after, undo/jobs/scheduling/messages domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `02-patterns/template-method.md` | Shared-workflow motivation, duplicated processors, inheritance structure, base/concrete/usage stages, invariant-flow explanation, before/after, document/framework/test/batch/ETL domains, clues, benefits, mistakes, learnings, Q&A, revision |
| `00-revision/design-patterns.md` | Requirement-first model, cheat-sheet signals, common confusions, detection hints, pattern collaboration, anti-forcing guidance, and interview-focused critical learning; exact overlap with detailed sources was merged |
| `03-problem-framework/pattern-identification.md` | Selection process, clues/examples/“think” mnemonic for all represented patterns, decision mapping, three global mistakes, critical learning, and fast revision; exact overlap with overview and pattern files was merged; compact **Think mnemonics** table restored under [Think mnemonics](#think-mnemonics) |

### Editorial/professional corrections retained

- Brand examples are labeled illustrative domains; no undocumented internal architecture is attributed to Uber, Swiggy, Instagram, Amazon, PhonePe, YouTube, or other companies.
- Simple Factory is distinguished from the GoF Factory Method, and Abstract Factory is reserved for related product families.
- Singleton uniqueness is scoped; enum/holder/container ownership is preferred over fragile global lazy initialization, and `volatile` is required for double-checked locking.
- Builder's `build()` validates invariants and should preferably return a fresh immutable product rather than expose mutable internal construction state.
- Observer's subscription lifecycle, synchronous/asynchronous behavior, exception isolation, and distributed-delivery limits remain explicit.
- Decorator order, retry idempotency, Proxy cache consistency/remote-call semantics, Composite cycle/depth hazards, State persistence/concurrency, and Command serialization/idempotency remain explicit.
- A fixed always-run pipeline is not automatically Chain of Responsibility; an API gateway is not automatically a GoF Proxy.
- `JdbcTemplate`, Spring events, JUnit hooks, Spring Security filters, and similar framework examples are described as pattern-like or related unless the cited public contract establishes the exact pattern.
