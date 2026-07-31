import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Cohesive Java 17 parking-lot example. Compile with:
 * javac ParkingLot.java && java -ea ParkingLot
 */
public final class ParkingLot {
    public enum VehicleType { BIKE, CAR, TRUCK }
    public enum SpotType { BIKE, CAR, TRUCK }
    public enum TicketStatus { ACTIVE, PAID }
    public enum PaymentStatus { PENDING, SUCCESS, FAILED }

    public enum ErrorCode {
        VALIDATION_ERROR,
        DUPLICATE_FLOOR,
        DUPLICATE_SPOT,
        VEHICLE_ALREADY_PARKED,
        NO_SPOT_AVAILABLE,
        INCOMPATIBLE_SPOT,
        SPOT_NOT_AVAILABLE,
        TICKET_NOT_FOUND,
        TICKET_NOT_ACTIVE,
        PAYMENT_FAILED
    }

    public static final class ParkingException extends RuntimeException {
        private final ErrorCode code;

        public ParkingException(ErrorCode code, String message) {
            super(message);
            this.code = Objects.requireNonNull(code);
        }

        public ErrorCode code() {
            return code;
        }
    }

    public record Vehicle(String registration, VehicleType type) {
        public Vehicle {
            registration = requireText(registration, "registration").toUpperCase();
            type = requireNonNull(type, "vehicle type");
        }
    }

    public static final class ParkingSpot {
        private final String id;
        private final SpotType type;
        private Vehicle vehicle;

        public ParkingSpot(String id, SpotType type) {
            this.id = requireText(id, "spot id");
            this.type = requireNonNull(type, "spot type");
        }

        public String id() { return id; }
        public SpotType type() { return type; }
        public Vehicle vehicle() { return vehicle; }
        public boolean isAvailable() { return vehicle == null; }

        private void occupy(Vehicle candidate, SpotCompatibility compatibility) {
            requireNonNull(candidate, "vehicle");
            if (!isAvailable()) {
                throw new ParkingException(
                        ErrorCode.SPOT_NOT_AVAILABLE, "Spot is already occupied: " + id);
            }
            if (!compatibility.supports(candidate.type(), type)) {
                throw new ParkingException(
                        ErrorCode.INCOMPATIBLE_SPOT,
                        candidate.type() + " cannot use " + type + " spot " + id);
            }
            vehicle = candidate;
        }

