# Parking Lot

A low-level design for a multi-floor parking facility with multiple entry and exit gates, compatible vehicle/spot types, pluggable allocation and pricing, tickets, payments, and live availability.

> **Provenance.** Source-derived teaching content is restored from
> `/tmp/software-engineering-sources/Learning-LLD/04-case-studies/parking-lot.md`.
> The runnable implementation and professional corrections are editorial
> hardening of that design. [Design.md](Design.md) labels the two tracks
> explicitly so source behavior is preserved without presenting known
> simplifications as production-safe behavior.

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

- **Two documented tracks:** the source-derived design preserves the
  incremental interview walkthrough, explicit class hierarchies, display
  board, source strategies, flows, patterns, follow-ups, and revision notes;
  the hardened track explains every deliberate implementation correction.
- **Parking lot as aggregate root:** it coordinates floors, active tickets, allocation, payment, and release.
- **Floors partition spots:** this mirrors a physical facility and keeps availability local.
- **Availability pools:** each floor maintains a deque per spot type, avoiding a scan of every spot.
- **Strategy pattern:** allocation and fee behavior vary independently of gate orchestration.
- **Atomic lifecycle operations:** a lot-level lock protects pool, spot, ticket, and vehicle-index mutations as one transaction.
- **Gates stay thin:** entry delegates parking; exit delegates validation, pricing, payment, and release.
- **Display is a projection:** availability is read from pool counts rather than recomputed by scanning spots.

Detailed source coverage and corrected rationale: [Design.md](Design.md).

## Class Diagram

The source-derived diagram retains `Bike`/`Car`/`Truck` and spot subclasses;
the hardened diagram uses typed values and adds compatibility, payment
processing, pools, and lifecycle coordination. Both center on `ParkingLot`,
with floors owning spots and gates delegating to the aggregate.

[Open the full class diagram](UML/README.md).

## Sequence Diagram

The sequence documentation includes the source's compact entry/exit teaching
flow and the hardened atomic lifecycle. In the corrected flow, entry claims a
compatible spot before creating a ticket; exit validates, prices, captures,
marks paid, and only then returns the spot to its pool.

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
