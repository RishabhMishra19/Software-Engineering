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

### Source-derived OOP tutorial

As applications grow, functions become hard to manage, data gets scattered, and code becomes difficult to extend. OOP organizes software around:

```text
Objects = Data + Behavior
```

For example, an Uber-like domain is described with `Driver`, `Passenger`, `Ride`, `Vehicle`, and `Payment` objects.

#### Class versus object

A class is a blueprint; an object is an actual runtime instance.

```java
class Driver {
    String name;
    String vehicleNumber;
}

Driver driver = new Driver();
```

```text
Class = Blueprint
Object = Real Thing
```

#### Abstraction walkthrough

When a rider books a ride, they do not need to know how drivers are matched, prices are calculated, or payments are processed. Expose the useful action and hide those details:

```java
Ride ride = rideService.bookRide();
```

The user sees “Book Ride,” not matching, pricing, or payment internals. The benefits are reduced complexity and easier usage.

#### Encapsulation walkthrough

Public state permits invalid changes:

```java
account.balance = -10000;
```

Put state transitions behind behavior:

```java
class Account {
    private double balance;

    public void withdraw(double amount) {
        // validate and update
    }
}
```

Banking systems do not expose balances for arbitrary mutation. Encapsulation protects data and creates a validation boundary.

> **Professional correction:** Encapsulation is not merely `private` data. Operations must actually enforce invariants, as the `Money`-based account example above demonstrates.

#### Inheritance and polymorphism walkthrough

`Car` and `Bike` may share a stable `Vehicle` contract such as `numberPlate`, `owner`, and `start()`:

```text
Vehicle
 ├── Car
 └── Bike
```

```java
class Vehicle {
    String numberPlate;
}
```

The tutorial benefit is shared code and common behavior. The common mistake is inheritance solely for reuse.

Payment methods illustrate polymorphism: the same `pay()` action can dispatch to UPI, card, or wallet behavior at runtime.

```java
PaymentMethod payment;
payment.pay();
```

Razorpay, Stripe, and PayPal are familiar provider examples: one client-facing capability, multiple implementations.

> **Professional correction:** Code reuse alone does not justify inheritance. An **IS-A** subtype must honor the parent contract; use **HAS-A** composition when behavior varies independently.

#### Composition and aggregation walkthrough

One object can be assembled from collaborators:

```java
class Ride {
    Driver driver;
    Vehicle vehicle;
}
```

The source uses an Amazon order illustration:

```text
Order
 ├── Customer
 ├── Payment
 └── Items
```

A weak whole–part example is:

```java
class Team {
    List<Player> players;
}
```

Players can exist if the team is removed, just as employees can survive removal of a department.

```text
Composition = Strong ownership
Aggregation = Weak ownership
```

> **Professional correction:** A field or “uses” relationship does not by itself establish UML composition or aggregation. `Ride`–`Driver` is normally an association because `Ride` does not own the driver's lifecycle; shared aggregation is intentionally weak and a plain association is often clearer.

#### OOP critical learnings and interview answers

- Abstraction hides unnecessary complexity.
- Encapsulation protects valid state.
- Inheritance shares a genuine subtype contract; polymorphism allows one contract to have many implementations.
- Composition is preferred over deep inheritance for flexible behavior.
- Aggregation denotes weaker ownership.
- **Class vs object?** Blueprint versus instance.
- **Why prefer composition over inheritance?** Lower coupling and more flexibility.
- **Real-world polymorphism example?** Payment gateways.
- **Real-world encapsulation example?** A bank account balance.

Fast revision:

| Concept | Remember |
| --- | --- |
| Class | Blueprint |
| Object | Instance |
| Abstraction | Hide complexity |
| Encapsulation | Protect valid state |
| Inheritance | Genuine IS-A specialization |
| Polymorphism | One contract, many implementations |
| Composition | Exclusive whole–part ownership in UML |
| Aggregation | Weak whole–part relationship |

### Source-derived object-modelling tutorial

Bad object modelling creates wrong abstractions, tight coupling, poor extensibility, and messy design. Good modelling makes much of the implementation obvious.

```text
Beginner: Read Problem → Start Coding

Senior:   Find Nouns
          ↓
          Find Relationships
          ↓
          Find Ownership
          ↓
          Find Behaviors
          ↓
          Start Coding
```

Do **not** start from pattern names. The source's negative anti-flow is:

```text
Find Design Patterns
```

