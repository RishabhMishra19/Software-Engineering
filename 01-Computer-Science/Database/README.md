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

### Detailed database choices

There is no context-free “best” database. A choice affects consistency, query performance, scaling, modeling, developer productivity, operations, maintenance, and future evolution. Start from requirements and finish with a product choice:

```text
Business requirements
        ↓
Natural data structure
        ↓
Consistency and durability
        ↓
Query and access patterns
        ↓
Scale and geographic needs
        ↓
Operational capability
        ↓
Database choice
```

Selection questions:

- **Structure:** Is the data relational, aggregate-shaped, hierarchical, semi-structured, or rapidly changing?
- **Correctness:** Must balances, stock, orders, or other invariants be immediately correct, or can feeds and analytics converge later?
- **Queries:** Are joins, aggregation, reporting, recursive traversal, or full-text search central?
- **Scale:** What are the expected data size, read/write mix, throughput, growth, and global-distribution needs?
- **Performance:** Which measured access path dominates—point reads, writes, analytical scans, or high-frequency transactions?
- **Operations:** Can the team reliably run backups, restores, monitoring, replication, failover, upgrades, and scaling?
- **Ecosystem:** Are the required ORM, migration, backup, cloud, and monitoring integrations mature?

| Database | Model | Particularly suitable for | Important trade-off |
| --- | --- | --- | --- |
| PostgreSQL | Relational SQL with document support | Relationships, financial and enterprise rules, complex SQL, reporting | Distributed write scaling and major schema changes require planning |
| MySQL/InnoDB | Relational SQL | General web, SaaS, CRUD, mature hosted deployments | Less extensible than PostgreSQL; advanced needs must be compared by current version |
| MongoDB | Document | Aggregate documents, evolving product models, native sharding | No foreign keys; duplication and cross-document coordination move complexity elsewhere |
| Redis | Key-value/data structure server | Caches, sessions, counters, rate limits | Usually complements rather than replaces the system of record |
| Cassandra | Wide-column distributed store | Very large, write-heavy, predefined distributed access paths | Joins and cross-row transactional workflows are a poor fit |

#### PostgreSQL

PostgreSQL supplies transactions, constraints, indexes, relationships, JSON/JSONB, views and materialized views, CTEs, recursive queries, window functions, stored procedures, and extensibility. PostGIS and pgvector are notable extensions; custom types, functions, operators, and index methods broaden its range. It integrates with Spring Boot, Hibernate, Sequelize, Prisma, TypeORM, Flyway, Liquibase, Docker, and Kubernetes.

Choose it when correctness, relationships, financial operations, sophisticated SQL, reporting, or long-term maintainability dominate. It also works well for startups, SaaS, personal projects, and database-per-service microservices—not only “enterprise” systems. Avoid forcing it onto a workload that is almost entirely unconstrained, rapidly changing documents, or one whose primary hard requirement is turnkey horizontal write distribution. PostgreSQL can scale through tuning, indexing, caching, partitioning, replicas, and distributed extensions, but each mechanism has a different purpose.

> **Professional correction:** UUID is a built-in PostgreSQL data type, and full-text search is a built-in feature; they should not be listed as extensions alongside PostGIS and pgvector.

> **Professional correction:** “PostgreSQL is slower than NoSQL” and “PostgreSQL cannot scale” are category errors. Performance and scale depend on model, workload, topology, indexes, queries, hardware, and operational design.

#### MySQL

MySQL with InnoDB offers mature ACID transactions, constraints, indexes, replication, partitioning, broad managed-hosting support, and a large ecosystem. It is approachable to administer and performs well for common CRUD and read-heavy web workloads. Spring Boot, Hibernate, Sequelize, Prisma, TypeORM, Flyway, Liquibase, Docker, and Kubernetes all have established support.

