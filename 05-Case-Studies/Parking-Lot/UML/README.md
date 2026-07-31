# Parking Lot Class Diagram

A driver does not interact with every class. Entry and exit gates delegate to
`ParkingLot`; the lot coordinates floors, spots, tickets, policies, and payment.
The first Mermaid diagram preserves the source teaching hierarchy. The second
shows the hardened value model and additional consistency boundaries. Compare
them as original versus corrected, not as two simultaneous implementations.

## Source-derived teaching design

This conceptual view preserves the source's explicit inheritance hierarchies
and the final class-diagram meaning:

```mermaid
classDiagram
    class ParkingLot {
      -String name
      -List~ParkingFloor~ floors
      -Map~String,Ticket~ activeTickets
      -ParkingSpotStrategy strategy
      +parkVehicle(Vehicle) Ticket
      +unparkVehicle(String) Ticket
    }
    class ParkingFloor {
      -String id
      -List~ParkingSpot~ spots
      -DisplayBoard displayBoard
      +getAvailableCount(SpotType) int
      +showAvailability()
    }
    class Vehicle {
      <<abstract>>
      -String number
      -VehicleType type
    }
    class Bike
    class Car
    class Truck
    class ParkingSpot {
      <<abstract>>
      -String id
      -SpotType type
      -Vehicle vehicle
      +park(Vehicle)
      +removeVehicle()
    }
    class BikeSpot
    class CarSpot
    class TruckSpot
    class DisplayBoard {
      +showAvailability(ParkingFloor)
    }
    class Ticket {
      -String id
      -Vehicle vehicle
      -ParkingSpot spot
      -LocalDateTime entryTime
      -TicketStatus status
    }
    class Payment
    class EntryGate
    class ExitGate
    class ParkingSpotStrategy {
      <<interface>>
      +findSpot(Vehicle, List) ParkingSpot
    }
    class NearestSpotStrategy
    class FeeStrategy {
      <<interface>>
      +calculateFee(Ticket) double
    }
    class HourlyFeeStrategy
    class FlatFeeStrategy

    ParkingLot "1" *-- "1..*" ParkingFloor
    ParkingFloor "1" *-- "0..*" ParkingSpot
    ParkingFloor "1" *-- "1" DisplayBoard
    Vehicle <|-- Bike
    Vehicle <|-- Car
    Vehicle <|-- Truck
    ParkingSpot <|-- BikeSpot
    ParkingSpot <|-- CarSpot
    ParkingSpot <|-- TruckSpot
    ParkingSpot "0..1" --> Vehicle
    Ticket --> Vehicle
    Ticket --> ParkingSpot
    ParkingLot --> ParkingSpotStrategy
    ParkingSpotStrategy <|.. NearestSpotStrategy
    EntryGate --> ParkingLot
    ExitGate --> ParkingLot
    ExitGate --> FeeStrategy
    FeeStrategy <|.. HourlyFeeStrategy
    FeeStrategy <|.. FlatFeeStrategy
    ExitGate --> Payment
```

The source diagram means that the lot coordinates floors, active tickets, and
allocation policy; floors contain spots and their display; tickets bind
vehicles to spots; gates delegate entry/exit; and two independent Strategy
families isolate allocation and pricing. The source class called
`NearestSpotStrategy` actually returns the first matching spot in floor/list
order, not a distance-based nearest spot.

## Editorial hardened design / Professional corrections

The next Mermaid class diagram introduces the corrected implementation. It
flattens behavior-free subtypes, makes payment and compatibility injectable,
and puts availability pools and lifecycle updates behind `ParkingLot`.

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

The hardened diagram flattens behavior-free bike/car/truck subclasses into
typed values, adds explicit compatibility and payment processing, uses
constant-time floor/type pools, and centralizes lifecycle invariants in the
lot. `Clock`, locking, typed errors, and immutable availability snapshots are
implementation concerns described in [Design.md](../Design.md); the strategy
interfaces still isolate the two behaviors expected to change.

[Back to the case study](../README.md)
