# BookMyShow Class Model

```mermaid
classDiagram
    class ShowInventory {
        -Set~String~ allSeats
        -Map~String, Hold~ holds
        -Map~String, Booking~ bookings
        -Set~String~ bookedSeats
        +hold(userId, seats) Hold
        +startBooking(holdId, userId) Booking
        +recordPayment(bookingId, reference, result) Booking
        +availableSeats() Set
    }
    class Hold {
        +String id
        +String userId
        +Set~String~ seats
        +Instant expiresAt
    }
    class Booking {
        +String id
        +String holdId
        +String userId
        +Set~String~ seats
        +BookingStatus status
        +String paymentReference
    }
    class BookingStatus {
        <<enumeration>>
        PENDING_PAYMENT
        CONFIRMED
        PAYMENT_FAILED
        EXPIRED
    }
    class PaymentResult {
        <<enumeration>>
        SUCCEEDED
        FAILED
    }

    ShowInventory "1" o-- "0..*" Hold
    ShowInventory "1" o-- "0..*" Booking
    Booking --> Hold : created from
    Booking --> BookingStatus
    ShowInventory ..> PaymentResult
```

`ShowInventory` is the aggregate and synchronization boundary in the reference implementation. Records are immutable snapshots; mutations replace a booking after validating its legal transition.

Back to the [overview](../README.md), [design](../Design.md), or [source](../implementation/java/BookMyShow.java).