Pattern-first selection skips nouns, ownership, and behavior and usually produces the wrong abstractions.

Use this running requirement:

```text
User books seats for a movie show in a theatre.
```

1. **Find nouns.** `User`, `Seat`, `Movie`, `Show`, and `Theatre` are candidate classes.
2. **Remove fake objects.** “User clicks button” does not make `Button` a business class. Likewise, `SearchButton`, `SubmitButton`, and `MoviePage` are UI terms, not domain objects here.
3. **Find core entities.** Ask what information must survive. For BookMyShow, likely core concepts are `User`, `Movie`, `Show`, `Seat`, and `Booking`.
4. **Find relationships and ownership.** Requirements often hide them in sentences:

```text
A theatre contains screens: Theatre → Screen
A screen contains seats:    Screen  → Seat
A movie has shows:          Movie   → Show
```

```text
Theatre
   ↓
 Screen
   ↓
  Seat
```

5. **Find behavior.** Important objects should not be data bags.

Bad:

```java
class Booking {
    String id;
    String status;
}
```

Better:

```java
class Booking {
    void confirm() {}
    void cancel() {}
}
```

Walk the scenario “User books a seat”:

```text
User → Booking → Seat

Booking → confirm()
Booking → cancel()
Seat    → reserve()
Seat    → release()
```

> **Professional correction:** A reusable physical `Seat` and its per-show availability are different concepts. `ShowSeat` is often the better owner of `reserve()`/`release()` because availability belongs to one show.

The modelling mistakes are: creating a class for every noun, ignoring ownership, selecting Strategy/Factory/Observer before the model, and coding before modelling. The reliable order is:

```text
Find Nouns
↓
Remove Fake Objects
↓
Find Relationships and Ownership
↓
Find Behaviors
↓
Start Coding
```

Critical learnings and answers:

- Objects begin with important business nouns, but not every noun becomes a class.
- Relationships and ownership matter more than patterns.
- Ownership drives design quality.
- Good modelling makes implementation easier; patterns come after the model.
- **How do you identify classes?** Find important business nouns, then filter by domain relevance, identity, value, behavior, and persistence needs.
- **How do you identify relationships?** Read requirements for ownership, collaboration, navigation, and lifecycle.
- **Biggest modelling mistake?** Creating classes for everything.
- **What comes before patterns and coding?** Object modelling.

### Source-derived dependency-injection tutorial

A dependency is a collaborator a class needs:

```java
class Car {
    Engine engine;
}
```

Without injection, business logic controls construction:

```java
class PaymentService {
    private RazorpayGateway gateway = new RazorpayGateway();
}
```

This tightly couples the service to Razorpay, makes Stripe/PayPal replacement and mock testing difficult, and mixes business logic with object creation. DI receives the dependency from outside:

```java
class PaymentService {
    private final PaymentGateway gateway;

    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

The service can now work with Razorpay, Stripe, PayPal, or a `MockGateway`.

#### Injection forms

Constructor injection is normally preferred for required dependencies:

- construction cannot omit the dependency;
- fields can be immutable;
- requirements are explicit;
- unit testing is straightforward.

Setter injection supplies an optional or reconfigurable dependency later:

```java
class PaymentService {
    private PaymentGateway gateway;

    public void setGateway(PaymentGateway gateway) {
        this.gateway = gateway;
    }
}
```

Its risks are partial initialization and a setter that is never called. Field injection is terse:

```java
@Autowired
private PaymentGateway gateway;
```

but hides requirements, is less explicit, and complicates isolated unit construction.

#### DI, IoC, and DIP

| Concept | Meaning | Example |
| --- | --- | --- |
| DI | Technique: how a dependency arrives | Constructor injection |
| IoC | Broader principle: control moves outside | Spring container controls construction |
| DIP | Design principle: dependency direction | Policy depends on a stable abstraction |

Traditional construction:

```java
class Car {
    Engine engine = new PetrolEngine();
}
```

With external control:

```java
class Car {
    Engine engine;
}
```

`PaymentGateway gateway;` expresses the abstraction chosen for DIP; passing it through the constructor is DI.

The testing contrast is:

```java
// Without an injectable collaborator
PaymentService service = new PaymentService();

