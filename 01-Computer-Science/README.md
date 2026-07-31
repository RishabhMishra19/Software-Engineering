# Computer Science

Computer science foundations explain what happens beneath application code: how software runs, communicates, stores data, and coordinates overlapping work. Think of an application as a business operating inside a building: the operating system supplies and schedules the rooms and equipment, networking carries messages to other buildings, concurrency coordinates people working at the same time, and databases preserve the records. These topics help readers reason about speed, correctness, and failures instead of treating a computer as a black box whose internal behavior is unknown.

No programming experience is required for this section. Each topic begins with an everyday mental model, then introduces the precise terms and internal mechanism. When a later topic depends on an earlier one, the guide explains or links that prerequisite.

## Topics

- [Operating Systems](Operating-System/README.md) — processes, memory, scheduling, files, and system calls.
- [Networking](Networking/README.md) — layered communication, addressing, transport, the Domain Name System (DNS), Hypertext Transfer Protocol (HTTP), and reliability.
- [Databases](Database/README.md) — data models, transactions, indexing, consistency, and scaling.
- [Concurrency](Concurrency/README.md) — coordination, synchronization, parallelism, and correctness.

## Suggested order

This order moves from one machine to communication between machines, then to coordination and durable data:

1. Operating Systems
2. Networking
3. Concurrency
4. Databases

The order is a recommendation, not a dependency rule. A reader interested in one practical problem can open that topic first and follow its prerequisite links.
