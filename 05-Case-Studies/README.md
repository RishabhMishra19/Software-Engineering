# Case Studies

These case studies explain how a system works from a user's request through its
state changes and failure paths. Each one turns requirements into an
architecture or object model, then connects that design to diagrams and a
reference implementation where one exists.

Every study documents its assumptions, actors, requirements, key flows,
trade-offs, and limitations according to the
[case-study contract](../CONTRIBUTING.md#case-study-contract). It links to
canonical topic documentation instead of repeating it.

## Beginner reading path

Start with a study's overview to understand the real-world problem and normal
flow. Read Requirements next for actors, states, and rules; then Design for why
each service or class exists. Only after that, use the sequence diagram to
follow calls over time, the class diagram to see static relationships, and the
Java link to compare the explanation with a small executable model. Complexity,
race conditions, failures, and trade-offs make more sense after that flow.

## Available studies

- [BookMyShow](BookMyShow/)
- [Elevator](Elevator/)
- [Global Search](Global-Search/)
- [Parking Lot](Parking-Lot/)
- [Snake and Ladder](Snake-And-Ladder/)
- [Splitwise](Splitwise/)

Each directory begins with an overview. Its requirements page defines the
contract, its design page explains why the model exists, and its diagram pages
make relationships and sequences visible. Keep study-specific code and
diagrams within that directory. Put only genuinely reusable assets in the
shared [`Assets`](../Assets/) directory.
