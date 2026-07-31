# Snake and Ladder

> **Provenance:** Editorial addition; no source counterpart existed.

## Problem Statement
Model a turn-based race in which players move from square 0 to the final square, following snakes and ladders after each valid move.

## Functional Requirements
- Support two or more uniquely identified players in fixed turn order.
- Roll an injected die, move only when the roll does not overshoot, and resolve chained board transitions.
- Finish only on an exact landing and reject turns after a winner exists.
- Reject invalid boards, transition cycles, players, and dice values.

See [detailed requirements](Requirements.md).

## Non Functional Requirements
- Deterministic tests and reproducible games through dependency-injected dice.
- Immutable board configuration and explicit game invariants.
- Constant-time normal turns, excluding transition-chain length.

## Design Decisions
`Game` owns the turn lifecycle, `Board` validates and resolves movement, and `Dice` isolates randomness. Positions are keyed by immutable players. Overshoot consumes the turn without movement; a winning turn does not advance the active player.

See [design details](Design.md).

## Class Diagram
The complete Mermaid diagram is in [UML/README.md](UML/README.md).

## Sequence Diagram
The normal and winning turn flow is in [Sequence-Diagrams/README.md](Sequence-Diagrams/README.md).

## Complexity
- Board construction: `O(T²)` worst case for validating `T` transition starts independently.
- Turn: `O(C)`, where `C` is the resolved transition-chain length; normally `O(1)`.
- Space: `O(P + T)` for `P` players and `T` transitions.

## Future Improvements
- Add cryptographically seeded random dice and replay logs.
- Support configurable overshoot and extra-turn rules.
- Persist game snapshots with optimistic concurrency for remote play.

## Run
Compile and execute the [Java 17 implementation](implementation/java/SnakeAndLadder.java):

```bash
javac implementation/java/SnakeAndLadder.java
java -cp implementation/java SnakeAndLadder
```
