# Requirements

## Scope
The model represents one elevator car and its local stop scheduler. A building-wide dispatcher, physical sensors, passengers, capacity, persistence, and real-time timing are outside scope.

## Behavior and Acceptance Criteria
1. Construction requires a valid inclusive floor range and an initial floor within it.
2. Any served floor may be requested; duplicate requests collapse into one pending stop.
3. Requests outside the served range are rejected.
4. A step closes an open door and performs no movement.
5. With closed doors, a step either serves the current floor, moves exactly one floor, or remains idle.
6. The scheduler continues in its direction while a pending stop exists ahead.
7. When none exists ahead, it selects the nearest side, preferring upward travel on equal distance.
8. Arrival removes the stop, changes motion to `STOPPED`, and opens the door.
9. Doors cannot be opened while motion is `MOVING`.
10. With no pending stops, direction is `IDLE`, motion is `STOPPED`, and the door retains its safe state.

## Quality Attributes
- **Safety:** movement is impossible with an open door through the public transition API.
- **Determinism:** no clocks, threads, randomness, or asynchronous callbacks affect scheduling.
- **Inspectability:** snapshots contain independent physical state and an immutable copy of pending stops.
- **Extensibility:** dispatching policy can later be separated without changing door safety rules.

## Assumptions
One controller serializes calls. Each step represents one control-loop tick, not a fixed real-world duration. Floor requests carry no passenger direction, priority, or authorization.

Return to the [overview](README.md) or inspect the [design](Design.md).