Choose it for general web and SaaS systems when team familiarity, operational simplicity, and predictable CRUD behavior matter. Prefer PostgreSQL when its concrete capabilities—such as richer extension points or a specific SQL/data-type feature—solve a demonstrated requirement. Prefer a document store when the aggregate model genuinely fits better. MySQL is neither obsolete nor restricted to small systems; many large internet and enterprise systems use it successfully.

> **Professional correction:** “MySQL has fewer advanced SQL features” is too broad without versions and named features. Modern MySQL supports CTEs, recursive CTEs, window functions, JSON, and more. Compare the exact feature, semantics, optimizer behavior, and supported release rather than relying on a historical label.

#### MongoDB

MongoDB stores BSON documents with nested objects and arrays. Embedding can make aggregate reads natural and reduce joins; document evolution can accelerate early product iteration. Replica sets, sharding, Atlas, validation, aggregation pipelines, and multi-document transactions support production deployments, with integrations including Spring Boot, Express, Mongoose, Prisma, Docker, and Kubernetes.

Choose it when the application normally reads and writes complete aggregate documents, fields evolve, and horizontal distribution is a real requirement. Common examples include content, product catalogs, and profiles. Avoid it when foreign-key relationships, complex relational joins, strict cross-aggregate invariants, or transaction-heavy financial workflows dominate. Embedding may duplicate facts and make updates harder; references are application-enforced because MongoDB has no foreign keys.

> **Professional correction:** MongoDB is not schema-free. It has a flexible document schema and supports collection validators; applications still need versioning, validation, and migrations.

> **Professional correction:** MongoDB supports multi-document ACID transactions. The reason to prefer a relational model for highly transactional domains is usually modeling, constraints, access paths, and operational simplicity—not an absence of transaction support.

#### PostgreSQL, MySQL, and MongoDB comparison

| Decision signal | PostgreSQL | MySQL/InnoDB | MongoDB |
| --- | --- | --- | --- |
| Complex relationships and joins | Strong fit | Strong fit | Possible, but usually not its natural model |
| Financial invariants | Strong fit | Strong fit with appropriate design | Possible, but cross-document design requires care |
| General CRUD web system | Strong fit | Strong fit, often operationally familiar | Fit when CRUD aligns with document aggregates |
| Evolving document shape | JSONB or relational migrations | JSON or relational migrations | Native flexible documents |
| Advanced extensibility | Major strength | More limited extension surface | Product-specific operators and pipelines |
| Horizontal distribution | External/distributed architecture choices | External/clustering architecture choices | Native sharding support |
| Reporting/ad hoc SQL | Major strength | Strong and version-dependent | Aggregation pipeline; SQL tooling is not native |

The interview-quality answer to “Why X over Y?” names the domain invariants, dominant queries, scaling constraint, and team operations. Product popularity alone is not evidence.

### SQL and NoSQL decision detail

SQL systems commonly offer declared relational schemas, native joins, constraints, strong transactions, mature tools, and expressive ad hoc querying. Costs include planned migrations and more complex horizontal write distribution. NoSQL covers several unrelated models; benefits may include aggregate flexibility, specialized access paths, and built-in distribution, while relationship enforcement, joins, and consistency semantics vary by database.

Use relational SQL when data has important relationships, complex reporting, financial operations, database-enforced rules, or strict integrity. Consider a particular NoSQL database for a particular access pattern: changing aggregate documents, enormous key-oriented throughput, graph traversal, or wide-column distribution. Polyglot persistence can keep orders in PostgreSQL, catalogs in MongoDB, and ephemeral cache data in Redis when ownership and consistency boundaries are explicit.

> **Professional correction:** “NoSQL” did not originate as a universal acronym for “Not Only SQL”; that phrase became a popular later interpretation. More importantly, NoSQL is not one consistency, schema, transaction, or scaling model.

> **Professional correction:** Fixed-versus-flexible, scalable-versus-unscalable, and ACID-versus-BASE are not reliable SQL/NoSQL binaries. Products, configurations, and application designs determine those properties.

