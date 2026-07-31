# Snake and Ladder

> **Provenance:** Editorial addition; no source counterpart existed.

Snake and Ladder is a turn-based board game. Players start at square 0, roll a
die in a fixed order, and race to the final square. A ladder moves a player
forward, a snake moves a player backward, and either transition may lead to
another transition.

The client starts each turn. `Game` owns turn order, positions, status, and the
winner; `Board` owns squares and transitions; `Dice` supplies a roll; and
`Player` identifies a participant. The game is either `ACTIVE` or `WON`.

## Problem Statement
Model a turn-based race in which players move from square 0 to the final square, following snakes and ladders after each valid move.

## Normal Flow, Before the Diagrams

1. The active player rolls through the `Dice` interface.
2. An overshoot consumes the turn without moving.
3. A valid landing is passed to `Board`, which follows every snake or ladder in
   an acyclic chain.
4. `Game` commits the final position, then either records `WON` and the winner
   or advances to the next player.

The whole turn is one state transition: position and active player must not be
updated independently. The in-memory sample therefore expects serialized
calls; a remote version would use a game version to reject simultaneous turns.
There is no money in this domain.

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
`Game` owns the turn lifecycle because turn order and winning must change
atomically. `Board` validates and resolves movement so malformed transitions
cannot enter gameplay. `Dice` isolates randomness, which allows deterministic
tests. Positions are keyed by immutable players. An overshoot consumes the
turn without movement; a winning turn records the winner and does not advance
the active player.

See [design details](Design.md).

## Class Diagram
The complete Mermaid diagram is in [UML/README.md](UML/README.md).

## Sequence Diagram
The [turn sequence](Sequence-Diagrams/README.md) shows the flow step by step:
validate game state, roll, apply the overshoot rule, resolve transitions,
commit the position, and either record a winner or advance the turn.

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
