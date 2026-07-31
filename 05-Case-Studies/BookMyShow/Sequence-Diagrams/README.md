# BookMyShow Sequence Diagrams

## Successful Checkout

These diagrams show time from top to bottom. In the normal flow, the customer
holds seats, creates a pending booking, and receives confirmation after a
provider callback. The second flow explains the race between customers and an
expiry worker; serialization and a conditional version check prevent both a
double booking and an old worker releasing newer state.

```mermaid
sequenceDiagram
    actor Customer
    participant API
    participant Inventory
    participant Booking
    participant Payment
    Customer->>API: hold(showId, seats)
    API->>Inventory: atomically claim seats
    Inventory-->>API: holdId, expiresAt
    API-->>Customer: hold details
    Customer->>API: checkout(holdId)
    API->>Booking: create pending booking
    Booking-->>API: bookingId
    API->>Payment: charge(bookingId as idempotency key)
    Payment-->>API: accepted
    Payment->>Booking: callback(reference, SUCCEEDED)
    Booking->>Inventory: confirm live hold
    Inventory-->>Booking: seats booked
    Booking-->>Customer: booking confirmed
```

## Concurrent Hold and Expiry

This second Mermaid sequence introduces the main race and cleanup edge case.
Customers A and B ask for the same seat while an expiry worker may later release
it. The inventory authority orders those writes, and the worker supplies the
hold version so it cannot erase a confirmed booking or a newer hold.

```mermaid
sequenceDiagram
    participant A as Customer A
    participant B as Customer B
    participant I as Inventory Authority
    participant W as Expiry Worker
    par competing requests
        A->>I: hold(A1)
    and
        B->>I: hold(A1)
    end
    I-->>A: hold created
    I-->>B: conflict
    Note over I: Hold reaches server-side expiry
    W->>I: conditional release(holdId, version)
    I-->>W: released
    B->>I: hold(A1)
    I-->>B: hold created
```

The authority serializes conflicting writes; the worker's conditional update cannot release a seat after confirmation or a newer lease.

Back to the [overview](../README.md), [requirements](../Requirements.md), or [design](../Design.md).