### Transaction detail

A bank transfer demonstrates atomic work:

```sql
BEGIN;
UPDATE account SET balance = balance - 100 WHERE id = 1;
UPDATE account SET balance = balance + 100 WHERE id = 2;
COMMIT; -- use ROLLBACK if either update or an invariant check fails
```

Single statements are transactions in common engines (often through autocommit). Multi-statement transactions can create an order, decrement inventory, record payment, and create an invoice as one unit. Use transactions for payments, transfers, reservations, related-table updates, and any invariant that must change atomically. Avoid holding them open for bulk processing, analytical work, long business workflows, or network calls.

A commit ends the transaction successfully and makes its effects durable according to the database’s durability configuration. A rollback abandons the transaction’s uncommitted effects. Transactions add logging, versioning/locking, and coordination overhead; long transactions retain resources, increase contention, delay cleanup, and raise failure cost. Cross-database atomicity may use two-phase commit (2PC), but coordinator failure modes, blocking, and operational coupling often motivate sagas or explicit compensation instead.

> **Professional correction:** Committed data is not necessarily immediately visible to every already-running transaction; visibility depends on its isolation level and snapshot.

ACID is about transaction guarantees:

- **Atomicity:** the transaction takes effect as a unit.
- **Consistency:** if application logic and constraints are correct, the transaction moves between valid states.
- **Isolation:** concurrent outcomes obey the selected isolation contract.
- **Durability:** acknowledged committed effects survive the failures covered by the configured guarantee.

BASE—Basically Available, Soft state, Eventual consistency—is an informal distributed-design description, not ACID’s precise opposite. BASE systems can offer high availability and throughput while requiring conflict handling and convergence-aware application logic. ACID can reduce concurrency or add distributed coordination; BASE can expose stale or conflicting values and make debugging harder.

Choose strict transactional guarantees for balances, stock, orders, payments, and bookings. Eventual convergence may suit feeds, view counts, recommendations, and some messaging data when temporary divergence is acceptable.

> **Professional correction:** Eventual consistency does not guarantee that replicas converge merely because time passes. Convergence requires writes to stop or conflicts to be resolved, communication to recover, and the system’s reconciliation process to succeed.

> **Professional correction:** ACID does not imply availability, and BASE does not imply permanent inconsistency. SQL does not always mean ACID-only, and NoSQL does not always mean BASE-only.

### Indexing detail

An index is a maintained access structure that can replace a full scan with a narrower lookup. It may help selective `WHERE` predicates, joins, ordering, grouping, and some covering queries. It consumes disk and cache and adds work to inserts, deletes, and updates of indexed values. Review unused and overlapping indexes, but use execution plans and representative statistics before changing them.

Common forms include primary-key/unique access paths, composite indexes, and engine-specific B-tree, hash, full-text, spatial, partial, expression, or specialized structures. For a B-tree composite index:

```sql
CREATE INDEX idx_person_name ON person (first_name, last_name);
```

It naturally supports predicates beginning with `first_name`, including `(first_name, last_name)`, but generally not a lookup on `last_name` alone. Column order must follow real predicates, selectivity, ordering, and engine behavior.

Consider indexes for frequent selective searches and join keys; test ordering/grouping benefits. Be cautious with tiny tables, rarely queried columns, write-hot columns, temporary data, and indexes that duplicate existing prefixes.

> **Professional correction:** A primary-key constraint normally creates or uses a unique index in PostgreSQL and MySQL/InnoDB, but “primary index” and physical clustering behavior are engine-specific. InnoDB clusters table storage by the primary key; PostgreSQL normally stores a heap, and `CLUSTER` is a one-time rewrite that is not continuously maintained.

> **Professional correction:** Low cardinality alone is not a rule against indexing. Partial indexes, bitmap combinations, covering indexes, skewed values, and compound predicates can make such an index useful. Validate with plans and workload measurements.