        private void release(Vehicle expected) {
            if (vehicle == null || !vehicle.equals(expected)) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Spot/vehicle invariant failed for " + id);
            }
            vehicle = null;
        }
    }

    public static final class ParkingFloor {
        private final String id;
        private final Map<String, ParkingSpot> spotsById = new LinkedHashMap<>();
        private final EnumMap<SpotType, ArrayDeque<ParkingSpot>> available =
                new EnumMap<>(SpotType.class);

        public ParkingFloor(String id) {
            this.id = requireText(id, "floor id");
            for (SpotType type : SpotType.values()) {
                available.put(type, new ArrayDeque<>());
            }
        }

        public String id() { return id; }

        public void addSpot(ParkingSpot spot) {
            requireNonNull(spot, "spot");
            if (spotsById.putIfAbsent(spot.id(), spot) != null) {
                throw new ParkingException(
                        ErrorCode.DUPLICATE_SPOT, "Duplicate spot on floor " + id + ": " + spot.id());
            }
            available.get(spot.type()).addLast(spot);
        }

        public int availableCount(SpotType type) {
            return available.get(requireNonNull(type, "spot type")).size();
        }

        private ParkingSpot claim(SpotType type) {
            return available.get(type).pollFirst();
        }

        private void returnToPool(ParkingSpot spot) {
            ParkingSpot configured = spotsById.get(spot.id());
            if (configured != spot || !spot.isAvailable()) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Cannot return invalid spot " + spot.id());
            }
            ArrayDeque<ParkingSpot> pool = available.get(spot.type());
            if (pool.contains(spot)) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Spot already in availability pool: " + spot.id());
            }
            pool.addLast(spot);
        }

        private List<ParkingSpot> spots() {
            return List.copyOf(spotsById.values());
        }
    }

    @FunctionalInterface
    public interface SpotCompatibility {
        boolean supports(VehicleType vehicleType, SpotType spotType);
    }

    public static final SpotCompatibility EXACT_TYPE =
            (vehicleType, spotType) -> vehicleType.name().equals(spotType.name());

    public record Allocation(ParkingFloor floor, ParkingSpot spot) {
        public Allocation {
            requireNonNull(floor, "floor");
            requireNonNull(spot, "spot");
        }
    }

    @FunctionalInterface
    public interface ParkingSpotStrategy {
        Allocation allocate(
                List<ParkingFloor> floors, Vehicle vehicle, SpotCompatibility compatibility);
    }

    public static final class LowestFloorFirstStrategy implements ParkingSpotStrategy {
        @Override
        public Allocation allocate(
                List<ParkingFloor> floors, Vehicle vehicle, SpotCompatibility compatibility) {
            for (ParkingFloor floor : floors) {
                for (SpotType spotType : SpotType.values()) {
                    if (!compatibility.supports(vehicle.type(), spotType)) {
                        continue;
                    }
                    ParkingSpot spot = floor.claim(spotType);
                    if (spot != null) {
                        return new Allocation(floor, spot);
                    }
                }
            }
            throw new ParkingException(
                    ErrorCode.NO_SPOT_AVAILABLE,
                    "No compatible spot is available for " + vehicle.type());
        }
    }

    public static final class Ticket {
        private final String id;
        private final Vehicle vehicle;
        private final String floorId;
        private final ParkingSpot spot;
        private final Instant entryTime;
        private TicketStatus status = TicketStatus.ACTIVE;
        private Instant exitTime;

        private Ticket(
                String id, Vehicle vehicle, String floorId, ParkingSpot spot, Instant entryTime) {
            this.id = requireText(id, "ticket id");
            this.vehicle = requireNonNull(vehicle, "vehicle");
            this.floorId = requireText(floorId, "floor id");
            this.spot = requireNonNull(spot, "spot");
            this.entryTime = requireNonNull(entryTime, "entry time");
        }

        public String id() { return id; }
        public Vehicle vehicle() { return vehicle; }
        public String floorId() { return floorId; }
        public String spotId() { return spot.id(); }
        public Instant entryTime() { return entryTime; }
        public TicketStatus status() { return status; }
        public Instant exitTime() { return exitTime; }

        private void markPaid(Instant paidAt) {
            if (status != TicketStatus.ACTIVE) {
                throw new ParkingException(
                        ErrorCode.TICKET_NOT_ACTIVE, "Ticket is not active: " + id);
            }
            if (paidAt.isBefore(entryTime)) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Exit cannot precede entry");
            }
            status = TicketStatus.PAID;
            exitTime = paidAt;
        }
    }

    public static final class Payment {
        private final String id;
        private final String ticketId;
        private final long amountMinor;
        private final Instant createdAt;
        private PaymentStatus status = PaymentStatus.PENDING;

        private Payment(String id, String ticketId, long amountMinor, Instant createdAt) {
            if (amountMinor < 0) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Payment amount cannot be negative");
            }
            this.id = requireText(id, "payment id");
            this.ticketId = requireText(ticketId, "ticket id");
            this.amountMinor = amountMinor;
            this.createdAt = requireNonNull(createdAt, "payment time");
        }

        public String id() { return id; }
        public String ticketId() { return ticketId; }
        public long amountMinor() { return amountMinor; }
        public Instant createdAt() { return createdAt; }
        public PaymentStatus status() { return status; }
        private void succeed() { transition(PaymentStatus.SUCCESS); }
        private void fail() { transition(PaymentStatus.FAILED); }

        private void transition(PaymentStatus target) {
            if (status != PaymentStatus.PENDING) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Payment is already final: " + id);
            }
            status = target;
        }
    }

    @FunctionalInterface
    public interface FeeStrategy {
        long calculate(Ticket ticket, Instant exitTime);
    }

    public static final class HourlyFeeStrategy implements FeeStrategy {
        private final long ratePerHourMinor;

        public HourlyFeeStrategy(long ratePerHourMinor) {
            if (ratePerHourMinor < 0) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Hourly rate cannot be negative");
            }
            this.ratePerHourMinor = ratePerHourMinor;
        }

        @Override
        public long calculate(Ticket ticket, Instant exitTime) {
            long seconds = Duration.between(ticket.entryTime(), exitTime).getSeconds();
            if (seconds < 0) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Exit cannot precede entry");
            }
            long billableHours = Math.max(1, Math.addExact(seconds, 3_599) / 3_600);
            return Math.multiplyExact(billableHours, ratePerHourMinor);
        }
    }

    public static final class FlatFeeStrategy implements FeeStrategy {
        private final long amountMinor;

        public FlatFeeStrategy(long amountMinor) {
            if (amountMinor < 0) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR, "Flat fee cannot be negative");
            }
            this.amountMinor = amountMinor;
        }

        @Override
        public long calculate(Ticket ticket, Instant exitTime) {
            return amountMinor;
        }
    }

    @FunctionalInterface
    public interface PaymentProcessor {
        boolean capture(Payment payment);
    }

    public static final PaymentProcessor ALWAYS_SUCCEEDS = payment -> true;

    public static final class EntryGate {
        private final String id;
        private final ParkingLot parkingLot;

        public EntryGate(String id, ParkingLot parkingLot) {
            this.id = requireText(id, "entry gate id");
            this.parkingLot = requireNonNull(parkingLot, "parking lot");
        }

        public String id() { return id; }
        public Ticket enter(Vehicle vehicle) { return parkingLot.park(vehicle); }
    }

    public static final class ExitGate {
        private final String id;
        private final ParkingLot parkingLot;

        public ExitGate(String id, ParkingLot parkingLot) {
            this.id = requireText(id, "exit gate id");
            this.parkingLot = requireNonNull(parkingLot, "parking lot");
        }

        public String id() { return id; }
        public Payment exit(String ticketId) { return parkingLot.payAndExit(ticketId); }
    }

    private final String name;
    private final Clock clock;
    private final ParkingSpotStrategy allocationStrategy;
    private final SpotCompatibility compatibility;
    private final FeeStrategy feeStrategy;
    private final PaymentProcessor paymentProcessor;
    private final List<ParkingFloor> floors = new ArrayList<>();
    private final Map<String, ParkingFloor> floorsById = new LinkedHashMap<>();
    private final Map<String, Ticket> activeTicketsById = new LinkedHashMap<>();
    private final Map<String, String> ticketIdByVehicle = new LinkedHashMap<>();
    private final ReentrantLock lifecycleLock = new ReentrantLock(true);

    public ParkingLot(
            String name,
            Clock clock,
            ParkingSpotStrategy allocationStrategy,
            SpotCompatibility compatibility,
            FeeStrategy feeStrategy,
            PaymentProcessor paymentProcessor) {
        this.name = requireText(name, "parking lot name");
        this.clock = requireNonNull(clock, "clock");
        this.allocationStrategy = requireNonNull(allocationStrategy, "allocation strategy");
        this.compatibility = requireNonNull(compatibility, "compatibility");
        this.feeStrategy = requireNonNull(feeStrategy, "fee strategy");
        this.paymentProcessor = requireNonNull(paymentProcessor, "payment processor");
    }

    public String name() { return name; }

    public void addFloor(ParkingFloor floor) {
        requireNonNull(floor, "floor");
        lifecycleLock.lock();
        try {
            if (!activeTicketsById.isEmpty()) {
                throw new ParkingException(
                        ErrorCode.VALIDATION_ERROR,
                        "Facility configuration cannot change while vehicles are parked");
            }
            if (floorsById.containsKey(floor.id())) {
                throw new ParkingException(
                        ErrorCode.DUPLICATE_FLOOR, "Duplicate floor: " + floor.id());
            }
            for (ParkingSpot candidate : floor.spots()) {
                boolean duplicate = floors.stream()
                        .flatMap(existing -> existing.spots().stream())
                        .anyMatch(existing -> existing.id().equals(candidate.id()));
                if (duplicate) {
                    throw new ParkingException(
                            ErrorCode.DUPLICATE_SPOT, "Duplicate spot in lot: " + candidate.id());
                }
            }
            floors.add(floor);
            floorsById.put(floor.id(), floor);
        } finally {
            lifecycleLock.unlock();
        }
    }

    public Ticket park(Vehicle vehicle) {
        requireNonNull(vehicle, "vehicle");
        lifecycleLock.lock();
        try {
            if (ticketIdByVehicle.containsKey(vehicle.registration())) {
                throw new ParkingException(
                        ErrorCode.VEHICLE_ALREADY_PARKED,
                        "Vehicle already has an active ticket: " + vehicle.registration());
            }
            Allocation allocation =
                    allocationStrategy.allocate(List.copyOf(floors), vehicle, compatibility);
            ParkingSpot spot = allocation.spot();
            spot.occupy(vehicle, compatibility);
            Ticket ticket = new Ticket(
                    UUID.randomUUID().toString(),
                    vehicle,
                    allocation.floor().id(),
                    spot,
                    clock.instant());
            activeTicketsById.put(ticket.id(), ticket);
            ticketIdByVehicle.put(vehicle.registration(), ticket.id());
            return ticket;
        } finally {
            lifecycleLock.unlock();
        }
    }

    public Payment payAndExit(String ticketId) {
        ticketId = requireText(ticketId, "ticket id");
        lifecycleLock.lock();
        try {
            Ticket ticket = activeTicketsById.get(ticketId);
            if (ticket == null) {
                throw new ParkingException(
                        ErrorCode.TICKET_NOT_FOUND, "Active ticket not found: " + ticketId);
            }
            if (ticket.status() != TicketStatus.ACTIVE) {
                throw new ParkingException(
                        ErrorCode.TICKET_NOT_ACTIVE, "Ticket is not active: " + ticketId);
            }

            Instant exitTime = clock.instant();
            long amount = feeStrategy.calculate(ticket, exitTime);
            Payment payment =
                    new Payment(UUID.randomUUID().toString(), ticket.id(), amount, exitTime);
            if (!paymentProcessor.capture(payment)) {
                payment.fail();
                throw new ParkingException(
                        ErrorCode.PAYMENT_FAILED, "Payment failed for ticket " + ticket.id());
            }

            payment.succeed();
            ticket.markPaid(exitTime);
            ParkingFloor floor = floorsById.get(ticket.floorId());
            ticket.spot.release(ticket.vehicle());
            floor.returnToPool(ticket.spot);
            activeTicketsById.remove(ticket.id());
            ticketIdByVehicle.remove(ticket.vehicle().registration());
            return payment;
        } finally {
            lifecycleLock.unlock();
        }
    }

    public Map<String, Map<SpotType, Integer>> availability() {
        lifecycleLock.lock();
        try {
            Map<String, Map<SpotType, Integer>> snapshot = new LinkedHashMap<>();
            for (ParkingFloor floor : floors) {
                EnumMap<SpotType, Integer> counts = new EnumMap<>(SpotType.class);
                for (SpotType type : SpotType.values()) {
                    counts.put(type, floor.availableCount(type));
                }
                snapshot.put(floor.id(), Collections.unmodifiableMap(counts));
            }
            return Collections.unmodifiableMap(snapshot);
        } finally {
            lifecycleLock.unlock();
        }
    }

    public int activeTicketCount() {
        lifecycleLock.lock();
        try {
            return activeTicketsById.size();
        } finally {
            lifecycleLock.unlock();
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ParkingException(
                    ErrorCode.VALIDATION_ERROR, field + " must not be blank");
        }
        return value.trim();
    }

    private static <T> T requireNonNull(T value, String field) {
        if (value == null) {
            throw new ParkingException(
                    ErrorCode.VALIDATION_ERROR, field + " must not be null");
        }
        return value;
    }

    private static void check(boolean condition, String message) {
        assert condition : message;
        if (!condition) {
            throw new IllegalStateException("Self-test failed: " + message);
        }
    }

    private static void expect(ErrorCode expected, Runnable action) {
        try {
            action.run();
            throw new IllegalStateException("Expected error " + expected);
        } catch (ParkingException exception) {
            check(exception.code() == expected,
                    "expected " + expected + " but got " + exception.code());
        }
    }

    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override public ZoneId getZone() { return zone; }
        @Override public Clock withZone(ZoneId newZone) {
            return new MutableClock(instant, newZone);
        }
        @Override public Instant instant() { return instant; }
        private void advance(Duration duration) { instant = instant.plus(duration); }
    }

    public static void main(String[] args) throws Exception {
        MutableClock clock =
                new MutableClock(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);
        ParkingLot lot = new ParkingLot(
                "Phoenix Mall",
                clock,
                new LowestFloorFirstStrategy(),
                EXACT_TYPE,
                new HourlyFeeStrategy(2_000),
                ALWAYS_SUCCEEDS);
        ParkingFloor floor = new ParkingFloor("F1");
        floor.addSpot(new ParkingSpot("B1", SpotType.BIKE));
        floor.addSpot(new ParkingSpot("C1", SpotType.CAR));
        floor.addSpot(new ParkingSpot("T1", SpotType.TRUCK));
        lot.addFloor(floor);

        EntryGate entry = new EntryGate("ENTRY-1", lot);
        ExitGate exit = new ExitGate("EXIT-1", lot);
        Vehicle car = new Vehicle("ka01ab1234", VehicleType.CAR);
        Ticket ticket = entry.enter(car);
        check(ticket.status() == TicketStatus.ACTIVE, "ticket starts active");
        check("C1".equals(ticket.spotId()), "car gets compatible car spot");
        check(lot.availability().get("F1").get(SpotType.CAR) == 0, "pool decremented");
        expect(ErrorCode.VEHICLE_ALREADY_PARKED, () -> entry.enter(car));
        expect(ErrorCode.NO_SPOT_AVAILABLE,
                () -> entry.enter(new Vehicle("KA02ZZ0001", VehicleType.CAR)));

        clock.advance(Duration.ofMinutes(90));
        Payment payment = exit.exit(ticket.id());
        check(payment.status() == PaymentStatus.SUCCESS, "payment succeeds");
        check(payment.amountMinor() == 4_000, "partial second hour rounds up");
        check(ticket.status() == TicketStatus.PAID, "ticket becomes paid");
        check(lot.activeTicketCount() == 0, "active ticket removed");
        check(lot.availability().get("F1").get(SpotType.CAR) == 1, "spot released");
        expect(ErrorCode.TICKET_NOT_FOUND, () -> exit.exit(ticket.id()));

        concurrencySelfTest();
        System.out.println("ParkingLot self-test passed");
    }

    private static void concurrencySelfTest() throws Exception {
        ParkingLot lot = new ParkingLot(
                "Concurrent Lot",
                Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC),
                new LowestFloorFirstStrategy(),
                EXACT_TYPE,
                new FlatFeeStrategy(500),
                ALWAYS_SUCCEEDS);
        ParkingFloor floor = new ParkingFloor("F1");
        floor.addSpot(new ParkingSpot("ONLY-CAR", SpotType.CAR));
        lot.addFloor(floor);

        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger parked = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            String registration = "CAR-" + i;
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    lot.park(new Vehicle(registration, VehicleType.CAR));
                    parked.incrementAndGet();
                } catch (ParkingException exception) {
                    if (exception.code() != ErrorCode.NO_SPOT_AVAILABLE) {
                        throw exception;
                    }
                    rejected.incrementAndGet();
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        executor.shutdown();
        check(parked.get() == 1, "exactly one concurrent allocation succeeds");
        check(rejected.get() == 1, "exactly one concurrent allocation is rejected");
        check(lot.activeTicketCount() == 1, "one active concurrent ticket");
        check(lot.availability().get("F1").get(SpotType.CAR) == 0, "no double allocation");
    }
}
