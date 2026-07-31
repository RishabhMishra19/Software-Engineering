# Database

## Overview

A database stores, validates, queries, and recovers application data. Database selection is a workload decision: data shape, relationships, consistency, query patterns, scale, operational capability, and ecosystem matter more than popularity.

Common models include relational tables, documents, key-value records, wide columns, and graphs. One system may use several models—polyglot persistence—when each has a clear ownership boundary.

## Why do we need it?

Databases provide durable state, controlled concurrent access, efficient retrieval, integrity rules, recovery, and replication. They centralize guarantees that would be difficult and unsafe to reproduce independently in every application process.

## How does it work?

### Selecting a database

Evaluate in this order:

1. Business correctness and durability requirements.
2. Natural data structure and relationships.
3. Read, write, join, aggregation, and search patterns.
4. Consistency, availability, and geographic requirements.
5. Expected size, throughput, and growth.
6. Backup, restore, failover, monitoring, and team expertise.

PostgreSQL is a strong default for relational data, complex SQL, constraints, transactions, JSONB, and extensions such as PostGIS. MySQL with InnoDB is a mature relational choice for general web and CRUD workloads. MongoDB fits aggregate-oriented documents and evolving structures when cross-document relationships are limited. Redis commonly serves caches, sessions, counters, and rate limits; Cassandra targets distributed, write-heavy access patterns.

### SQL and NoSQL

Relational databases use declared schemas, joins, constraints, and broadly standardized SQL. NoSQL is an umbrella for document, key-value, wide-column, and graph databases; guarantees and query capabilities vary by product. A flexible schema is not an absent schema: validation and migration remain necessary.

Choose SQL when relationships, transactions, ad hoc queries, and integrity dominate. Choose a specific NoSQL model when access patterns, distribution, or aggregate shape justify its narrower trade-offs. Neither category is inherently faster or more scalable.

### Transactions, ACID, and isolation

A transaction groups operations into one logical unit. A commit makes changes durable; a rollback discards them. ACID means:

- **Atomicity:** all operations commit or none do.
- **Consistency:** declared invariants remain valid.
- **Isolation:** concurrent transactions behave according to an isolation level.
- **Durability:** committed data survives expected failures.

Isolation levels trade concurrency for protection against anomalies such as dirty reads, non-repeatable reads, phantoms, and serialization conflicts. Keep transactions short, retry documented transient conflicts, and avoid remote calls inside them.

BASE—Basically Available, Soft state, Eventual consistency—describes designs that favor availability and convergence over immediate consistency. It is not synonymous with NoSQL.

### CAP theorem

During a network partition, a distributed system cannot guarantee both linearizable consistency and availability for every request. Partition tolerance is not a practical optional choice in distributed deployments; the operational decision during a partition is consistency versus availability. CAP does not describe normal-operation latency or replace transaction analysis.

### Data modeling

Normalization separates facts to reduce duplication and update anomalies. Denormalization deliberately duplicates or precomputes data to improve reads. Start normalized for transactional integrity, then denormalize measured bottlenecks with an explicit synchronization strategy.

Database constraints—primary keys, foreign keys, uniqueness, `NOT NULL`, and checks—provide the final integrity boundary. Application validation improves error reporting but cannot replace constraints under concurrent requests.

### Indexing

Indexes maintain ordered or specialized structures that avoid full scans. Index columns used selectively in filters, joins, and ordering; derive composite index order from actual query prefixes. Every index consumes storage and write work. Inspect execution plans and production-like statistics before adding or removing one.

Clustered storage terminology is product-specific: some engines organize table rows by a primary index, while PostgreSQL heaps are not continuously maintained in clustered order. Avoid treating clustered/non-clustered behavior as universal.

### Concurrency control

Optimistic locking checks a version or timestamp at update time and retries on conflict; it suits low-contention workloads. Pessimistic locking holds database locks before modification; it suits short, high-contention operations where conflicts are expensive. Consistent lock ordering and short transactions reduce deadlocks, but callers must still handle deadlock retries.

### Connections and pooling

Opening a connection involves networking, authentication, and session setup. A bounded pool reuses connections and protects database capacity. Too small a pool creates queueing; too large a pool increases contention and can overwhelm the database. Size it from database limits, instance count, query latency, workload, and measured saturation—not user count.

### Replication, partitioning, and sharding

Replication copies data to improve availability, disaster recovery options, and read capacity. Synchronous replication reduces data-loss exposure at the cost of write latency; asynchronous replication lowers latency but permits lag and stale reads. Replication is not a backup because it also propagates accidental changes.