> **Professional correction:** Too many indexes primarily increase write, storage, cache, planning, and maintenance cost. Saying they “confuse” an optimizer is imprecise; an optimizer may misestimate costs, but candidate choice itself is not confusion.

### Normalization and denormalization detail

Normalization stores each fact in one authoritative place, reducing duplicates, update anomalies, and storage. Denormalization intentionally duplicates, aggregates, or precomputes facts to remove expensive joins or repeated computation. Normalized models favor frequent updates and transactional integrity; denormalized projections favor measured read and reporting paths but require synchronization, repair, and ownership rules.

For example, this denormalized design repeats customer data:

```text
orders(order_id, customer_id, customer_name, customer_email, total)
```

A normalized design separates the facts:

```text
customers(customer_id, name, email)
orders(order_id, customer_id, total)
```

The normalized form updates an email once and protects the relationship with a foreign key. A read model may deliberately copy `customer_name` into an order snapshot when historical display semantics or measured read performance require it. Most production systems normalize transactional sources and selectively denormalize tables, materialized views, caches, or search documents.

> **Professional correction:** Normalization does not inherently make writes “faster,” nor does denormalization inherently make reads faster. Each changes the number and shape of operations; measure the actual workload. Denormalization also does not remove the need for indexes.

### Locking detail

Optimistic concurrency reads a version and conditions the update on that version:

```sql
UPDATE product
SET stock = 9, version = version + 1
WHERE id = 42 AND version = 7;
```

Zero affected rows means another writer won, so the caller reloads and retries or reports conflict. This fits profiles, content, and catalog updates when conflicts are rare. Pessimistic concurrency acquires a lock, such as `SELECT ... FOR UPDATE`, before a short critical change; it fits inventory, booking, and financial operations when conflicting writes are likely and the wait is bounded.

Locks protect concurrent operations but reduce concurrency and consume resources. Use consistent acquisition order, narrow locked sets, short transactions, sensible timeouts, and retry handling.

> **Professional correction:** A deadlock is a wait cycle, not necessarily an indefinite application hang. Production databases detect deadlocks and abort at least one participant; applications must handle and retry the chosen victim when safe.

> **Professional correction:** Optimistic locking does not mean the database uses no internal locks, and pessimistic locking is not universally safer. Retry rates and lock waits determine which performs better.

### Connection-pooling detail

At startup or on demand, a pool opens bounded reusable connections. A request borrows one, performs database work, and returns it; when all are busy, callers queue until a connection is returned or the acquisition timeout expires. Pools reduce repeated network, authentication, allocation, and session-initialization cost.

Too few connections create queueing; too many consume memory, create context switching and lock pressure, and can exceed database limits. Tune maximum size, minimum/idle size, acquisition timeout, idle timeout, maximum lifetime, validation, and leak detection. Account for every application instance: ten replicas each with a pool of twenty can open two hundred database sessions. Determine capacity from measurements, not end-user count, and remember that pooling cannot fix slow SQL or missing indexes.

### Replication detail

Leader/follower (primary/replica) replication keeps multiple copies. Read traffic that tolerates lag can go to replicas; a promoted replica can reduce downtime after leader failure. Replicas also support geographic copies and recovery options, but add monitoring, failover, backup, consistency-routing, and infrastructure cost.

With synchronous replication, commit acknowledgement waits for the configured replica acknowledgement, reducing a defined data-loss window while increasing write latency. With asynchronous replication, the leader acknowledges sooner, allowing lag and possible loss of unreplicated writes if it fails. Read-after-write paths may need the primary, session guarantees, or a replication-position check.

> **Professional correction:** “Synchronous” does not universally mean every replica has durably written the change. The required acknowledgements, quorum, memory/disk state, and failure guarantees are product and configuration specific.

> **Professional correction:** Replication contributes to availability and disaster-recovery architecture but is not a backup: accidental deletion, corruption, and malicious writes can replicate. Keep independent, point-in-time-restorable backups and test restores.

