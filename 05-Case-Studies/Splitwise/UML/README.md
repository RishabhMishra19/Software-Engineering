# Splitwise Class Model

Start with one expense: a `Split` allocates an exact `Money` total, an immutable
`Expense` stores the result, and `ExpenseLedger` updates balances and can derive
`Settlement` suggestions. `EqualSplit` and `ExactSplit` exist as replaceable
allocation policies; `Money` centralizes exact arithmetic; the ledger is the
atomic consistency boundary.

The following Mermaid class diagram shows those static relationships. It does
not imply that a settlement changes balances.

```mermaid
classDiagram
    class Money {
        +BigDecimal value
        +add(other) Money
        +subtract(other) Money
        +negate() Money
    }
    class Split {
        <<interface>>
        +allocate(total) Map
    }
    class EqualSplit {
        +List~String~ participants
        +allocate(total) Map
    }
    class ExactSplit {
        +Map~String, Money~ shares
        +allocate(total) Map
    }
    class Expense {
        +String id
        +String payerId
        +Money total
        +Map~String, Money~ shares
    }
    class ExpenseLedger {
        -Map~String, Expense~ expenses
        -Map~String, Money~ netBalances
        +addExpense(id, payer, total, split) Expense
        +balances() Map
        +simplify() List
    }
    class Settlement {
        +String debtorId
        +String creditorId
        +Money amount
    }

    Split <|.. EqualSplit
    Split <|.. ExactSplit
    Expense --> Money
    ExpenseLedger "1" o-- "0..*" Expense
    ExpenseLedger --> Money : tracks balances
    ExpenseLedger ..> Settlement : derives
```

Split policies are pure allocation strategies. The ledger validates and atomically applies their immutable result, keeping balance projection logic independent from how shares were requested.

Back to the [overview](../README.md), [design](../Design.md), or [source](../implementation/java/Splitwise.java).
