# Design

## Beginner Design Map

One object owns the car because door, motion, floor, and stop changes must agree.
`TreeSet` exists to deduplicate stops and find the nearest stop on either side;
`Snapshot` exists to expose state without allowing callers to mutate it. A
normal tick closes an open door or, with closed doors, chooses direction, moves
one floor, and serves an arrival.

This sample chooses deterministic LOOK scheduling over perfect waiting-time
optimization. One serialized event loop avoids races. The safety trade-off is
clarity over realism: production still needs sensors, brakes, redundancy, and a
building dispatcher.

## Aggregate Model
`Elevator` is the aggregate root and owns its floor bounds, current floor, pending stops, direction, motion, and door state. A `TreeSet` both deduplicates requests and provides the nearest higher or lower stop. `Snapshot` copies that set before exposing it.

## State Invariants
- `OPEN` doors imply `STOPPED` motion.
- The current floor and every pending stop are within the served range.
- A movement step changes the current floor by exactly one.
- `IDLE` is selected when no stop exists; an arrival is removed before doors open.

## Scheduling Policy
The implementation uses LOOK semantics: continue upward while a higher stop exists, or downward while a lower stop exists. When continuation is impossible, choose the only available side; if both sides exist, choose the nearest, breaking ties upward. This minimizes needless reversals while remaining deterministic.

## Control Loop
`step` is intentionally discrete. An open door is closed and scheduling is recalculated, but movement waits until the next call. With closed doors, a request at the current floor is served immediately. Otherwise, direction is selected, one floor is traversed, and arrival may atomically stop the car and open its doors.

## Safety and Concurrency
`openDoor` checks motion directly, while `step` ensures doors close before travel. The model is not thread-safe; a production controller should process commands through one event loop. Hardware integration must additionally require positive closed-door and brake sensor feedback rather than trusting software state alone.

See the [class diagram](UML/README.md), [request sequence](Sequence-Diagrams/README.md), and [implementation](implementation/java/Elevator.java).