Replication normally scales reads, not a single leader’s writes. More replicas help only if read load and routing justify their cost.

### Partitioning and sharding detail

Partitioning divides a logical table into pieces that the optimizer can prune and operators can maintain independently. Typical strategies are:

- **Range:** orders by year, logs by month, transactions by date.
- **List:** country, region, or department.
- **Hash:** a hash distributes rows among a fixed set of partitions.

Partition when a table is very large, queries commonly include the partition key, and pruning, archival, retention, or maintenance are the goals. Indexes remain important inside partitions.

Sharding distributes ownership across independently scalable database nodes. Common strategies are:

- **Range-based:** simple and range-query friendly, but vulnerable to uneven growth and hot ranges.

```text
Shard 1 → customer_id 1–1,000,000
Shard 2 → customer_id 1,000,001–2,000,000
```

- **Hash-based:** usually balances distribution but scatters range queries and complicates resharding.
- **Directory-based:** a lookup maps keys to shards, adding flexibility and another highly available component.

Shard only when one database cannot satisfy storage or write throughput, or hard geographic/tenancy boundaries require it. Select a high-cardinality shard key that matches routing and avoids hotspots. Plan cross-shard queries, global uniqueness, transactions, joins, rebalancing, failures, and observability. Large systems may shard across nodes and partition tables within each shard.

> **Professional correction:** Partitioning is not universally restricted to one physical server. It is a logical table-layout technique whose physical placement depends on the product. Sharding refers to distributing ownership across independent database nodes or clusters; the precise boundary is architectural, not merely “same server versus multiple servers.”

### CAP in practice

CAP applies when communication is partitioned. A CP design may reject or delay operations that cannot preserve a single-copy/linearizable view; an AP design continues responding at non-failing nodes and may expose divergent versions requiring reconciliation. Outside partitions, systems still trade latency and consistency, but those are not the CAP impossibility statement.

- CP-oriented scenarios: balance changes, payments, stock allocation, order state, ticket reservations, and trading where accepting a conflicting operation is worse than temporary rejection.
- AP-oriented scenarios: feeds, likes, view counters, recommendations, catalogs, and some messaging paths where a stale or mergeable response is preferable to unavailability.
- Standalone PostgreSQL is outside CAP’s distributed premise. A distributed PostgreSQL topology’s behavior depends on replication and failover configuration.
- MongoDB behavior depends on read preference, read concern, write concern, elections, and topology; typical majority-oriented replica-set operation leans CP during partitions, while Cassandra can be configured toward availability with tunable consistency.

> **Professional correction:** CAP consistency means a single-copy consistency model commonly formalized as linearizability, not simply “every read gets the latest write” without timing qualifications. CAP availability means every request to a non-failing node eventually receives a non-error response; it does not promise low latency or fresh data.

> **Professional correction:** “Pick any two” is misleading. When no partition exists, both consistency and availability may be provided; during a partition, a distributed system that tolerates the partition cannot guarantee both.

ACID describes transaction semantics, CAP describes behavior under a network partition, replication creates and synchronizes copies, and partitioning/sharding divide data. These related tools answer different questions.

### Trade-offs

- Stronger consistency simplifies business logic but may reduce availability or increase latency.
- Flexible models speed some schema changes but move relationship enforcement into applications.
- Normalization improves integrity; denormalization improves selected reads.
- Indexes accelerate reads while increasing write and storage costs.
- Replicas scale reads; sharding can scale writes but sharply increases operations complexity.

### Decision matrix

These are starting hypotheses, not automatic choices; verify them against product versions, topology, workload tests, and failure requirements.

