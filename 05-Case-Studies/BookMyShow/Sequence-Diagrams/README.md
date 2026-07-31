# BookMyShow Sequence Diagrams

## Successful Checkout

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