// With DI
PaymentGateway mockGateway = new MockGateway();
PaymentService service = new PaymentService(mockGateway);
```

> **Professional correction:** DI is not a Spring feature and does not require a container. Injecting a concrete dependency can still improve construction and testing; injecting an interface does not by itself satisfy DIP if the contract mirrors a vendor or is owned by the wrong layer.

Critical learnings and answers:

- DI reduces coupling and improves testability.
- Constructor injection is usually preferred; DI helps implement DIP.
- Spring is one DI framework, not the definition of DI.
- **What problem does DI solve?** Hidden construction and tight coupling.
- **DI vs IoC?** DI is a technique; IoC is the broader transfer of control.
- **DI vs DIP?** DI says how a collaborator arrives; DIP says what direction dependencies should point.
- **Constructor vs setter?** Required and valid-at-construction versus genuinely optional/reconfigurable.

```text
Bad:   new Dependency() throughout business logic
Good:  Receive dependencies
Great: Depend on policy-shaped abstractions and use constructor injection
```

### Source-derived design-decision cards

#### Factory vs Builder

| Factory | Builder |
| --- | --- |
| Chooses or creates an object | Builds one object step by step |
| Usually one call | Usually multiple construction steps |
| Focuses on product selection/creation | Focuses on complex assembly |
| Example: payment-gateway factory | Example: Amazon search-request builder |

```text
Need to choose/create a product? → Factory
Need complex staged construction? → Builder
```

#### Strategy vs State

| Strategy | State |
| --- | --- |
| Behavior selected externally | Behavior changes with internal lifecycle |
| Client/configuration chooses | Context state governs permitted behavior |
| Algorithms vary | State transitions vary |
| Example: Uber pricing | Example: Swiggy order lifecycle |

```text
Strategy: Client ─┬─ GoPricing
                  └─ XLPricing

State: Created → Confirmed → Delivered
```

```text
Behavior selected?             → Strategy
Behavior changes due to state? → State
```

#### Observer vs brokered publish/subscribe

| Observer | Pub/sub |
| --- | --- |
| Publisher communicates with subscribers | Producer communicates through a broker |
| Publisher knows subscriber objects | Producer need not know consumers |
| Commonly in-process | Often distributed |
| Example: Instagram follower notification | Example: Kafka |

```text
Observer: Instagram ─┬→ A
                     ├→ B
                     └→ C

PubSub: Producer → Kafka ─┬→ A
                          ├→ B
                          └→ C
```

```text
Same-process direct notification? → Observer
Brokered/distributed decoupling?   → Pub/sub
```

> **Professional correction:** “Same process” and “distributed” are useful signals, not definitions. The decisive distinction is direct subject–observer registration versus broker-mediated temporal/network decoupling.

#### Entity vs Service

| Entity | Service |
| --- | --- |
| Has business identity and continuity | Performs a cohesive operation |
| Holds state and behavior that protect its invariants | Coordinates work or expresses behavior that belongs to no entity naturally |
| Example: `User`, `Booking` | Example: `UserService`, booking application service |

```java
class User {
    Long id;
    String name;
}

class UserService {
    void createUser() {}
}
```

```text
Behavior protects one entity/aggregate invariant? → Entity
Behavior spans collaborators or belongs nowhere naturally? → Service
```

> **Professional correction:** “Data → Entity, behavior → Service” creates an anemic model. Entities should own invariant-preserving behavior; application services orchestrate persistence, external systems, and use cases. Services are not necessarily stateless.

#### Interface vs abstract class

| Interface | Abstract class |
| --- | --- |
| Client-facing capability contract | Partial implementation and shared invariant |
| A class may implement several | A class may extend only one |
| No per-instance mutable state | May hold instance state and constructors |
| Example: `PaymentGateway` | Example: `BaseNotification` |

```text
Need a capability contract or multiple roles? → Interface
Need cohesive shared state, code, or hooks?     → Abstract class
```

#### Cohesion vs coupling

| Cohesion | Coupling |
| --- | --- |
| How strongly responsibilities within a module belong together | How strongly modules depend on each other's decisions |
| Aim high | Keep only deliberate, necessary coupling |

```text
Low cohesion:  UserService = Authentication + Payments + Emails + Reports
High cohesion: AuthService | PaymentService | NotificationService

High coupling: PaymentService → RazorpayGateway
Lower coupling: PaymentService → PaymentGateway
```

#### IS-A vs HAS-A

| IS-A | HAS-A |
| --- | --- |
| Inheritance/generalization | Composition or, more generally, collaboration/association |
| Requires behavioral substitutability | Supports flexible delegation |

```text
Car IS-A Vehicle
Vehicle
   ↑
  Car

