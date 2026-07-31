# BookMyShow Design

## Beginner Design Map

The difficult part is not listing seats; it is making the final write truthful
when thousands of customers see the same popular show. Catalog serves discovery,
Inventory is the single authority for seat state, Booking owns the customer
purchase lifecycle, the Payment adapter translates provider results, and the
expiry worker cleans abandoned holds. Each exists because it owns a different
kind of change and failure.

Follow the normal path as Catalog read → Inventory hold → pending Booking →
Payment callback → Inventory confirmation. The later sections explain why
locks or versions serialize seat races, why durable state outranks cache, why a
late payment needs reconciliation, and what is traded for scale.

## Component Boundaries
The Catalog service serves cinemas, shows, and denormalized seat maps. The Inventory service is the consistency boundary for show-seat state and holds. The Booking service owns the customer-facing lifecycle. A Payment adapter normalizes provider callbacks. An expiry worker and transactional outbox handle asynchronous cleanup and publication.

## Persistence Model
Use relational tables keyed by `(show_id, seat_id)` for inventory, `hold_id` for holds, and `booking_id` for bookings. Inventory rows contain `state`, `hold_id`, and a monotonic `version`. Payment references have a unique constraint. Booking transitions append to an audit table in the same transaction.

Hold creation locks requested inventory rows in deterministic seat order, verifies all are available (or expired), then updates all rows and inserts the hold. This avoids deadlocks and guarantees all-or-nothing acquisition. Confirmation validates the hold and changes its seats from `HELD` to `BOOKED` in one transaction.

## Concurrency Strategy
The reference implementation uses a monitor per `ShowInventory`, which makes the invariant obvious in one process. A deployed system should partition requests by `show_id` and use database row locks or compare-and-set versions. Redis TTL keys alone are insufficient because expiry and durable booking confirmation cannot be committed atomically without an additional authority.

## Payment Workflow
Creating a booking does not call the provider inside a database transaction. The API records `PENDING_PAYMENT`, then invokes payment with a stable idempotency key. A callback transaction inserts the unique payment reference, validates the live hold, transitions the booking, updates seats, and emits an outbox event. Ambiguous or late successes go to reconciliation and refund processing.

## Scaling and Availability
Catalog reads use CDN/cache replicas. Inventory writes route by show partition; exceptionally popular shows can shard by seat block while multi-block requests use a database transaction. Read replicas may display slightly stale maps, but hold responses always come from the write authority. Backpressure and per-user limits reduce hot-show abuse.

## Observability and Security
Trace hold, booking, and payment IDs together. Alert on expiry lag, callback age, transition conflicts, and payments without confirmed bookings. Authorize ownership on every mutation, tokenize provider details, rate-limit holds, and redact payment data from logs.

## Recovery
An indexed expiry scan releases overdue rows using conditional updates. Outbox relay retries safely. Reconciliation compares provider settlements with local terminal states. Database backups and the transition audit support replay, while version checks prevent an old worker from releasing a renewed or confirmed seat.

See the [requirements](Requirements.md), [class model](UML/README.md), [interaction flows](Sequence-Diagrams/README.md), and [Java model](implementation/java/BookMyShow.java).
