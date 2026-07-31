# Elevator

> **Provenance:** Editorial addition; the matching source stub was empty.

## Problem Statement
Model one elevator car that accepts floor requests, schedules stops, moves deterministically, and never opens its doors while moving.

## Functional Requirements
- Accept and deduplicate requests within the configured floor range.
- Continue in the current direction while requests remain ahead, then reverse or become idle.
- Move at most one floor per step and stop with doors open at requested floors.
- Close open doors in a separate step before any movement.

See [detailed requirements](Requirements.md).

## Non Functional Requirements
- Deterministic state transitions suitable for simulation and unit testing.
- Safety invariants enforced inside the aggregate.
- Read-only snapshots for monitoring without exposing scheduler state.

## Design Decisions
A sorted stop set supports a LOOK-style scheduler. The car models direction, motion, and doors independently so illegal combinations are detectable. `step` is the single transition boundary and produces no hidden timing behavior.

See [design details](Design.md).

## Class Diagram
The complete Mermaid model is in [UML/README.md](UML/README.md).

## Sequence Diagram
Request service and door sequencing are shown in [Sequence-Diagrams/README.md](Sequence-Diagrams/README.md).

## Complexity
- Add request: `O(log R)` for `R` pending stops.
- Scheduling step: `O(log R)`; movement is one floor.
- Space: `O(R)`.

## Future Improvements
- Coordinate multiple cars with hall-call direction and load-aware dispatch.
- Add obstruction, emergency, maintenance, and overload states.
- Persist events and expose estimated arrival times.

## Run
Compile and execute the [Java 17 implementation](implementation/java/Elevator.java):

```bash
javac implementation/java/Elevator.java
java -cp implementation/java Elevator
```