Car HAS-A Engine
Car ─── Engine
```

Prefer HAS-A when the relationship is collaboration or independently varying behavior. Use IS-A only when inheritance is genuinely natural and the subtype preserves the base contract.

> **Professional correction:** HAS-A does not automatically mean UML composition. Decide association, aggregation, or composition from ownership and lifecycle.

#### Aggregation vs composition

| Aggregation | Composition |
| --- | --- |
| Weak whole–part semantics | Exclusive, strong whole–part ownership |
| Part can exist independently | Part belongs to at most one whole and normally follows its lifecycle |

```text
Aggregation: Department ─── Employee
Employee can exist independently.

Composition: Order ◆── OrderItem
OrderItem belongs to its Order.
```

```text
Independent lifecycle? → Aggregation or plain association
Exclusive dependent lifecycle? → Composition
```

The decision-card takeaways are: Factory and Builder solve different creation problems; Strategy and State solve different behavior problems; Observer and pub/sub are distinct; prefer stable client-shaped contracts over concrete vendors; favor composition for independent variation; and aim for high cohesion with deliberate low coupling.

Interview answers:

- **Strategy vs State?** External/configured algorithm selection versus lifecycle-driven behavior.
- **Factory vs Builder?** Product selection/creation versus complex construction.
- **Observer vs pub/sub?** Direct subject–subscriber collaboration versus broker-mediated communication.
- **Interface vs abstract class?** Capability contract versus shared implementation/state.
- **Aggregation vs composition?** Weak shared whole–part semantics versus exclusive ownership.
- **Ideal structural goal?** High cohesion and low, explicit coupling.

Fast revision:

| Comparison | Remember |
| --- | --- |
| Factory vs Builder | Choose/create vs construct |
| Strategy vs State | Selected algorithm vs lifecycle behavior |
| Observer vs PubSub | Direct vs brokered |
| Entity vs Service | Identity/invariants vs orchestration/cross-object operation |
| Interface vs Abstract Class | Contract vs shared implementation/state |
| Cohesion vs Coupling | High vs deliberate low |
| IS-A vs HAS-A | Subtyping vs delegation/collaboration |
| Aggregation vs Composition | Weak vs exclusive ownership |

### Source-derived LLD interview walkthrough

Do not jump into classes. Use the complete flow:

```text
Requirements
↓
Entities
↓
Relationships
↓
Patterns
↓
APIs
↓
Concurrency
↓
Follow Ups
```

1. **Gather requirements.** Ask about core features, assumptions, scale, and whether users act concurrently.

   Bad:

   ```text
   Interviewer: Design BookMyShow
   Candidate: Let's create Movie class...
   ```

   Better:

   ```text
   Can users book multiple seats?
   Can booking fail midway?
   Should seats be locked?
   ```

2. **Identify candidate entities.** For a parking lot, nouns suggest `ParkingLot`, `ParkingFloor`, `ParkingSpot`, `Vehicle`, and `Ticket`. Treat “nouns → candidate entities” as a discovery aid, not an automatic mapping.
3. **Identify relationships.** Ask “Who owns whom?” and “Who talks to whom?”

   ```text
   ParkingLot
    └── ParkingFloor
         └── ParkingSpot
   ```

   Use association, composition, and inheritance only when their semantics are needed.
4. **Identify pattern pressure.**

   | Requirement signal | Candidate pattern |
   | --- | --- |
   | Multiple algorithms | Strategy |
   | State transitions | State |
   | Notifications | Observer |
   | Product-selection complexity | Factory |
   | Complex staged construction | Builder |

   Start simple; do not force a pattern.
5. **Define APIs from requirements, not imagination.**

   ```java
   parkVehicle(vehicle)
   unparkVehicle(ticket)
   findSpot(vehicle)
   ```

6. **Think about concurrency.** Ask whether two users can modify the same authoritative data.

   | Problem | Concern |
   | --- | --- |
   | BookMyShow | Seat locking |
   | Splitwise | Balance updates |
   | Ride sharing | Driver assignment |
   | ATM | Concurrent withdrawals |
   | Inventory systems | Stock reservation and overselling |

   Many candidates skip this step even after listing entities and APIs.

7. **Discuss follow-ups.** Examples include EV vehicles, multiple cities, multiple payment methods, and notifications. Interviewers often use these changes to evaluate extensibility and trade-off reasoning.

Critical learnings:

- Requirements matter more than code.
- Good object modelling resolves much of the design.
- Patterns are tools, not goals.
- Concurrency is frequently missed.
- Follow-ups often determine interview performance.

```text
Don't design classes first.
Design understanding first.
```

### Source-derived mistake clinic

1. **Jumping to classes too early.** Listing `Movie`, `Theatre`, `Seat`, and `Booking` before requirements creates wrong assumptions. Use `Requirements → Entities → Design`.
2. **Pattern-first thinking.** Saying “I will use Strategy” before understanding the problem causes overengineering. Use `Problem → Pattern`, never `Pattern → Problem`.
3. **Huge god classes.** A `UserService` that owns authentication, payments, emails, and reports violates SRP and is difficult to test and maintain. Split cohesive capabilities such as `AuthService`, `PaymentService`, and `NotificationService`.
4. **Unnecessary inheritance.**

   ```text
   Vehicle
    ├── Car
    ├── Bike
    ├── Truck
    ├── ElectricCar
    ├── SportsCar
    └── LuxuryCar
   ```

   This hierarchy explodes when dimensions vary; compose variable behavior.
5. **Large type conditionals.**

   ```java
   if (type == GO) {}
   else if (type == XL) {}
   else if (type == PREMIER) {}
   ```

   They can violate OCP when an open set of behavior keeps growing; Strategy is one candidate.

   > **Professional correction:** A closed, small enum or lookup table can be clearer than polymorphism. Use Strategy only when variation is behavior-rich or independently extensible.
6. **Ignoring concurrency.** Seat booking without an authoritative atomic hold can double-book. Also check ATM, ride assignment, Splitwise, and inventory systems.
7. **Interfaces for everything.** Adding `IUserService` for one concrete implementation is not useful by itself. Introduce a contract at a real client, variation, test, or integration boundary.
8. **Missing follow-ups.** Do not stop after a class diagram; discuss scalability, likely features, extensibility, and trade-offs.
9. **Ignoring trade-offs.** “Factory is always good” is false. Explain benefits, drawbacks, and alternatives. Interviewers respond well when you name benefits, drawbacks, and simpler alternatives—not when you treat any pattern as universally good.
10. **Overengineering.** Factory + Builder + Strategy + Observer + Decorator for a tiny problem adds complexity without benefit. Add complexity only when requirements demand it.

Critical learnings:

- Requirements matter more than code.
- Simpler design is usually better.
- Patterns are tools, not goals.
- Concurrency is often overlooked.
- Good tradeoff discussion impresses interviewers.

Fast revision:

| Mistake | Remember |
| --- | --- |
| Jumping to classes | Gather requirements first |
| Pattern-first thinking | Understand the problem first |
| God classes | Split by cohesive reason to change |
| Excessive inheritance | Prefer composition for variation |
| Large if/else | Consider Strategy when the axis is open |
| Ignoring concurrency | Think multi-user and authoritative atomicity |
| Interfaces everywhere | Abstract only at meaningful boundaries |
| Missing follow-ups | Discuss future changes |
| Ignoring trade-offs | Every design has costs |
| Overengineering | Keep the design as simple as requirements allow |

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

## Provenance

- **Source-derived:** “Source-derived OOP tutorial” comes from `01-core/oop.md`; “Source-derived object-modelling tutorial” from `03-problem-framework/object-modelling.md`; “Source-derived dependency-injection tutorial” from `01-core/dependency-injection.md`; “Source-derived design-decision cards” from `01-core/design-decisions.md`; and the interview walkthrough and mistake clinic from `00-revision/lld-interview-flow.md` and `00-revision/common-lld-mistakes.md`.
- **Editorial synthesis:** The overview, section ordering, removal of exact repeated reminders, links, tables that consolidate equivalent cards, and mapping of source material into this repository's topic template.
- **Professional correction:** Every callout explicitly labeled **Professional correction**, plus the existing guidance on invariants, client-shaped contracts, behavioral substitutability, authoritative concurrency control, weak UML aggregation semantics, and the costs of abstractions. These corrections refine source shorthand without deleting its interview-learning intent.
- **Excluded duplicate:** No `concurrency.md` content was imported; the provided source set contains no required standalone concurrency tutorial, avoiding the mislabeled duplicate while retaining the unique concurrency checks present in the interview and mistakes sources.
