# BookMyShow Requirements

## Scope
The bounded context begins after shows and seat layouts exist. It owns per-show availability from hold through confirmation. Catalog search, venue onboarding, recommendations, pricing rules, refunds, and notifications are integration points rather than core implementation concerns.

## Actors and Use Cases
- **Customer:** inspect availability, hold a seat set, begin checkout, and receive a booking result.
- **Payment provider:** submit a success or failure callback identified by a globally unique payment reference.
- **Expiry worker:** release elapsed holds and expire their unpaid bookings.
- **Operator:** inspect state transitions and reconcile payment/booking mismatches.

## State and Invariants
Each seat is exactly one of available, actively held, or booked for a show. A hold has one owner and an exclusive expiry instant. A booking progresses from `PENDING_PAYMENT` to one terminal state: `CONFIRMED`, `PAYMENT_FAILED`, or `EXPIRED`.

1. A transaction may create a hold only when every requested seat is available.
2. Partial holds are forbidden: all requested seats are acquired or none are.
3. Only the hold owner can create its booking.
4. A successful callback confirms only a live hold.
5. Replaying the same payment reference returns the original result and performs no additional transition.
6. A payment reference cannot belong to multiple bookings.
7. Confirmed seats are never released by hold cleanup.

## Consistency and Failure Semantics
The seat-claim path requires serializable behavior for rows belonging to the same show; stale availability reads are acceptable only before checkout. Client timeouts are treated as unknown outcomes and retried with operation identifiers. Payment success received after expiry enters reconciliation rather than silently confirming seats that may have been sold.

Hold expiry is enforced both lazily on inventory access and proactively by a worker. Expiry is based on server time. At-least-once event delivery is expected, so every consumer must be idempotent.

## Service-Level Objectives
- 99.9% monthly availability for availability and checkout APIs.
- p95 below 200 ms for reads and below 500 ms for hold creation, excluding payment-provider latency.
- Zero acknowledged double bookings.
- Booking and payment transitions retained as an immutable audit trail.

## Acceptance Scenarios
- Two simultaneous requests for the same seat produce one hold and one conflict.
- A multi-seat request with one unavailable seat acquires no seats.
- An unpaid hold becomes available immediately after its expiry is observed.
- A duplicated successful callback produces one confirmed booking.
- A failed payment releases its held seats.

Related documents: [overview](README.md), [design](Design.md), [sequence flows](Sequence-Diagrams/README.md), and [implementation](implementation/java/BookMyShow.java).
