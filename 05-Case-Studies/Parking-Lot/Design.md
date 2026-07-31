# Parking Lot Design

## Domain Model

`ParkingLot` is the aggregate root. A lot contains `ParkingFloor` objects; each floor owns `ParkingSpot` objects and availability pools. `EntryGate` and `ExitGate` are stateless adapters over the aggregate. A `Ticket` records admission and binds a `Vehicle` to a spot. A `Payment` records the result of settling a ticket.

The source model's explicit bike, car, and truck subclasses are represented by immutable `Vehicle` values and enums in the single-file implementation. This removes subtype boilerplate while preserving type-safe compatibility. The same simplification applies to spot subclasses.

## Availability and Allocation

Each floor keeps an `EnumMap<SpotType, ArrayDeque<ParkingSpot>>`. Adding a spot inserts it into exactly one pool. Allocation removes from the head; release appends to the tail.

- Availability count: `O(1)`.
- Claim or return within a known floor/type: `O(1)`.
- Default lowest-floor-first selection: `O(F)`, where `F` is the number of floors.

The allocation policy is exposed through `ParkingSpotStrategy`. The default `LowestFloorFirstStrategy` asks each floor's matching pool in insertion order. Alternatives such as nearest-gate, VIP-first, or occupancy-balancing can change selection without changing ticket or payment logic.

Compatibility is explicit through `SpotCompatibility`. The baseline policy maps bike to bike, car to car, and truck to truck. A richer policy could permit a bike in a car spot or model compact, accessible, EV, and oversized constraints.

## Concurrency Model

All lifecycle mutations are guarded by one fair `ReentrantLock` in `ParkingLot`:

1. Check duplicate vehicle admission.
2. Remove a compatible spot from an availability pool.
3. Occupy it and create/index the ticket.

Exit similarly validates, prices, captures payment, transitions state, removes indexes, and releases the spot while holding the same lock. This coarse-grained design is intentionally easy to reason about and prevents double allocation or partial state changes in a single JVM.

Payment gateways are external calls in production and should not run while holding a JVM lock. A production evolution would use a `PAYMENT_PENDING` ticket state, an idempotency key, optimistic versioning, and a short finalization transaction. The sample keeps payment deterministic and local to demonstrate state integrity.

## Ticket and Payment State

```text
Ticket:  ACTIVE --successful payment--> PAID
Payment: PENDING --capture--> SUCCESS
                 \--failure----------> FAILED
```

An unsuccessful payment leaves the ticket active and spot occupied. Once paid, a ticket records exit time and cannot be settled again. Monetary values use `long` minor units.

## Time and Pricing

`Clock` is constructor-injected into the lot. Ticket entry, payment, and exit timestamps all derive from it. `HourlyFeeStrategy` computes elapsed duration, rounds any partial hour upward, and applies a one-hour minimum. `FlatFeeStrategy` demonstrates an interchangeable policy.

## Gates and Display

Entry and exit gates deliberately contain no allocation or pricing rules:

- `EntryGate.enter(vehicle)` delegates to `ParkingLot.park`.
- `ExitGate.exit(ticketId)` delegates to `ParkingLot.payAndExit`.

Availability is exposed as an immutable snapshot. A physical `DisplayBoard` can render that projection. In a larger system, spot events can update boards through an observer or event bus.

## Validation and Errors

The implementation rejects blank identifiers, null inputs, duplicate floor/spot IDs, incompatible occupancy, duplicate active registrations, missing or inactive tickets, invalid state transitions, and negative monetary values. `ParkingException` carries an `ErrorCode` so callers can distinguish validation, capacity, conflict, and not-found failures without parsing messages.

## Production Evolution

- Persist spots, tickets, and payments in a transactional database.
- Use row locks, compare-and-set versions, or Redis atomic operations for distributed allocation.
- Separate payment initiation from exit finalization and reconcile asynchronous outcomes.
- Publish occupancy events for displays, analytics, and notifications.
- Add reservations with expiration and allocation holds.
- Partition by facility/floor and expose idempotent gate APIs.
