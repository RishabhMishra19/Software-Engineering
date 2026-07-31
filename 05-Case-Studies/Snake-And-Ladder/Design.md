# Design

## Boundaries
`Board` is an immutable rules object. It validates transition topology once and resolves a landing without mutating game state. `Game` is the aggregate root: it owns players, positions, active-player index, status, and winner. `Dice` is an injected port; `SequenceDice` is a deterministic adapter used by the executable self-test.

## Invariants
- A player has exactly one position in `[0, finalSquare]`.
- `ACTIVE` implies no winner; `WON` implies the winner is at the final square.
- The active index points to a registered player.
- Board transition traversal always terminates.

These are established at construction and changed only by `playTurn`.

## Turn Transaction
`playTurn` checks lifecycle state, obtains one roll, computes the raw landing, applies the overshoot policy, resolves transitions, and commits the resulting position. It then either records a winner or advances the active index. The returned immutable `TurnResult` is suitable for logging or presentation without exposing mutable internals.

## Failure Model
Configuration errors use `IllegalArgumentException`; lifecycle misuse uses `IllegalStateException`. Dice failures propagate because silently advancing or retrying would make a replay nondeterministic. The in-memory implementation assumes one caller at a time; a service wrapper should serialize commands or use aggregate versioning.

## Extension Points
Alternative dice, win policies, and turn policies can be introduced behind interfaces. Persistence should store board identity, ordered player IDs, positions, active index, status, winner, and a monotonically increasing version.

See the [class diagram](UML/README.md), [turn sequence](Sequence-Diagrams/README.md), and [implementation](implementation/java/SnakeAndLadder.java).
