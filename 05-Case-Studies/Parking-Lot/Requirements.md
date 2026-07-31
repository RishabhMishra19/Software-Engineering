# Parking Lot Requirements

## Scope

The system models one parking facility containing multiple floors and multiple entry and exit gates. It manages spot assignment, ticket lifecycle, fee calculation, payment, release, and availability. Physical gate hardware, a real payment gateway, persistence, and distributed deployment are outside the initial implementation.

## Actors

- **Driver:** enters, receives a ticket, pays, and exits.
- **Entry gate:** submits a vehicle for allocation and ticket generation.
- **Exit gate:** validates a ticket, calculates the fee, accepts payment, and releases the spot.
- **Operator/display board:** views current availability by floor and type.
- **Administrator:** configures floors, spots, and policies before operation.

## Functional Requirements

### Facility

1. The lot must support multiple floors.
2. Each floor must contain uniquely identified spots.
3. The design must allow multiple entry and exit gate instances.
4. Availability must be reported per floor and spot type.

### Vehicles and spots

1. The initial vehicle types are `BIKE`, `CAR`, and `TRUCK`.
2. The initial spot types are `BIKE`, `CAR`, and `TRUCK`.
3. A vehicle may occupy only a compatible spot; the baseline rule requires matching types.
4. A spot may hold at most one vehicle.
5. A vehicle registration may have at most one active ticket.

### Entry

1. Validate the vehicle and registration number.
2. Select a compatible free spot through the configured allocation strategy.
3. Atomically reserve the spot so concurrent gates cannot claim it.
4. Create an `ACTIVE` ticket with a unique ID and clock-derived entry time.
5. Update availability and the active-ticket indexes.
6. Return a clear no-spot error when capacity is exhausted.

### Exit, pricing, and payment

1. Locate and validate the active ticket.
2. Calculate the fee using the configured fee strategy.
3. Support hourly and flat pricing policies.
4. Round hourly billing up to the next started hour, with a one-hour minimum.
5. Create a payment with `PENDING`, `SUCCESS`, or `FAILED` state.
6. On successful capture, mark the ticket `PAID`, record exit time, remove it from active indexes, release the spot, and update availability.
7. A failed payment must not release the spot or close the ticket.
8. Repeated or invalid exits must fail explicitly.

## Non-Functional Requirements

- **Concurrency:** allocation, ticket creation, payment completion, and release must preserve invariants under multiple gates.
- **Performance:** a floor/type availability lookup and pool mutation should be `O(1)`; allocation may inspect floors but must not scan every spot.
- **Extensibility:** allocation and fee calculation must be replaceable policies. New vehicle, spot, reservation, and payment capabilities should have localized impact.
- **Testability:** all time-dependent behavior must use an injected `Clock`; payment behavior must be injectable.
- **Correctness:** invalid IDs, duplicate spots, duplicate active vehicles, incompatible assignments, illegal transitions, and negative fees are rejected.
- **Observability:** errors use stable domain categories and availability is queryable as structured data.
- **Maintainability:** gates orchestrate but do not own business rules; mutable state remains encapsulated.

## Invariants

- Every occupied spot is absent from its availability pool.
- Every free spot is present exactly once in its matching pool.
- Every active vehicle maps to exactly one active ticket.
- Every active ticket references one occupied spot containing the same vehicle.
- Only an `ACTIVE` ticket may be paid.
- A spot is released only after successful payment.
- Ticket and payment timestamps never precede their corresponding start timestamps.

## Assumptions

- Facility configuration is completed before traffic begins.
- Money is represented in integer minor units, not floating point.
- The baseline implementation runs in one JVM and uses one aggregate lock.
- The payment processor in the sample succeeds deterministically; production integrations need idempotency and reconciliation.
