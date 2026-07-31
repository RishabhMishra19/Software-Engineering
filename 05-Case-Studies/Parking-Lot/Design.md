# Parking Lot Design

> **Provenance.** The teaching track below is derived from
> `/tmp/software-engineering-sources/Learning-LLD/04-case-studies/parking-lot.md`.
> The professional track documents the corrections embodied by the runnable
> Java 17 implementation in
> [`implementation/java/ParkingLot.java`](implementation/java/ParkingLot.java).
> Both are retained because the source explains how the model grows, while the
> implementation demonstrates a safer final design.

## Beginner Design Map

The real-world problem is coordinating physical capacity, admission records,
and payment while several gates work at once. A floor groups spots, a strategy
chooses among them, a ticket records one stay, a fee strategy computes exact
money, and gates translate external requests while `ParkingLot` keeps the whole
lifecycle consistent.

Read the source-derived track as the interview model being built step by step.
Then read the hardened track as explicit corrections for races, time, money,
failures, and validation. The normal path is claim → occupy → ticket → price →
pay → release; the critical failure rule is that unsuccessful payment must not
release a physical space.

## Design Tracks

### Source-derived teaching design

The source begins with a deliberately small, interview-oriented model and adds
responsibilities one at a time. Its scope is:

- multiple floors, entry gates, and exit gates;
- bike, car, and truck vehicle and spot types;
- finding and occupying a spot, generating a ticket, calculating a fee,
  accepting payment, releasing the spot, and displaying availability;
- easy extension, pluggable parking strategies, and an `O(1)` availability
  lookup goal.

The source's functional shorthand is: park/remove a vehicle, generate a
ticket, calculate a fee, find a spot, and display availability. Its initial
object graph is:

```text
ParkingLot
├── ParkingFloor ── ParkingSpot ── Vehicle
├── EntryGate ── Ticket
└── ExitGate ── Payment
```

`ParkingLot` is the root object; floors partition thousands of spots into
real-world units; a floor owns spots and a display board; entry and exit gates
are the boundaries through which drivers use the lot.

### Editorial hardened design / Professional corrections

The runnable design preserves that domain but strengthens its contracts:

- one fair lot-level lock makes allocation and exit atomic in a single JVM;
- per-floor availability pools meet the `O(1)` count/update objective;
- compatibility is an explicit policy rather than an enum-name conversion;
- injected `Clock`, integer minor units, typed errors, and payment failure
  states make behavior deterministic and testable;
- lifecycle methods validate duplicate admission, ticket state, spot
  ownership, and pool membership.

These are corrections, not a replacement for the source's teaching model.
The rest of this document presents the source design first, then states where
the professional version intentionally differs.

## Source-derived Teaching Design

### Enums and object hierarchies

The source introduces four enums:

```java
enum VehicleType { BIKE, CAR, TRUCK }
enum SpotType { BIKE, CAR, TRUCK }
enum TicketStatus { ACTIVE, PAID }
enum PaymentStatus { PENDING, SUCCESS, FAILED }
```

Its vehicle model is an abstract `Vehicle(number, type)` with immutable number
and type, specialized by `Bike`, `Car`, and `Truck`. Each subtype's constructor
fixes its enum value:

```java
final class Car extends Vehicle {
    Car(String number) {
        super(number, VehicleType.CAR);
    }
}
```

Its spot model mirrors that hierarchy. Abstract
`ParkingSpot(id, type)` stores either one vehicle or `null`;
`isAvailable()` tests that state, `park(vehicle)` assigns it, and
`removeVehicle()` clears it. `BikeSpot`, `CarSpot`, and `TruckSpot` fix their
respective `SpotType`.

This symmetry makes the initial compatibility rule easy to teach:

```text
Bike  → BikeSpot
Car   → CarSpot
Truck → TruckSpot
```

The source's `Ticket` generates a UUID, retains the vehicle and spot, captures
`LocalDateTime.now()` at entry, begins `ACTIVE`, and can be marked `PAID`.
Its `Payment` generates a UUID, records a `double` amount, begins `PENDING`,
and can be marked `SUCCESS`; the `FAILED` enum exists for an extension even
though the source's sample exit always succeeds.

### ParkingFloor and DisplayBoard

`ParkingFloor` exists so the lot does not directly manage one unstructured
collection of thousands of spots. In the source it owns:

- an ID;
- a list of `ParkingSpot` instances;
- one `DisplayBoard`;
- `addSpot`, `getSpots`, `getAvailableCount(type)`, and
  `showAvailability`.

The teaching implementation computes a count by scanning the floor's list:

```java
int getAvailableCount(SpotType type) {
    int count = 0;
    for (ParkingSpot spot : spots) {
        if (spot.getType() == type && spot.isAvailable()) {
            count++;
        }
    }
    return count;
}
```

`DisplayBoard.showAvailability(floor)` prints the floor ID and available
`BIKE`, `CAR`, and `TRUCK` counts by calling that method. Keeping rendering out
of `ParkingLot` demonstrates the Single Responsibility Principle. A real board
might show labels such as “Available Bike Spots”, “Available Car Spots”, and
“Available Truck Spots.”

### ParkingLot responsibilities and first snapshot

Before strategies are introduced, the source `ParkingLot` owns its name,
ordered floors, and a map of active tickets keyed by vehicle number. It adds
and exposes floors, adds/looks up/removes active tickets, and asks each floor
to show availability.

```text
ParkingLot
├── ParkingFloor
│   ├── ParkingSpot
│   └── DisplayBoard
└── Active Tickets
```

This is the first **Current Design** snapshot: facility structure, live spot
state, display, and ticket registry are present, but allocation and pricing
policies have not yet been extracted.

### Parking strategy and second snapshot

Allocation rules vary: first available, nearest spot, nearest gate, lowest
floor, VIP first, or occupancy balancing. Hardcoding vehicle-type branches or
selection rules inside `ParkingLot` would require modifying the aggregate for
every new policy. The source therefore adds:

```java
interface ParkingSpotStrategy {
    ParkingSpot findSpot(Vehicle vehicle, List<ParkingFloor> floors);
}
```

The source class named `NearestSpotStrategy` is specifically a **first
compatible spot in iteration order** strategy; it does not calculate physical
distance. It converts `VehicleType` to the same-named `SpotType`, scans floors
and then spots, and returns `null` when no match exists:

```java
SpotType requiredType = SpotType.valueOf(vehicle.getType().name());
for (ParkingFloor floor : floors) {
    for (ParkingSpot spot : floor.getSpots()) {
        if (spot.isAvailable() && spot.getType() == requiredType) {
            return spot;
        }
    }
}
return null;
```

`ParkingLot` receives this strategy in its constructor. Its source
`parkVehicle` asks the strategy for a spot, throws `"No Spot Available"` on
`null`, occupies the spot, creates a ticket, indexes it, and returns it.
`unparkVehicle(vehicleNumber)` looks up the active ticket, throws
`"Invalid Ticket"` when absent, clears its spot, removes the index, and
returns the ticket.

```text
ParkingLot
├── ParkingFloor ── ParkingSpot
├── ParkingSpotStrategy ── NearestSpotStrategy
└── Active Tickets
```

This second **Current Design** snapshot adds a replaceable behavior seam.
`VIPSpotStrategy`, a true `NearestGateStrategy`, or
`LowestFloorStrategy` can be injected without changing `ParkingLot`. That is
the Strategy pattern and the Open/Closed Principle.

The entry flow at this stage is:

```text
Vehicle arrives → ParkingLot → ParkingSpotStrategy
                → find and occupy spot → generate ACTIVE ticket
```

### Fee strategies, gates, payment, and third snapshot

Pricing can likewise be hourly, flat, weekend, peak-hour, or VIP-specific, so
it should not be a chain of conditions inside `ExitGate`:

```java
interface FeeStrategy {
    double calculateFee(Ticket ticket);
}
```

The source includes two concrete policies:

- `HourlyFeeStrategy` uses `Duration.between(entryTime,
  LocalDateTime.now()).toHours()`, applies a one-hour minimum, and charges
  `hours × 20.0`. Because `toHours()` truncates, a 90-minute stay is billed as
  one hour in the source.
- `FlatFeeStrategy` always returns `100.0`.

An `EntryGate` accepts a vehicle, delegates to
`parkingLot.parkVehicle(vehicle)`, and returns the ticket. Its responsibilities
are accepting the vehicle, allocating a spot, and generating the ticket, but
the business operation remains delegated.

The source `ExitGate` holds a `ParkingLot` and a `FeeStrategy`. It:

1. looks up a ticket by vehicle number and rejects a missing ticket;
2. calculates the fee;
3. creates a `PENDING` payment and immediately marks it `SUCCESS`;
4. marks the ticket `PAID`;
5. calls `unparkVehicle` to release the spot and remove the active index;
6. returns the payment.

```text
ParkingLot
├── ParkingFloor
├── ParkingSpotStrategy
└── Active Tickets

EntryGate → ParkingLot
ExitGate  ├── FeeStrategy
          └── Payment
```

This third **Current Design** snapshot completes the source's gate, pricing,
payment, and release story. The two end-to-end flows are:

```text
Vehicle → EntryGate → ParkingLot → ParkingSpotStrategy
        → ParkingSpot → Ticket generated

Vehicle exit → ExitGate → FeeStrategy → Payment → spot released
```

### Setup and main walkthrough

The source's `Main` composes the object graph rather than hiding it behind a
framework:

1. create `NearestSpotStrategy`;
2. create `"Phoenix Mall"` with that strategy;
3. create floor `"F1"` with spots `"B1"`, `"C1"`, and `"T1"`;
4. add the floor to the lot;
5. create an entry gate and an exit gate using `HourlyFeeStrategy`;
6. create car `"KA01AB1234"` and enter it;
7. print “Ticket Generated” and display reduced availability;
8. exit by vehicle number, print the paid amount, and display restored
   availability.

The runnable Java retains this walkthrough's intent but uses the corrected
APIs and adds exhaustion, duplicate admission, billing, repeat-exit, and
concurrency checks.

### Meaning of the final source class diagram

The final source diagram communicates four relationships:

1. `ParkingLot` composes floors, selects through
   `ParkingSpotStrategy`, and owns active tickets.
2. `ParkingFloor` composes abstract `ParkingSpot`; bike/car/truck spot
   subclasses specialize it.
3. Abstract `Vehicle` is specialized by bike/car/truck; a `Ticket` binds a
   vehicle to its occupied spot.
4. `EntryGate` delegates to the lot; `ExitGate` uses a `FeeStrategy`, with
   hourly and flat implementations, and produces a `Payment`.

See [the diagram README](UML/README.md) for both the source-derived conceptual
diagram and the hardened implementation diagram.

### Patterns and design discussion

- **Strategy:** both allocation (`ParkingSpotStrategy`) and pricing
  (`FeeStrategy`) are changing behaviors. New policies are injected instead
  of adding conditionals to `ParkingLot` or `ExitGate`.
- **Single Responsibility:** floors group spots, boards render availability,
  gates adapt requests, pricing policies price, and the lot coordinates.
- **Open/Closed Principle:** VIP, nearest-gate, lowest-floor, weekend, and
  peak-hour policies extend behavior through new strategy classes.
- **Factory (possible follow-up):** `VehicleFactory` and `SpotFactory` could
  centralize subtype construction; the source does not require them yet.
- **Observer (possible follow-up):** spot occupied/released events could update
  display boards automatically. The source updates/displays manually.

### Interview follow-ups

- Multiple entry and exit gates already work by constructing multiple gate
  objects against the same lot.
- EV support adds `EV`/`EVSpot` (plus enum and compatibility updates in the
  hardened model).
- VIP parking injects `VIPSpotStrategy`.
- Dynamic pricing adds `WeekendFeeStrategy` or `PeakHourFeeStrategy`.
- Reservations add a `Reservation` entity and, in a complete design, a hold
  state/expiry rule; allocation remains the policy seam.

### Scalability note

The source design is appropriate for one parking lot, one process, and an LLD
interview. A production system may add a transactional database, Redis or
another atomic coordination mechanism, payment gateway, notification service,
and service boundaries. Microservices are an optional deployment evolution,
not an LLD requirement.

### Critical learnings

- Start with domain modeling.
- Make spot allocation and pricing pluggable.
- Keep changing rules in strategies and gates free of business-policy
  conditionals.
- Use floors to reflect the physical domain and contain spots.
- Treat `ParkingLot` as the coordinating root aggregate.
- Design for extension rather than repeated modification.

### Fast revision

```text
ENTRY: Vehicle → EntryGate → ParkingStrategy → Spot → Ticket
EXIT:  Vehicle → ExitGate  → FeeStrategy     → Payment → release

Changing allocation → Strategy pattern
Changing pricing    → Strategy pattern
ParkingLot          → Root aggregate
ParkingFloor        → Contains spots
Ticket              → Entry/admission record
Payment             → Exit/settlement record
```

