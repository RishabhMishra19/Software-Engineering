# Unified Modeling Language (UML)

## Beginner vocabulary

**Everyday mental model:** think of UML as the set of maps used for a building: one map shows rooms, another shows how people move through them, and another shows which modes an alarm can enter. An **object** is one actual thing in a running program. Its **state** is the information or lifecycle condition it currently holds, and its **behavior** is what it can do. A **class** is the blueprint for similar objects. An **interface** is a promise of operations that different classes can fulfill. A **dependency** means one part needs another to do some work. A **design pattern** is a named, reusable arrangement of such parts.

An **entity** is a domain object with a continuing identity, such as one
specific order. A class is the blueprint, an object is one runtime instance,
and an entity is an object whose identity remains meaningful as its state
changes.

**Coupling** is how strongly parts depend on one another's details. **Cohesion** is how closely the responsibilities inside one part belong together. UML can expose both, but a diagram does not automatically improve either one.

- **Problem:** prose can hide structure and call order.
- **Internal mechanics:** class diagrams show blueprints and structural links, sequence diagrams show messages over time, and state-machine diagrams show legal lifecycle changes.
- **Concrete example:** booking diagrams can separately show that `Booking` owns order lines, that payment occurs after a seat hold, and that a booking moves from created to confirmed or cancelled.
- **Edge cases:** ambiguous arrows, missing failure branches, illegal transitions, and diagrams that drift away from code.
- **Trade-off:** diagrams can create quicker shared understanding but require time and precision to maintain another representation.

## Overview

Unified Modeling Language (UML) is a standardized visual language for describing software structure and behavior. It makes relationships and flows visible when prose alone would be hard to review.

In low-level design (LLD), class, sequence, and state-machine diagrams answer complementary questions:

- **Class diagram:** what types and structural relationships exist?
- **Sequence diagram:** how do participants collaborate over time for one scenario?
- **State-machine diagram:** how does one entity move between lifecycle states?

Use the smallest diagram that resolves the design question. UML is a communication tool, not a substitute for requirements or code.

## Why do we need it?

Prose alone can hide ownership, cardinality, dependency direction, call order, failure paths, and allowed transitions. A focused diagram makes assumptions reviewable before implementation and helps interviewers follow a design. Formal notation matters when ambiguity matters; in a time-boxed interview, a clear legend and consistent notation matter more than decorative completeness.

## How does it work?

### Class diagrams

**Everyday mental model:** a class diagram is like a labeled floor plan: it shows what kinds of rooms exist and how they connect, not the order in which a person walks through them.

**Problem:** prose can leave ownership, quantity, and dependency direction ambiguous.

A class box may show name, attributes, and operations. Visibility is commonly `+` public, `-` private, `#` protected, and `~` package. Italics or `{abstract}` mark abstract classifiers; `<<interface>>` marks an interface.

Key relationships:

| Relationship | Notation | Meaning |
| --- | --- | --- |
| Generalization | solid line, hollow triangle toward parent | subtype inheritance |
| Realization | dashed line, hollow triangle toward interface | implementation of a contract |
| Association | solid line | structural link between instances |
| Dependency | dashed arrow toward supplier | temporary use, such as a parameter or call |
| Shared aggregation | hollow diamond at whole | weak whole–part association; use sparingly |
| Composition | filled diamond at whole | exclusive whole–part ownership and lifecycle |

Multiplicity belongs at association ends: `1`, `0..1`, `*`, `0..*`, or `1..*`. Navigability arrows, role names, and constraints can remove ambiguity.

```mermaid
classDiagram
    class PaymentPort {
      <<interface>>
      +charge(request) Receipt
    }
    class CheckoutService {
      -payments PaymentPort
      +checkout(cart) Order
    }
    class StripeAdapter {
      -client StripeClient
      +charge(request) Receipt
    }
    class Order {
      +confirm()
      +cancel()
    }
    class OrderLine

    PaymentPort <|.. StripeAdapter : realizes
    CheckoutService --> PaymentPort : uses
    CheckoutService ..> Order : creates
    Order "1" *-- "1..*" OrderLine : owns
```

