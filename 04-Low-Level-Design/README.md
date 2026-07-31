# Low-Level Design

## Beginner vocabulary

**Everyday mental model:** software is like a workplace. People and tools have information, perform tasks, and rely on one another. Low-level design is the detailed plan for assigning those jobs and connections before or while writing code.

- An **object** is one working thing in a running program, like one particular bank account. It combines **state**—the facts it remembers, such as a balance—with **behavior**—the actions it can perform, such as withdrawing money.
- A **class** is the reusable blueprint used to create similar objects. `Account` is a class; “Riya's account with a ₹500 balance” is one object made from it.
- An **interface** is a promise of available actions without fixing how they are performed. It resembles a wall socket: a device depends on the socket's shape and rules, not the wiring behind it.
- A **dependency** is something another part needs to do its job. A checkout object that needs a payment provider has that provider as a dependency.
- A **design pattern** is a named, reusable arrangement for a recurring design problem. It is like a familiar floor plan, not a finished building or code to copy blindly.
- **Unified Modeling Language (UML)** is a shared diagram notation for drawing software structure, call order, and lifecycle changes.
- **Coupling** is how strongly parts rely on each other's details. Tight coupling is like an appliance wired directly into a building: replacement is difficult.
- **Cohesion** is how naturally the jobs inside one part belong together. High cohesion is like a billing desk that handles related billing work instead of billing, hiring, and deliveries.
- **Concurrency** means two or more actions can overlap. For example, two
  customers may try to reserve the same final seat at nearly the same time.
  The design must keep shared state correct regardless of which action finishes
  first.

- **Problem:** when responsibilities and dependencies are unclear, one small requirement can force unrelated edits.
- **Internal mechanics:** LLD assigns state and behavior to objects, groups similar objects through classes, puts promises at useful interfaces, and makes dependencies visible.
- **Concrete example:** ticket booking can give one `Booking` object its lifecycle state and behavior while a payment interface hides provider-specific details.
- **Edge cases:** two users may claim the same seat, payment may succeed after a timeout, or a dependency may be unavailable.
- **Trade-off:** extra classes, interfaces, patterns, and diagrams can make change safer but can also make a simple program harder to follow.

> Low-level design (LLD) turns requirements into understandable objects, responsibilities, interfaces, and interactions.

## Overview

LLD describes how the parts inside a software system collaborate. It sits between requirements and implementation: requirements explain what the system must do, while LLD explains which objects own each rule and how those objects work together.

Use this section as a learning path. Begin with modelling principles, apply SOLID to boundaries, use Unified Modeling Language (UML) to communicate the design, and introduce patterns only when a concrete problem justifies them.

## Core concepts

- [Principles](./Principles/README.md) — object-oriented modelling, dependency injection, composition, design decisions, interview workflow, and common mistakes.
- [SOLID](./SOLID/README.md) — five principles for maintainable object-oriented boundaries.
- [Design Patterns](./Design-Patterns/README.md) — selection guidance and implementation mechanics for 15 creational, structural, and behavioral patterns.
- [UML](./UML/README.md) — class, sequence, and state diagrams for communicating structure and behavior.

## How it works

Follow this problem-first sequence:

```text
Requirements
    ↓
Use cases and invariants
    ↓
Objects, responsibilities, and relationships
    ↓
Interfaces and interactions
    ↓
Patterns only where pressure exists
    ↓
Concurrency, failure, and trade-off checks
```

An **invariant** is a rule that must always remain true. For example, one seat cannot belong to two confirmed bookings for the same show. Naming the invariant first makes it easier to decide which object owns the behavior and where concurrency control belongs.

## Example

For a ticket-booking requirement, first identify the booking flow and the no-double-booking invariant. Then model `ShowSeat` as the per-show seat state, let `Booking` own valid lifecycle transitions, and use an application service to coordinate persistence and payment. A sequence diagram can then show seat holding, payment failure, and release.

## Trade-offs

More abstractions can localize change and improve testing, but they also add types, wiring, and indirect control flow. A small direct design is preferable when it meets the current requirements clearly. Add an interface, pattern, or diagram only when it resolves a real boundary, variation, or communication problem.

## Common pitfalls

- Starting with classes or patterns before clarifying requirements.
- Turning every noun into a class.
- Hiding all behavior in a large service instead of protecting invariants near their state.
- Using inheritance for code reuse without behavioral substitutability.
- Ignoring concurrent updates, failure compensation, or persistence constraints.
- Presenting a diagram without explaining the decision it supports.

## When to use it

Use LLD when a feature has meaningful domain rules, object lifecycles, integrations, or collaboration between components. Keep the design lightweight for simple data transformations or straightforward create/read/update/delete operations with little behavior.

## Further reading

Read the topics in this order for a guided introduction:

1. [Low-Level Design Principles](./Principles/README.md)
2. [SOLID](./SOLID/README.md)
3. [Unified Modeling Language](./UML/README.md)
4. [Design Patterns](./Design-Patterns/README.md)