## Editorial Hardened Design / Professional Corrections

### Value model instead of subtype boilerplate

The runnable implementation uses immutable `Vehicle(registration, type)` and a
concrete `ParkingSpot(id, type)` rather than six trivial subclasses. The
source hierarchy remains a valid teaching example; the value model carries
the same distinctions with less code. Real subtype-specific behavior would
justify restoring subclasses.

### Availability pools and complexity

The source promises `O(1)` availability lookup but its
`getAvailableCount` and `NearestSpotStrategy` scan lists. The hardened
`ParkingFloor` keeps an `EnumMap<SpotType, ArrayDeque<ParkingSpot>>`:

- count and claim/return for a known floor/type are `O(1)`;
- lowest-floor-first allocation is `O(F)` without scanning every spot;
- full availability snapshot is `O(F × T)`;
- release and expected active-ticket lookup are `O(1)`.

The implementation calls the strategy `LowestFloorFirstStrategy`, accurately
describing insertion-order floor selection. A genuinely “nearest” policy
needs gate/location metadata and a distance metric.

### Explicit compatibility

`SpotType.valueOf(vehicle.getType().name())` silently couples two enums and
only permits exact names. `SpotCompatibility` makes the rule explicit and
injectable. `EXACT_TYPE` preserves source behavior, while another policy can
allow bikes in larger spots or account for compact, accessible, EV, and
oversized constraints.

### Locking and lifecycle integrity

Multiple source gate instances share mutable lists/maps but do not prevent two
threads from choosing the same spot. One fair `ReentrantLock` in `ParkingLot`
guards admission, pool mutation, occupancy, ticket indexing, payment
completion, and release. This coarse lock is intentionally simple and
prevents double allocation and partial state in one JVM.

The maintained invariants are:

- every free spot appears exactly once in its matching pool;
- every occupied spot is absent from all availability pools;
- one registration has at most one active ticket;
- an active ticket points to its vehicle's occupied spot;
- only an active ticket can be paid;
- release occurs only after successful payment.

A real remote payment must not be performed while holding a JVM lock.
Production should introduce `PAYMENT_PENDING`, an idempotency key, optimistic
versioning, and short transactional finalization.

### Clock, money, billing, and payment

The source directly calls `LocalDateTime.now()`, stores `double`, and truncates
partial hours. The hardened design injects `Clock`, uses `Instant`, and stores
`long` minor units. It rounds every started hour upward with a one-hour
minimum: 90 minutes is two hours. These choices avoid flaky tests, timezone
ambiguity, floating-point money errors, and accidental underbilling.

`PaymentProcessor` makes success or failure injectable. A failed capture marks
the payment `FAILED` but leaves the ticket `ACTIVE` and the spot occupied.
Success transitions payment to `SUCCESS`, ticket to `PAID`, records exit time,
removes active indexes, and returns the spot to its floor.

### Gate API and display projection

The source exits by vehicle number; the hardened API exits by unique ticket ID
to avoid ambiguity and better match a presented ticket. Both gates remain thin:

- `EntryGate.enter(vehicle)` delegates to `ParkingLot.park`;
- `ExitGate.exit(ticketId)` delegates to `ParkingLot.payAndExit`.

Rather than embedding console output in the model, `availability()` returns an
immutable floor/type snapshot. A `DisplayBoard` can render that projection.
Observer/events can refresh physical boards asynchronously without coupling
them to allocation.

### Validation and typed errors

The source uses `null` and generic `RuntimeException` messages. The hardened
design rejects blank/null IDs, duplicate floors or spots, incompatible or
already occupied spots, duplicate active registrations, missing/inactive
tickets, illegal transitions, invalid pool returns, negative rates/fees, and
time reversal. `ParkingException` carries an `ErrorCode`, so clients can
handle capacity, conflict, validation, payment, and not-found failures without
parsing text.

## Production Evolution

- Persist spots, tickets, and payments transactionally.
- Use row locks, compare-and-set versions, or Redis atomic operations for
  distributed allocation.
- Separate payment initiation from exit finalization and reconcile
  asynchronous outcomes.
- Publish occupancy events for displays, analytics, and notifications.
- Add reservations with expiring holds and explicit allocation interaction.
- Partition by facility/floor and expose idempotent gate APIs.

[Back to the case study](README.md)
