# Elevator

> **Provenance:** Editorial addition; the matching source stub was empty.

An elevator controller turns floor requests into safe, ordered movement. In
this case study, one controller accepts requests for one car, chooses its next
direction, moves one floor at a time, and opens the doors only after stopping.

The main actor is the controller that submits requests and advances the
simulation. The main entity is `Elevator`. Its independent states are travel
direction (`UP`, `DOWN`, or `IDLE`), motion (`MOVING` or `STOPPED`), and door
position (`OPEN` or `CLOSED`). Keeping these states separate makes unsafe
combinations explicit.

## Problem Statement
Model one elevator car that accepts floor requests, schedules stops, moves deterministically, and never opens its doors while moving.

## Normal Flow, Before the Diagrams

1. The controller submits a valid floor request; duplicate stops collapse.
2. On each `step`, an open door closes without movement.
3. With closed doors, the scheduler chooses `UP` or `DOWN` and moves one floor.
4. On a requested floor, the car becomes `STOPPED`, removes that stop, and opens
   the door. With no requests it becomes `IDLE`.

A race would occur if two threads changed requests and motion at once, so the
sample assumes one serialized controller. A production controller uses one
event loop and physical sensor feedback. There is no money or distributed data
consistency here; consistency means every state combination obeys safety rules.

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
A sorted stop set supports a LOOK-style scheduler: the car continues toward
requests ahead before considering a reversal. This avoids needless direction
changes while keeping scheduling deterministic. The car models direction,
motion, and doors independently so illegal combinations are detectable.
`step` is the single transition boundary, which makes every state change
visible and produces no hidden timing behavior.

See [design details](Design.md).

## Class Diagram
The complete Mermaid model is in [UML/README.md](UML/README.md).

## Sequence Diagram
The [request-service sequence](Sequence-Diagrams/README.md) follows one request
step by step: validate it, close an open door if necessary, choose a direction,
move one floor, and open the door only after reaching a requested floor.

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
