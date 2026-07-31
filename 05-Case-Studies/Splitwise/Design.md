# Splitwise Design

## Beginner Design Map

The core problem is preserving an exact, explainable history while answering
balances quickly. `Money` prevents rounding surprises; `Split` turns user intent
into shares; `Expense` preserves the accepted fact; `ExpenseLedger` applies it
once and maintains a projection; `Settlement` derives advice without rewriting
history. These responsibilities keep money arithmetic, allocation policy,
audit, and read optimization separate.

The happy path normalizes one command, validates zero-sum postings, commits the
journal and projection together, and returns the stored result. Version checks
handle concurrent writers; immutable postings repair a damaged projection; and
greedy settlement trades global optimality for simple deterministic speed.

## Domain Model
`Money` enforces the ledger currency's scale and supplies closed arithmetic. A `Split` converts intent into immutable participant shares. `Expense` is the normalized journal fact. `ExpenseLedger` owns idempotency and projects journal entries into per-user net balances. `Settlement` is a derived recommendation, not persisted debt.

## Write Path
The API normalizes user IDs and decimal strings, then starts a transaction scoped to the group. It checks the unique expense ID. An identical normalized payload returns the stored result; a different payload returns conflict. Otherwise it validates allocation, inserts the expense and postings, updates the balance projection, increments the group version, and commits.

Persist postings as `(expense_id, user_id, signed_amount)` rows. Their sum is constrained in application logic and checked asynchronously for corruption. The balance table is a rebuildable projection; immutable postings are authoritative.

## Exact Money
The reference uses `BigDecimal` at scale two because it models a two-minor-unit currency. A production `Money` includes ISO currency and derives scale from policy. Construction rejects hidden rounding. Integer minor units are also valid when amounts fit a documented range; decimal storage is easier for currencies with different scales.

## Read and Scale Strategy
Groups are natural partitions. Route all writes for a group to one database shard and use optimistic versions to detect concurrent projection updates. Cache balance queries by group version. Expense history uses keyset pagination. Large groups can update posting rows synchronously and refresh aggregate projections asynchronously if the API exposes the accepted journal version.

## Simplification Trade-offs
Greedy matching separates creditors and debtors, then transfers the smaller outstanding amount until one side is exhausted. It is deterministic, preserves exact totals, runs in `O(u log u)`, and emits at most `u - 1` transfers. It does not optimize fees, social constraints, payment rails, or the globally minimum edge count under arbitrary restrictions. Those variants require min-cost flow or combinatorial optimization.

Simplification should run on a versioned balance snapshot. A proposal carries that version and is recalculated when new expenses arrive.

## Reliability and Security
Use a database unique constraint for idempotency, not an in-memory cache. Publish expense events through a transactional outbox. Rebuild projections from postings and compare their checksum regularly. Authorize group membership, retain an audit trail, rate-limit writes, and encrypt user metadata. Money values and IDs are safe to log; free-text notes and receipts may not be.

See the [requirements](Requirements.md), [UML](UML/README.md), [sequence diagrams](Sequence-Diagrams/README.md), and [Java reference](implementation/java/Splitwise.java).