| Requirement | First option to evaluate | Qualification |
| --- | --- | --- |
| Strong relational consistency, financial rules, complex joins | PostgreSQL or MySQL/InnoDB | Both can implement ACID systems; compare exact features and operations |
| Rich SQL, extension ecosystem, geospatial/vector needs | PostgreSQL | Confirm the required extension and managed-service support |
| Familiar general-purpose web/CRUD stack | MySQL/InnoDB or PostgreSQL | Team capability may dominate small feature differences |
| Aggregate documents and rapidly evolving fields | MongoDB | Keep validation, ownership, migrations, and transaction boundaries explicit |
| Cache, session, rate limiting | Redis | Usually not the durable source of truth |
| Massive write-oriented, distributed key access | Cassandra | Model around predefined queries; avoid relational joins |

| Design decision | Prefer left side when | Prefer right side when |
| --- | --- | --- |
| SQL / a specific NoSQL model | Relationships, ad hoc SQL, constraints, multi-row invariants | A concrete document, key-value, graph, or wide-column access pattern justifies it |
| ACID / eventual convergence | Atomic correctness and immediate invariants dominate | Temporary divergence is acceptable and reconciliation is defined |
| Normalization / denormalization | Facts change often and update anomalies matter | A measured read path needs a projection, snapshot, or aggregate |
| Optimistic / pessimistic concurrency | Conflicts are uncommon and retries are cheap | Conflicts are common and a short bounded wait is safer |
| Synchronous / asynchronous replication | A smaller data-loss window justifies commit latency | Lower write latency justifies lag and failover exposure |
| Partitioning / sharding | One logical database can meet capacity; table management is the issue | Storage/write/geographic requirements exceed one database boundary |
| CP / AP during a partition | Rejecting work is safer than accepting divergent state | Serving mergeable or stale state is safer than rejecting work |

Operational quick checks:

| Situation | Direction |
| --- | --- |
| Payment, order creation, inventory decrement | Use a short transaction |
| Read-only query | An explicit transaction is often unnecessary, though the engine still provides statement semantics |
| Long workflow or remote API call | Keep outside a long-held database transaction; use orchestration/compensation |
| Selective filter, join key, useful ordering/grouping | Test an index with an execution plan |
| Tiny table, write-hot or rarely queried column | Usually avoid an additional index unless evidence supports it |
| Read traffic greatly exceeds write traffic | Evaluate caching and replicas |
| Single-leader write bottleneck | Replicas alone will not fix it; tune, partition, or evaluate sharding |
| Huge time-based table | Evaluate range partitioning |
| Boolean/low-cardinality filter | Decide from skew, compound predicates, partial indexes, and plans—not cardinality alone |

### Engineering principles

When making database decisions, apply these principles as a checklist:

1. Start with business requirements, not technology popularity.
2. Prefer simplicity over unnecessary complexity.
3. Optimize only after identifying real bottlenecks.
4. Design for maintainability before scalability.
5. Use strong consistency only when the domain requires it.
6. Scale horizontally only when vertical scaling is insufficient.
7. Measure performance before optimizing.
8. Every optimization introduces trade-offs.

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
- Assuming PostgreSQL is enterprise-only, MySQL is obsolete or small-scale, or MongoDB replaces relational databases.
- Treating PostgreSQL/MySQL/MongoDB performance as a category property instead of testing the actual model and workload.
- Selecting NoSQL because “every modern application uses it,” or interpreting it as “no schema” or “always faster.”
- Equating SQL with ACID and NoSQL with BASE, or believing ACID prevents infrastructure failure.
- Assuming eventual consistency makes values correct on its own or necessarily takes a long time.
- Believing larger transactions are safer, every read needs an explicit transaction, or transactions eliminate deadlocks and isolation anomalies.
- Creating every possible index, assuming more indexes are always better, or assuming partitioning/denormalization replaces indexing.
- Applying clustered/non-clustered terminology uniformly across database engines.
- Assuming optimistic concurrency is always faster or lock-free, pessimistic concurrency is always safer, or locking eliminates starvation and anomalies.
- Sizing a pool to user count, overlooking per-instance pool multiplication, or expecting pooling to repair inefficient queries.
- Expecting replication to increase leader write throughput, eliminate backups, or guarantee current reads.
- Treating partitioning and sharding as synonyms, expecting either to improve every query, or sharding before a hard need exists.
- Treating CAP as “choose any two” in all operating conditions or assuming AP always means incorrect data and CP means no failures.

