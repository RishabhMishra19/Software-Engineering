# Parking Lot

A low-level design for a multi-floor parking facility with multiple entry and exit gates, compatible vehicle/spot types, pluggable allocation and pricing, tickets, payments, and live availability.

## Problem Statement

Design a parking lot that admits bikes, cars, and trucks, assigns each vehicle a compatible free spot, issues a ticket, calculates a fee at exit, records payment, and releases the spot. Multiple gates may operate concurrently without allocating the same spot twice.

## Functional Requirements

- Manage multiple floors, spots, entry gates, and exit gates.
- Park and unpark bikes, cars, and trucks in compatible spots.
- Find an available spot and issue one active ticket per vehicle.
- Show available spots by floor and spot type.
- Calculate hourly or flat fees and record payment state.
- Reject invalid vehicles, tickets, transitions, and duplicate parking.

Full scope: [Requirements.md](Requirements.md).

## Non Functional Requirements

- Near-constant-time availability lookup and updates.
- Thread-safe allocation and release across concurrent gates.
- Extensible allocation and fee policies.
- Deterministic, testable time through an injected `Clock`.
- Explicit invariants, validation, and domain-specific errors.

## Design Decisions

- **Parking lot as aggregate root:** it coordinates floors, active tickets, allocation, payment, and release.
- **Floors partition spots:** this mirrors a physical facility and keeps availability local.
- **Availability pools:** each floor maintains a deque per spot type, avoiding a scan of every spot.
- **Strategy pattern:** allocation and fee behavior vary independently of gate orchestration.
- **Atomic lifecycle operations:** a lot-level lock protects pool, spot, ticket, and vehicle-index mutations as one transaction.
- **Gates stay thin:** entry delegates parking; exit delegates validation, pricing, payment, and release.
- **Display is a projection:** availability is read from pool counts rather than recomputed by scanning spots.

Detailed rationale: [Design.md](Design.md).

## Class Diagram

The model centers on `ParkingLot`, with floors owning spots and gates delegating to the aggregate. Tickets bind vehicles to spots; payments settle tickets.

[Open the full class diagram](UML/README.md).

## Sequence Diagram

Entry atomically claims a compatible spot before creating a ticket. Exit validates an active ticket, calculates and captures payment, marks the ticket paid, then returns the spot to its availability pool.

[Open the entry and exit sequences](Sequence-Diagrams/README.md).

## Implementation

[`implementation/java/ParkingLot.java`](implementation/java/ParkingLot.java) is a cohesive Java 17, single-file implementation. All domain types are nested so it can be compiled and run directly:

```bash
javac ParkingLot.java
java -ea ParkingLot
```

The `main` method exercises compatibility, exhaustion, duplicate admission, hourly billing with a fixed clock, state transitions, release, and concurrent single-spot allocation. It uses explicit checks in addition to assertions, so validation remains active without `-ea`.

## Complexity

Let `F` be floors, `T` spot types (three here), and `A` active tickets.

- Allocate: `O(F)` with `O(1)` pool access per floor; no spot scan.
- Release: `O(1)` using ticket-to-spot and floor indexes.
- Active ticket lookup: expected `O(1)`.
- Availability for one floor/type: `O(1)`; complete snapshot: `O(F × T)`.
- Space: `O(S + A)`, where `S` is total spots.

The default strategy checks floors in insertion order. A global nearest-spot heap could reduce selection to `O(log S)` when floor scans become significant.

## Future Improvements

- EV charging, accessible, compact, and oversized compatibility rules.
- Reservations, grace periods, lost-ticket handling, subscriptions, and VIP policies.
- Weekend, peak-hour, progressive, and dynamic pricing strategies.
- Asynchronous display-board updates via observer/events.
- Durable database state, Redis-backed distributed allocation, and idempotent payment gateway integration.
- Allocation by gate distance, occupancy balancing, or license-plate recognition.
- Metrics, audit history, reconciliation, and failure recovery.

## Related Files

- [Requirements](Requirements.md)
- [Detailed design](Design.md)
- [UML class diagram](UML/README.md)
- [Sequence diagrams](Sequence-Diagrams/README.md)
- [Java 17 implementation](implementation/java/ParkingLot.java)
