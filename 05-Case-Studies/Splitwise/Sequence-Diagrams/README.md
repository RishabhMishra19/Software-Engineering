# Splitwise Sequence Diagrams

## Idempotent Expense Creation

```mermaid
sequenceDiagram
    actor Member
    participant API
    participant Ledger
    participant Database
    Member->>API: addExpense(expenseId, payer, total, split)
    API->>Ledger: normalize and allocate shares
    Ledger->>Database: begin group transaction
    Database-->>Ledger: existing expense or empty
    alt identical expense exists
        Ledger-->>API: stored expense
    else ID has different payload
        Ledger-->>API: conflict
    else new expense
        Ledger->>Ledger: verify zero-sum postings
        Ledger->>Database: insert expense + postings + balances
        Database-->>Ledger: committed group version
        Ledger-->>API: created expense
    end
    API-->>Member: deterministic result
```

## Balance Simplification

```mermaid
sequenceDiagram
    actor Member
    participant API
    participant Ledger
    Member->>API: simplify(groupId)
    API->>Ledger: load balances with version
    Ledger->>Ledger: partition debtors and creditors
    loop Until one side is empty
        Ledger->>Ledger: match min(debt, credit)
    end
    Ledger-->>API: settlements + source version
    API-->>Member: advisory transfer plan
```

The proposal does not mutate balances. A later payment is recorded as a new journal entry, and stale proposals are rejected or recalculated when their source version no longer matches.

Back to the [overview](../README.md), [requirements](../Requirements.md), or [design](../Design.md).