## Real-world examples

- Payments use relational constraints and short ACID transactions for ledger invariants.
- A product catalog may store aggregate-shaped documents while orders remain relational.
- A read-heavy service routes tolerant queries to asynchronous replicas and consistency-sensitive reads to the primary.
- Time-series tables partition by date for pruning, retention, and archival.
- A global feed may accept eventual consistency to remain available during partitions.
- A booking service uses a short pessimistic lock when simultaneous allocation of the same seat is likely.
- A profile editor uses a version column and asks the losing writer to reload after an optimistic conflict.
- An order read model copies customer display data for fast/history-stable reads while the normalized customer row remains authoritative.
- A database that has outgrown a node uses a shard key chosen from measured routing and hotspot behavior, then partitions large tables within each shard.

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

### Complete question bank

Use these prompts to test reasoning rather than memorized product rankings. Questions that overlap the answered set above are retained when they introduce a distinct comparison, scenario, or operational concern from the source.

#### Database fundamentals and product selection

- What factors should be evaluated when selecting a database?
- How would you decide among PostgreSQL, MySQL, and MongoDB?
- What advantages do relational databases provide?
- What advantages can a purpose-built NoSQL database provide?
- Which document, key-value, graph, and wide-column NoSQL models exist, and what workloads fit each?
- What is polyglot persistence, and when can multiple databases be justified?
- Why would you choose PostgreSQL rather than MongoDB, and when would that be wrong?
- Why would you choose PostgreSQL rather than MySQL?
- Why would you choose MySQL rather than PostgreSQL?
- Is PostgreSQL suitable for microservices and enterprise applications?
- Is PostgreSQL only a SQL/table database, or where do JSON/JSONB fit?
- What are PostgreSQL’s strongest capabilities and operational costs?
- Does MySQL support ACID transactions, and why does the storage engine matter?
- Is MySQL suitable for enterprise-scale applications?
- What are MySQL’s strengths and limitations?
- Why would you choose MongoDB rather than PostgreSQL?
- Is MongoDB schema-less?
- What disadvantages follow from embedding, duplication, and absent foreign keys?
- When should MongoDB be avoided?
- Can MongoDB support multi-document transactions and enterprise applications?

#### SQL, NoSQL, ACID, and BASE

- Compare SQL and NoSQL without treating either as one homogeneous product category.
- Which database characteristics fit a banking system, social platform, or e-commerce system?
- Can SQL and NoSQL be used together, and how should ownership be divided?
- Is NoSQL always faster than SQL?
- What do Atomicity, Consistency, Isolation, and Durability each guarantee?
- What do Basically Available, Soft state, and Eventual consistency describe?
- Why do financial systems commonly prefer strong ACID transaction semantics?
- What is eventual consistency, and what conditions are required for convergence?
- Can NoSQL databases support ACID transactions?
- Why do some geographically distributed systems choose BASE-like designs?
- Is BASE better than ACID, or are they answers to different requirements?

#### CAP theorem

- What exactly does the CAP theorem state?
- Why can a partitioned distributed system not guarantee both linearizable consistency and availability?
- How do CP and AP behavior differ during a partition?
- What is partition tolerance, and why is it operationally unavoidable in a distributed deployment?
- Which applications commonly favor CP behavior, and which can favor AP behavior?
- How should CAP be discussed for standalone PostgreSQL versus a distributed PostgreSQL topology?
- How do MongoDB read/write concerns and topology affect its CAP behavior?

#### Transactions

