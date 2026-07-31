# Requirements

## Scope
The model is an in-memory game engine. UI, networking, authentication, persistence, and matchmaking are outside scope.

## Rules and Acceptance Criteria
1. A board has a final square greater than one and zero or more directed transitions.
2. Transition endpoints must be on playable squares; starts cannot be square 0 or the final square.
3. Self-loops and multi-transition cycles are rejected. A landing may traverse an acyclic chain.
4. A game requires at least two unique players and starts each at square 0.
5. Exactly one active player may roll while the game is active.
6. A positive roll that would pass the final square leaves the player in place.
7. Otherwise, movement occurs first and board transitions are then resolved to completion.
8. Landing on the final square atomically records the winner and closes the game.
9. Every non-winning roll advances to the next player, including an overshoot.
10. Calls after completion and references to unknown players fail explicitly.

## Quality Attributes
- **Testability:** dice behavior is supplied through an interface; no clock or global randomness is used.
- **Correctness:** constructors establish invariants so gameplay does not operate on malformed state.
- **Observability:** each turn returns its actor, roll, source, raw landing, resolved position, movement flag, and status.
- **Maintainability:** rule ownership is separated between board topology and game lifecycle.

## Assumptions
Players do not leave or join an active game. A die implementation is responsible for its upper bound; the game defensively rejects non-positive rolls. Calls are serialized by the caller.

Return to the [overview](README.md) or inspect the [design](Design.md).
