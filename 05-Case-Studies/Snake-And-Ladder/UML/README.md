# Class Diagram

During one turn, `Game` asks `Dice` for a roll, asks `Board` to resolve the
landing, updates one `Player` position, and returns `TurnResult`. Each class
exists to isolate randomness, immutable board rules, participant identity, game
lifecycle, or read-only reporting.

The following Mermaid class diagram shows those static relationships. The
sequence diagram explains their runtime order.

```mermaid
classDiagram
    class Dice {
        <<interface>>
        +roll() int
    }
    class SequenceDice {
        -rolls int[]
        -index int
        +roll() int
    }
    class Board {
        -lastSquare int
        -transitions Map
        +resolve(square) int
    }
    class Player {
        +id String
        +name String
    }
    class Game {
        -players List
        -positions Map
        -status Status
        +playTurn() TurnResult
        +positionOf(player) int
    }
    class TurnResult {
        +roll int
        +from int
        +landedOn int
        +finalPosition int
        +moved boolean
        +status Status
    }

    Dice <|.. SequenceDice
    Game --> Dice : rolls
    Game --> Board : applies rules
    Game "1" o-- "2..*" Player
    Game ..> TurnResult : returns
```

See the [design](../Design.md), [sequence diagram](../Sequence-Diagrams/README.md), and [overview](../README.md).