The filled diamond is justified only if `OrderLine` is an exclusive part of one `Order`. A `Ride` referencing a `Driver` is normally a plain association, because the ride does not own the driver's lifecycle. Shared aggregation adds little beyond association in many software models; do not use a diamond merely because one class has a field.

**Edge cases and trade-offs:** bidirectional links, qualified associations, generic types, and framework-generated relationships can make a precise diagram crowded. Show the detail needed for the decision; omitted detail improves readability but can hide an important constraint.

### Sequence diagrams

**Everyday mental model:** a sequence diagram resembles a time-ordered conversation transcript showing who speaks to whom and when.

**Problem:** a list of components does not reveal call order, waits, branches, or recovery after failure.

Sequence diagrams model one scenario. Participants appear across the top, time flows downward, **lifelines** show each participant's presence over time, and activation bars may show execution. Common messages include synchronous calls, asynchronous messages, returns, and self-calls.

Combined fragments express control:

- `alt` for mutually exclusive branches;
- `opt` for an optional branch;
- `loop` for repetition;
- `par` for concurrent fragments;
- `break` for terminating the enclosing interaction.

```mermaid
sequenceDiagram
    actor User
    participant API as Booking API
    participant Seats as Seat Service
    participant Pay as Payment Port
    participant Store as Booking Repository

    User->>API: book(showId, seatIds, requestId)
    API->>Seats: tryHold(showId, seatIds)
    alt hold acquired
        API->>Pay: authorize(amount, requestId)
        alt payment authorized
            API->>Store: saveConfirmedBooking()
            API->>Seats: confirmHold()
            API-->>User: confirmation
        else payment failed
            API->>Seats: releaseHold()
            API-->>User: payment failure
        end
    else seat unavailable
        API-->>User: conflict
    end
```

Show the behavior that affects the decision: failure, retry, transaction boundary, asynchronous delivery, or concurrency. Avoid turning the diagram into a list of every getter and mapper.

**Edge cases and trade-offs:** retries can duplicate side effects, asynchronous replies may arrive out of order, and concurrent branches may race. Adding every return and internal call makes the diagram accurate but unreadable; omitting failure paths can make it dangerously reassuring.

### State-machine diagrams

**Everyday mental model:** a traffic light has meaningful modes, and only certain events permit movement from one mode to another.

**Problem:** a raw status value does not explain which actions are legal, what triggers change, or what happens during that change.

A state machine models a stateful entity's permitted transitions. A transition is commonly labeled:

```text
event [guard] / effect
```

States may define entry, exit, and internal behavior. Include initial and final pseudostates where useful. A state diagram should distinguish a **state**—a meaningful behavioral condition—from a raw status field.

```mermaid
stateDiagram-v2
    [*] --> Created
    Created --> Confirmed: paymentAuthorized
    Created --> Cancelled: cancel
    Confirmed --> Preparing: accept
    Confirmed --> Cancelled: cancel [beforeCutoff] / refund
    Preparing --> OutForDelivery: dispatch
    OutForDelivery --> Delivered: deliver
    Delivered --> [*]
    Cancelled --> [*]
```

List illegal or guarded transitions explicitly when they are important. A transition table can complement the picture for exhaustive validation.

**Edge cases and trade-offs:** two events may arrive concurrently, timers may trigger transitions, recovery may restart from a persisted state, and nested or parallel states may be needed. A full state machine gives precision but can be heavier than a simple status plus a small validated transition table.

### Choosing and combining diagrams

| Design question | Best starting diagram |
| --- | --- |
| Types, ownership, cardinality, dependencies | Class |
| One request's calls, order, branches, async work | Sequence |
| Valid lifecycle transitions and state-dependent behavior | State machine |

For ticket booking, a class diagram describes `Show`, `ShowSeat`, and `Booking`; a sequence diagram exposes seat-hold and payment failure ordering; a state machine defines booking or seat lifecycle. These diagrams should agree but need not duplicate every detail.

### Modelling workflow

1. State the diagram's question and scope.
2. Add only participants or states relevant to that question.
3. Label relationships, multiplicities, messages, guards, and important failures.
4. Validate the diagram against a concrete scenario and domain invariants.
5. Update or discard it when it no longer communicates the implementation.

### Common mistakes

