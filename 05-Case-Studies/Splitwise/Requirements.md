# Splitwise Requirements

## Beginner Orientation

The system turns “Alice paid 10.00 for three people” into facts that can be
audited. A **member** submits an expense; the **ledger** stores it; a **payer**
receives credit; **participants** receive debits; and a **settlement** is only a
suggested transfer. Positive balance means the group owes that person, negative
means the person owes the group, and all balances together must equal zero.

Normally the ledger validates one exact monetary allocation, stores postings,
and updates balances atomically. The requirements then address awkward cases:
remainder cents, duplicate retries, conflicting IDs, and simultaneous writes.
Those are consistency rules, not optional optimizations.

## Scope
The system manages expenses and balances inside a group using one configured currency. Authentication, social discovery, bank transfer execution, foreign exchange, and receipt OCR are outside the core model.

## Commands and Queries
- Create an expense with a client-generated immutable ID, payer, positive total, and split specification.
- Retrieve the normalized expense, including the exact allocated shares.
- Retrieve current net balances for group members.
- Generate a settlement proposal from current balances.

Equal splits accept a unique non-empty participant set. Exact splits accept non-negative shares whose sum equals the total at the currency's scale. The payer must be a participant.

## Monetary Rules
Amounts are decimal values at the ledger currency's fixed minor-unit scale. Values requiring rounding are rejected at the boundary. Equal splitting operates in integer minor units: divide evenly, then allocate leftover units by stable ascending user ID. This rule makes allocation reproducible across retries and service instances.

## Ledger Invariants
For an expense, the payer receives a credit equal to the total and every participant receives a debit equal to their share. Therefore:

- The sum of each expense's postings is zero.
- The sum of all member net balances is always zero.
- Positive balances mean the group owes that member; negative balances mean that member owes the group.
- Applying the same expense ID and normalized payload twice changes state once.
- Reusing an expense ID with a different payload is a conflict.

## Concurrency and Audit
Commands for one group are serialized with an optimistic group version or database transaction. The expense ID has a unique constraint. The immutable journal and balance projection update atomically; failed transactions expose neither. Every command records actor, timestamp, request ID, and resulting version.

## Settlement Semantics
A settlement proposal is advisory and does not mutate balances. Recorded payments should be represented as ledger entries so history remains auditable. Greedy matching preserves all net positions but does not promise the global minimum number of transfers when additional constraints exist.

## Acceptance Scenarios
- Splitting `10.00` among three sorted users yields `3.34`, `3.33`, and `3.33`.
- Exact shares not totaling the expense are rejected.
- Retrying an identical expense returns the original expense.
- Retrying its ID with a changed amount returns a conflict.
- Every accepted sequence leaves aggregate net balance equal to `0.00`.

Related documents: [overview](README.md), [design](Design.md), [class model](UML/README.md), and [implementation](implementation/java/Splitwise.java).
