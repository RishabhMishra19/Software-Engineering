import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory reference model. A production service would preserve the same
 * state transitions inside database transactions with row locks or CAS.
 */
public final class BookMyShow {
    enum BookingStatus { PENDING_PAYMENT, CONFIRMED, PAYMENT_FAILED, EXPIRED }
    enum PaymentResult { SUCCEEDED, FAILED }

    record Hold(String id, String userId, Set<String> seats, Instant expiresAt) {
        Hold {
            seats = Set.copyOf(seats);
        }
    }

    record Booking(String id, String holdId, String userId, Set<String> seats,
                   BookingStatus status, String paymentReference) {
        Booking {
            seats = Set.copyOf(seats);
        }

        Booking with(BookingStatus next, String paymentReference) {
            return new Booking(id, holdId, userId, seats, next, paymentReference);
        }
    }

    static final class ShowInventory {
        private final Set<String> allSeats;
        private final Duration holdTtl;
        private final Clock clock;
        private final Map<String, Hold> holds = new HashMap<>();
        private final Map<String, Booking> bookings = new HashMap<>();
        private final Map<String, String> paymentToBooking = new HashMap<>();
        private final Set<String> bookedSeats = new HashSet<>();

        ShowInventory(Set<String> allSeats, Duration holdTtl, Clock clock) {
            if (allSeats.isEmpty() || holdTtl.isNegative() || holdTtl.isZero()) {
                throw new IllegalArgumentException("Seats and positive hold TTL are required");
            }
            this.allSeats = Set.copyOf(allSeats);
            this.holdTtl = holdTtl;
            this.clock = Objects.requireNonNull(clock);
        }

        synchronized Hold hold(String userId, Set<String> requestedSeats) {
            Objects.requireNonNull(userId);
            if (requestedSeats.isEmpty() || !allSeats.containsAll(requestedSeats)) {
                throw new IllegalArgumentException("Unknown or empty seat selection");
            }
            expireDueHolds();
            Set<String> unavailable = unavailableSeats();
            if (requestedSeats.stream().anyMatch(unavailable::contains)) {
                throw new IllegalStateException("One or more seats are unavailable");
            }
            Hold hold = new Hold(UUID.randomUUID().toString(), userId, requestedSeats,
                    clock.instant().plus(holdTtl));
            holds.put(hold.id(), hold);
            return hold;
        }

        synchronized Booking startBooking(String holdId, String userId) {
            expireDueHolds();
            Hold hold = requireLiveOwnedHold(holdId, userId);
            return bookings.values().stream()
                    .filter(b -> b.holdId().equals(holdId))
                    .findFirst()
                    .orElseGet(() -> {
                        Booking booking = new Booking(UUID.randomUUID().toString(), hold.id(),
                                userId, hold.seats(), BookingStatus.PENDING_PAYMENT, null);
                        bookings.put(booking.id(), booking);
                        return booking;
                    });
        }

        synchronized Booking recordPayment(String bookingId, String paymentReference,
                                           PaymentResult result) {
            Objects.requireNonNull(paymentReference);
            Booking booking = requireBooking(bookingId);
            String priorBookingId = paymentToBooking.get(paymentReference);
            if (priorBookingId != null) {
                if (!priorBookingId.equals(bookingId)) {
                    throw new IllegalStateException("Payment reference already used");
                }
                return requireBooking(bookingId);
            }
            if (booking.status() != BookingStatus.PENDING_PAYMENT) {
                throw new IllegalStateException("Booking is not payable");
            }
            Hold hold = holds.get(booking.holdId());
            if (hold == null || !hold.expiresAt().isAfter(clock.instant())) {
                expireBooking(booking);
                throw new IllegalStateException("Seat hold expired before payment");
            }

            BookingStatus next = result == PaymentResult.SUCCEEDED
                    ? BookingStatus.CONFIRMED : BookingStatus.PAYMENT_FAILED;
            Booking updated = booking.with(next, paymentReference);
            bookings.put(bookingId, updated);
            paymentToBooking.put(paymentReference, bookingId);
            holds.remove(booking.holdId());
            if (next == BookingStatus.CONFIRMED) {
                bookedSeats.addAll(booking.seats());
            }
            return updated;
        }

        synchronized Set<String> availableSeats() {
            expireDueHolds();
            Set<String> available = new HashSet<>(allSeats);
            available.removeAll(unavailableSeats());
            return Set.copyOf(available);
        }

        private Hold requireLiveOwnedHold(String holdId, String userId) {
            Hold hold = holds.get(holdId);
            if (hold == null || !hold.userId().equals(userId)) {
                throw new IllegalStateException("Live hold not found for user");
            }
            return hold;
        }

        private Booking requireBooking(String bookingId) {
            Booking booking = bookings.get(bookingId);
            if (booking == null) throw new IllegalArgumentException("Unknown booking");
            return booking;
        }

        private Set<String> unavailableSeats() {
            Set<String> unavailable = new HashSet<>(bookedSeats);
            holds.values().forEach(h -> unavailable.addAll(h.seats()));
            return unavailable;
        }

        private void expireDueHolds() {
            Instant now = clock.instant();
            List<Hold> expired = holds.values().stream()
                    .filter(h -> !h.expiresAt().isAfter(now))
                    .toList();
            for (Hold hold : expired) {
                holds.remove(hold.id());
                bookings.values().stream()
                        .filter(b -> b.holdId().equals(hold.id())
                                && b.status() == BookingStatus.PENDING_PAYMENT)
                        .forEach(this::expireBooking);
            }
        }

        private void expireBooking(Booking booking) {
            bookings.put(booking.id(), booking.with(BookingStatus.EXPIRED, null));
            holds.remove(booking.holdId());
        }
    }

    static final class MutableClock extends Clock {
        private Instant instant;

        MutableClock(Instant instant) { this.instant = instant; }
        void advance(Duration duration) { instant = instant.plus(duration); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }

    public static void main(String[] args) throws InterruptedException {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        ShowInventory show = new ShowInventory(Set.of("A1", "A2"), Duration.ofMinutes(5), clock);

        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        List<Thread> contenders = new ArrayList<>();
        for (String user : List.of("alice", "bob")) {
            Thread thread = new Thread(() -> {
                try {
                    start.await();
                    show.hold(user, Set.of("A1"));
                    successes.incrementAndGet();
                } catch (IllegalStateException expected) {
                    // Exactly one contender must lose.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            contenders.add(thread);
            thread.start();
        }
        start.countDown();
        for (Thread contender : contenders) contender.join();
        check(successes.get() == 1, "atomic hold permits one winner");

        Hold hold = show.hold("carol", Set.of("A2"));
        Booking pending = show.startBooking(hold.id(), "carol");
        Booking confirmed = show.recordPayment(pending.id(), "pay-1", PaymentResult.SUCCEEDED);
        check(confirmed.status() == BookingStatus.CONFIRMED, "payment confirms booking");
        check(show.recordPayment(pending.id(), "pay-1", PaymentResult.SUCCEEDED).equals(confirmed),
                "payment callback is idempotent");

        ShowInventory expiring = new ShowInventory(Set.of("B1"), Duration.ofMinutes(1), clock);
        expiring.hold("dave", Set.of("B1"));
        clock.advance(Duration.ofMinutes(2));
        check(expiring.availableSeats().contains("B1"), "expired hold releases seat");
        System.out.println("BookMyShow self-tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