- Drawing classes before clarifying requirements and invariants.
- Using composition diamonds for every field or collection.
- Omitting multiplicity and role names on ambiguous associations.
- Modelling object call order in a class diagram.
- Using a sequence diagram as a static architecture map.
- Showing only a happy path when failures determine correctness.
- Treating statuses as states without defining events, guards, or legal transitions.
- Mixing business lifecycle state with transient UI state.
- Over-specifying framework details that obscure the design decision.

### Source-derived diagram walkthroughs

The source tutorial begins with the communication problem: instead of describing classes, relationships, interactions, and state changes only in prose, draw the question that matters. For LLD interviews, the three most useful views are class, sequence, and state diagrams.

#### Class diagram walkthrough

A class diagram shows classes, attributes, methods, and relationships. The source's parking-lot sketch starts with:

```text
ParkingLot
    |
    +---- ParkingFloor
               |
               +---- ParkingSpot
```

and expands the class compartments:

```text
+----------------+
| ParkingLot     |
+----------------+
| floors         |
+----------------+
| park()         |
| unpark()       |
+----------------+

         |
         v

+----------------+
| ParkingFloor   |
+----------------+
| spots          |
+----------------+

         |
         v

+----------------+
| ParkingSpot    |
+----------------+
| id             |
| type           |
+----------------+
```

Use a class diagram for object modelling, structural relationships, and the main LLD overview. A common mistake is creating this diagram before understanding requirements.

> **Professional correction:** The plain arrows above preserve the source sketch's meaning but do not claim ownership. In precise UML, label associations and multiplicities, and use a filled composition diamond only for exclusive lifecycle ownership.

#### Sequence diagram walkthrough

A sequence diagram answers “Who calls whom, and in what order?” It models behavior, not static structure. The source's BookMyShow flow is:

```text
User
 |
 | Book Seat
 v
BookingService
 |
 | Lock Seat
 v
SeatService
 |
 | Save Booking
 v
BookingRepository
```

The more detailed call/return-oriented version is:

```text
User
 |
 | bookSeat()
 v
BookingService
 |
 | lockSeat()
 v
SeatService
 |
 | success
 v
BookingService
 |
 | saveBooking()
 v
BookingRepository
```

Use sequence diagrams for request flows, APIs, and service interactions. Do not put static class relationships in a sequence diagram.

> **Professional correction:** A successful seat lock alone is not the whole booking contract. The Mermaid diagram above retains the professional failure branches for unavailable seats and payment failure, including release/compensation.

#### State diagram walkthrough

A state diagram shows how an object changes behavioral state. The source gives a Swiggy-style order lifecycle:

```text
Created
   |
   v
Confirmed
   |
   v
Preparing
   |
   v
OutForDelivery
   |
   v
Delivered
```

and an ATM transaction lifecycle:

```text
Idle
  |
  v
CardInserted
  |
  v
Authenticated
  |
  v
TransactionProcessing
  |
  v
Completed
```

Use this view for order and booking lifecycles, workflow systems, and designs that may warrant the State pattern. Do not use a class diagram to represent transitions.

> **Professional correction:** A list of statuses is only a starting sketch. A state machine becomes precise when transitions identify events, guards, effects, initial/final states, and illegal paths.

### Source-derived diagram selection card

| Problem | Diagram |
| --- | --- |
| Object modelling and ownership | Class diagram |
| Request or interaction flow | Sequence diagram |
| Status/lifecycle transitions | State-machine diagram |

```text
Class Diagram
    ↓
What exists?

Sequence Diagram
    ↓
What happens?

State Diagram
    ↓
What changes?
```

### Source-derived interview example: BookMyShow

Each diagram answers a different question.

Class candidates:

```text
Movie
Theatre
Screen
Seat
Booking
```

Interaction flow:

```text
User
  |
  v
BookingService
  |
  v
SeatService
  |
  v
PaymentService
```

Seat lifecycle:

```text
Available
    |
    v
Locked
    |
    v
Booked
```

> **Professional correction:** For a fuller domain model, distinguish a physical `Seat` from a per-show `ShowSeat`; add lock expiry, release, payment failure, and atomic store enforcement where correctness depends on them.

### Source-derived critical learnings and answers

