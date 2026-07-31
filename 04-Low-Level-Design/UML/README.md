# Unified Modeling Language (UML)

## Overview

UML is a standardized visual language for describing software structure and behavior. In low-level design, class, sequence, and state-machine diagrams answer complementary questions:

- **Class diagram:** what types and structural relationships exist?
- **Sequence diagram:** how do participants collaborate over time for one scenario?
- **State-machine diagram:** how does one entity move between lifecycle states?

Use the smallest diagram that resolves the design question. UML is a communication tool, not a substitute for requirements or code.

## Why do we need it?

Prose alone can hide ownership, cardinality, dependency direction, call order, failure paths, and allowed transitions. A focused diagram makes assumptions reviewable before implementation and helps interviewers follow a design. Formal notation matters when ambiguity matters; in a time-boxed interview, a clear legend and consistent notation matter more than decorative completeness.

## How does it work?

### Class diagrams

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

### Sequence diagrams

Sequence diagrams model one scenario. Participants appear across the top, time flows downward, lifelines show existence, and activation bars may show execution. Common messages include synchronous calls, asynchronous messages, returns, and self-calls.

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

### State-machine diagrams

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

1. How do class, sequence, and state-machine diagrams differ?
2. What is the difference between association, shared aggregation, and composition?
3. How do you show interface realization and a temporary dependency?
4. Where do multiplicities appear, and what does `0..1` mean?
5. How would you show errors, optional behavior, loops, and concurrency in a sequence diagram?
6. What belongs on a state transition label?
7. Do interviews require perfect UML notation?
8. **Interview tip:** declare a compact legend, model one critical flow, and spend precision on ownership, multiplicity, failures, and state transitions.

## References

- [Object Management Group: UML 2.5.1 Specification](https://www.omg.org/spec/UML/2.5.1/About-UML)
- [Martin Fowler: UML Distilled resources](https://martinfowler.com/books/uml.html)
- [Mermaid: Class Diagrams](https://mermaid.js.org/syntax/classDiagram.html)
- [Mermaid: Sequence Diagrams](https://mermaid.js.org/syntax/sequenceDiagram.html)
- [Mermaid: State Diagrams](https://mermaid.js.org/syntax/stateDiagram.html)
- [Related: Principles](../Principles/README.md)
