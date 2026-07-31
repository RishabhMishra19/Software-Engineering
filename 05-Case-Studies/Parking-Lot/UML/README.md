# Parking Lot Class Diagram

```mermaid
classDiagram
    class ParkingLot {
      -Clock clock
      -ParkingSpotStrategy allocationStrategy
      -FeeStrategy feeStrategy
      -Map floorsById
      -Map activeTicketsById
      -Map activeTicketByVehicle
      +park(Vehicle) Ticket
      +payAndExit(String) Payment
      +availability() Map
    }
    class ParkingFloor {
      -String id
      -Map spotsById
      -EnumMap availableByType
      +addSpot(ParkingSpot)
      +claim(SpotType) ParkingSpot
      +release(ParkingSpot)
      +availableCount(SpotType) int
    }
    class ParkingSpot {
      -String id
      -SpotType type
      -Vehicle vehicle
      +occupy(Vehicle)
      +release()
    }
    class Vehicle {
      +String registration
      +VehicleType type
    }
    class Ticket {
      +String id
      +Vehicle vehicle
      +ParkingSpot spot
      +Instant entryTime
      +TicketStatus status
      +markPaid(Instant)
    }
    class Payment {
      +String id
      +String ticketId
      +long amountMinor
      +PaymentStatus status
    }
    class EntryGate {
      +String id
      +enter(Vehicle) Ticket
    }
    class ExitGate {
      +String id
      +exit(String) Payment
    }
    class ParkingSpotStrategy {
      <<interface>>
      +allocate(List, Vehicle, SpotCompatibility) Allocation
    }
    class FeeStrategy {
      <<interface>>
      +calculate(Ticket, Instant) long
    }
    class PaymentProcessor {
      <<interface>>
      +capture(Payment) boolean
    }

    ParkingLot "1" *-- "1..*" ParkingFloor
    ParkingFloor "1" *-- "0..*" ParkingSpot
    ParkingSpot "0..1" --> Vehicle
    Ticket --> Vehicle
    Ticket --> ParkingSpot
    Payment --> Ticket
    EntryGate --> ParkingLot
    ExitGate --> ParkingLot
    ParkingLot --> ParkingSpotStrategy
    ParkingLot --> FeeStrategy
    ParkingLot --> PaymentProcessor
```

The strategy interfaces isolate behaviors expected to change. Floors own spot collections and constant-time availability pools, while the lot owns cross-floor invariants and ticket/payment coordination.

[Back to the case study](../README.md)