- Class diagrams model structure.
- Sequence diagrams model behavior and interaction order.
- State diagrams model lifecycle transitions.
- Most LLD interviews need at least a class diagram.
- Sequence diagrams explain workflows clearly.
- State diagrams are especially useful for state-heavy systems.
- **Class vs sequence diagram?** Structure versus interaction flow.
- **When should you use a state diagram?** When permitted behavior changes with lifecycle state.
- **Most common UML diagram in LLD interviews?** The class diagram.
- **Is perfect UML notation required in interviews?** No. Clear communication matters more, while ownership, multiplicity, failures, and transitions deserve precision.

Fast revision:

| Diagram | Purpose |
| --- | --- |
| Class | Structure |
| Sequence | Interaction flow |
| State machine | State transitions |

## Advantages

- Makes structure, interaction, and lifecycle assumptions visible.
- Reveals missing cardinalities, ownership, failure paths, and transitions.
- Gives teams a language-independent design vocabulary.
- Supports focused review before implementation.
- Helps explain an interview design quickly.

## Limitations

- Diagrams drift unless maintained with the code.
- Dense diagrams become harder to understand than prose.
- UML does not establish runtime correctness, performance, or transactional guarantees.
- Tool-specific syntax may support only a subset of the UML standard.
- Formal precision can cost more than it adds for short-lived sketches.

## Real-world examples

- A class diagram captures payment ports and provider adapters.
- A sequence diagram documents checkout compensation after payment failure.
- A state machine defines order, booking, document, ATM, or vending-machine lifecycles.
- A `par` fragment documents concurrent calls; notes identify where an atomic store operation enforces correctness.

## Interview Questions

1. **How do class, sequence, and state-machine diagrams differ?** A class diagram shows static structure, a sequence diagram shows interactions over time for one scenario, and a state-machine diagram shows valid lifecycle states and transitions.
2. **What is the difference between association, shared aggregation, and composition?** Association is a general structural link. Shared aggregation is a weak whole–part relationship whose parts remain independent. Composition is exclusive whole–part ownership with a normally dependent lifecycle.
3. **How do you show interface realization and a temporary dependency?** Realization uses a dashed line with a hollow triangle pointing to the interface. A temporary dependency uses a dashed arrow pointing to the supplier.
4. **Where do multiplicities appear, and what does `0..1` mean?** Multiplicities appear at association ends. `0..1` means that an object may be linked to no instance or one instance at that end.
5. **How would you show errors, optional behavior, loops, and concurrency in a sequence diagram?** Use `alt` for success/error branches, `opt` for optional behavior, `loop` for repetition, and `par` for concurrent fragments.
6. **What belongs on a state transition label?** Use `event [guard] / effect`: the trigger, an optional condition, and an optional action caused by the transition.
7. **Do interviews require perfect UML notation?** No. Use a clear legend and consistent notation, then spend precision on ownership, multiplicity, failure paths, guards, and legal transitions.
8. **Interview tip:** declare a compact legend, model one critical flow, and spend precision on ownership, multiplicity, failures, and state transitions.

## References

- [Object Management Group: UML 2.5.1 Specification](https://www.omg.org/spec/UML/2.5.1/About-UML)
- [Martin Fowler: UML Distilled resources](https://martinfowler.com/books/uml.html)
- [Mermaid: Class Diagrams](https://mermaid.js.org/syntax/classDiagram.html)
- [Mermaid: Sequence Diagrams](https://mermaid.js.org/syntax/sequenceDiagram.html)
- [Mermaid: State Diagrams](https://mermaid.js.org/syntax/stateDiagram.html)
- [Related: Principles](../Principles/README.md)

## Provenance

- **Source-derived:** “Source-derived diagram walkthroughs,” selection card, BookMyShow example, critical learnings, interview answers, ASCII diagrams, and fast-revision card derive from `01-core/uml.md`.
- **Editorial synthesis:** The repository topic template, formal notation tables, Mermaid renderings, modelling workflow, consolidated mistakes, links, and removal of exact repeated purpose statements.
- **Professional correction:** Every callout explicitly labeled **Professional correction**, plus the existing distinctions between association/aggregation/composition, complete sequence failure paths, and event/guard/effect state-machine semantics. These additions preserve the source sketches while preventing their simplified arrows and status lists from being mistaken for complete UML.