- What is a database transaction, and why is it important?
- How do commit and rollback differ?
- What distinguishes a single-statement transaction from a multi-statement transaction?
- Why should transactions remain short?
- Which problems remain despite transactions—deadlocks, contention, and isolation anomalies?
- Can a transaction span multiple databases?
- Why are distributed transactions and two-phase commit difficult?
- Why should an external API call not be held inside a database transaction?

#### Indexes and data modeling

- What is an index, and why can it improve query performance?
- Why do indexes slow inserts, updates, and deletes?
- How do clustered storage and secondary/non-clustered structures differ by engine?
- What is a composite index, and how does its leading-column order affect use?
- When should an index be avoided?
- How would execution plans, cardinality estimates, and production statistics reveal a missing or ineffective index?
- What is normalization, and why does it reduce update anomalies?
- What is denormalization, and when is it justified?
- Which is better for a given workload: normalization or denormalization?
- Can normalized sources and denormalized projections be used together?

#### Locking and pooling

- What is database locking?
- How do optimistic and pessimistic concurrency control differ?
- When would you choose optimistic locking?
- When would you choose pessimistic locking?
- What is a deadlock, how does a database resolve one, and how should a caller react?
- How can short transactions, lock order, narrow lock scope, and timeouts reduce deadlock risk?
- Why is optimistic locking common in read-heavy web applications?
- What is connection pooling and why is connection creation expensive?
- Why should every request not establish a fresh connection?
- What happens when a pool is exhausted?
- How should pool size be chosen across multiple application instances?

#### Replication, partitioning, and sharding

- What is replication, and which availability/read-scaling problems does it solve?
- What is replication lag, and why can users observe stale reads?
- How do synchronous and asynchronous replication trade latency against data-loss exposure?
- Does replication improve write performance?
- Why can replication never replace independent backups?
- What is table partitioning?
- What is database sharding?
- How do partitioning and sharding differ conceptually and operationally?
- When should a large table be partitioned?
- When should a database be sharded?
- Why are routing, cross-shard work, rebalancing, and distributed transactions difficult?
- Can partitioning and sharding be combined?

#### Scenario questions

- You are building a banking application. Which database and transaction model would you choose, and why?
- Millions of user profiles have frequently changing fields. Which model would you evaluate, and what validation would you retain?
- Reads are ten times writes. Which query, cache, index, and replica measurements come before scaling?
- A query on a large table takes seconds. What plans, statistics, predicates, indexes, schema choices, and resource evidence would you inspect before scaling?
- Writes exceed one server’s capacity. Which vertical, batching, partitioning, and sharding options would you evaluate?
- Users occasionally read stale replica data. Why, and which read-routing or consistency mechanism can address it?
- The same rows receive frequent conflicting updates. Which locking strategy fits, and how will timeout/retry behavior work?
- A product catalog is slow because of many joins. Would a measured denormalized projection or document aggregate help?
- A table has hundreds of millions of rows. Is the problem table maintenance/query pruning or whole-database capacity, and therefore partitioning or sharding?
- How would you decide between SQL and a specific NoSQL database for a new application?

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

## Provenance

- **Primary source restored:** `/tmp/software-engineering-sources/Learning-Engineering/01-Projects/Engineering-Decisions/02-Database.md` (“Database Engineering Decisions”).
- **Editorial material retained:** the existing topic-template overview, purpose, concise operating model, advantages, limitations, best practices, examples, interview tips, and official references from this README.
- **Editorial additions:** explicit professional corrections; product/version nuance; executable SQL and text examples for transactions, indexing, normalization, optimistic concurrency, and sharding; operational guidance for isolation, constraints, pool multiplication, replica reads, restore testing, and shard-key design; the source-section coverage structure; and the eight-item `Engineering principles` checklist restored from `# 15. Decision Matrix` in `02-Database.md`.
- Exact repetition was consolidated while preserving distinct requirements, trade-offs, misconceptions, scenarios, diagrams, code examples, decision prompts, and interview questions.