Table partitioning divides data within one logical database for pruning and maintenance. Sharding distributes data across database servers for horizontal storage or write scaling. Sharding adds routing, hotspots, rebalancing, cross-shard query, and distributed-transaction complexity. Scale vertically, tune queries, index, cache, partition, and add read replicas before sharding unless hard requirements dictate otherwise.

### Trade-offs

- Stronger consistency simplifies business logic but may reduce availability or increase latency.
- Flexible models speed some schema changes but move relationship enforcement into applications.
- Normalization improves integrity; denormalization improves selected reads.
- Indexes accelerate reads while increasing write and storage costs.
- Replicas scale reads; sharding can scale writes but sharply increases operations complexity.

## Advantages

- Durable, queryable state with recovery mechanisms.
- Declarative integrity and transactional guarantees.
- Efficient access through indexes and query optimization.
- Safe concurrent access through isolation and locking.
- Availability and scale through replication and distribution.

## Limitations

- Guarantees differ by engine, configuration, and failure mode.
- Schema and index changes on large datasets require operational planning.
- Replication can serve stale data and does not inherently scale writes.
- Distributed consistency and transactions add latency and complexity.
- Poor models or queries cannot be fixed merely by larger infrastructure.

## Best Practices

- Model around invariants and access patterns; document consistency requirements.
- Enforce critical integrity in the database and validate at API boundaries.
- Use migrations that are backward-compatible across rolling deployments.
- Keep transactions short and external side effects outside transaction boundaries.
- Examine query plans, cardinality, lock waits, pool saturation, and replication lag.
- Test restores; define recovery point and recovery time objectives.
- Prefer the simplest database that satisfies current, evidenced requirements.

## Common Mistakes

- Choosing a database from trend, company size, or an unsupported performance claim.
- Treating NoSQL as schema-free or automatically horizontally scalable.
- Adding indexes to every column or relying on low-cardinality indexes without evidence.
- Increasing connection pools or replicas without locating the bottleneck.
- Assuming replicas are current or are substitutes for backups.
- Sharding before optimizing schema, queries, indexes, caching, and vertical capacity.
- Holding transactions open while calling external services.

## Real-world examples

- Payments use relational constraints and short ACID transactions for ledger invariants.
- A product catalog may store aggregate-shaped documents while orders remain relational.
- A read-heavy service routes tolerant queries to asynchronous replicas and consistency-sensitive reads to the primary.
- Time-series tables partition by date for pruning, retention, and archival.
- A global feed may accept eventual consistency to remain available during partitions.

## Interview Questions

1. **How do you select a database?** Start with data shape, invariants, access patterns, consistency, scale, operations, and team capability.
2. **SQL or NoSQL?** Choose the concrete model whose guarantees and query behavior fit the workload; using both is valid with clear ownership.
3. **What are ACID properties?** Atomicity, consistency, isolation, and durability define transaction guarantees.
4. **What does CAP actually state?** During a partition, a distributed system must sacrifice either linearizable consistency or availability.
5. **Why do indexes slow writes?** Inserts, updates, and deletes must maintain each affected index.
6. **When should data be denormalized?** After a measured read bottleneck, with a defined consistency and repair strategy.
7. **Optimistic or pessimistic locking?** Optimistic locking favors rare conflicts and retries; pessimistic locking favors predictable conflict prevention at lower concurrency.
8. **How do you size a connection pool?** Load test against database capacity and observe queueing, latency, CPU, locks, and active connections.
9. **Replication or sharding?** Replication primarily improves availability and reads; sharding distributes storage and writes at much higher complexity.
10. **Why is replication not a backup?** Replication copies accidental deletion and corruption; backups preserve recoverable historical states.

## Interview Tips

Frame answers around workload and failure semantics. Distinguish product capabilities from defaults, and explain how you would measure query plans, lock contention, replication lag, and recovery rather than asserting that one database is universally superior.

## References

- [PostgreSQL documentation](https://www.postgresql.org/docs/current/)
- [MySQL 8.4 Reference Manual](https://dev.mysql.com/doc/refman/8.4/en/)
- [MongoDB Manual](https://www.mongodb.com/docs/manual/)
- [Redis documentation](https://redis.io/docs/latest/)
- [PostgreSQL transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html)
- [PostgreSQL indexes](https://www.postgresql.org/docs/current/indexes.html)
- [PostgreSQL table partitioning](https://www.postgresql.org/docs/current/ddl-partitioning.html)
- [MongoDB replication](https://www.mongodb.com/docs/manual/replication/)
- [MongoDB sharding](https://www.mongodb.com/docs/manual/sharding/)
- [Brewer's CAP theorem retrospective](https://www.infoq.com/articles/cap-twelve-years-later-how-the-rules-have-changed/)
