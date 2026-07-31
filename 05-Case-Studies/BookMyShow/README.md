# BookMyShow

> **Provenance:** Editorial addition; the matching source stub was empty.

## Problem Statement
Design a ticketing service that lets users discover a show, temporarily reserve seats, pay, and receive a booking without overselling inventory during concurrent demand.

## Functional Requirements
- View seat availability for a show.
- Atomically hold one or more seats for a bounded time.
- Create a pending booking and process idempotent payment callbacks.
- Confirm paid bookings; release seats after failed payments or expired holds.

Detailed scope and invariants are in [Requirements](Requirements.md).

## Non Functional Requirements
- No seat may be held or sold to two users at once.
- Availability and booking writes must be strongly consistent per show.
- Reads should remain low-latency under bursty traffic.
- Payment retries, process crashes, and delayed callbacks must be recoverable and observable.

## Design Decisions
Inventory is partitioned by show and seat. A transactional write serializes conflicting claims; a TTL hold protects checkout without permanently consuming inventory. Booking and payment have explicit state machines, and payment references provide idempotency. See [Design](Design.md) for persistence, scaling, and failure handling.

## Class Diagram
The model separates show inventory, temporary holds, bookings, and payment outcomes.

```mermaid
classDiagram
    ShowInventory "1" o-- "*" Hold
    ShowInventory "1" o-- "*" Booking
    Hold --> Booking : creates
    Booking --> PaymentResult : records
```

Full diagram: [UML](UML/README.md).

## Sequence Diagram
Checkout first acquires a hold, then creates a pending booking, and only converts seats to sold inventory after a successful payment.

```mermaid
sequenceDiagram
    User->>Inventory: hold(seats)
    Inventory-->>User: holdId + expiry
    User->>Booking: startBooking(holdId)
    Booking->>Payment: charge(paymentReference)
    Payment-->>Booking: succeeded
    Booking->>Inventory: confirm held seats
    Booking-->>User: confirmed booking
```

Detailed flows: [Sequence Diagrams](Sequence-Diagrams/README.md).

## Complexity
For `s` requested seats and `h` active holds, the compact in-memory reference has `O(s + h)` hold checks and `O(s)` confirmation. A production indexed seat table makes conflicting-seat acquisition approximately `O(s log n)` while storage is `O(n + b)` for seats and bookings.

## Future Improvements
- Add a transactional outbox for payment and notification events.
- Use a database lease plus fencing/version fields across service instances.
- Add waitlists, dynamic pricing, refunds, and partial cancellation.
- Cache read-only seat maps with version-based invalidation.

## Run the Reference Implementation
The Java 17 single-file implementation and self-tests are at [BookMyShow.java](implementation/java/BookMyShow.java).

```bash
javac implementation/java/BookMyShow.java
java -cp implementation/java BookMyShow
```
