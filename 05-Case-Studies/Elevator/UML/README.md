# Class Diagram

A request is stored in `Elevator`, `step()` changes the car by at most one floor,
and `Snapshot` reports the result. `Elevator` exists as the safety boundary;
`Snapshot` prevents outside mutation; the three enums model independent
direction, movement, and door states.

The following Mermaid class diagram shows structure, not timing. Read `..>` as
“creates or uses” and `-->` as “holds or refers to.”

```mermaid
classDiagram
    class Elevator {
        -minimumFloor int
        -maximumFloor int
        -currentFloor int
        -pendingStops NavigableSet
        -direction Direction
        -motion MotionState
        -door DoorState
        +requestStop(floor)
        +step() Snapshot
        +openDoor()
        +closeDoor()
        +snapshot() Snapshot
    }
    class Snapshot {
        +floor int
        +direction Direction
        +motion MotionState
        +door DoorState
        +pendingStops NavigableSet
    }
    class Direction {
        <<enumeration>>
        UP
        DOWN
        IDLE
    }
    class MotionState {
        <<enumeration>>
        MOVING
        STOPPED
    }
    class DoorState {
        <<enumeration>>
        OPEN
        CLOSED
    }

    Elevator ..> Snapshot : creates
    Elevator --> Direction
    Elevator --> MotionState
    Elevator --> DoorState
    Snapshot --> Direction
    Snapshot --> MotionState
    Snapshot --> DoorState
```

See the [design](../Design.md), [sequence diagram](../Sequence-Diagrams/README.md), and [overview](../README.md).
