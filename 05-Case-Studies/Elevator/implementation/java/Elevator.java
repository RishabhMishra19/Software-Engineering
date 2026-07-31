import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;

public final class Elevator {
    public enum Direction { UP, DOWN, IDLE }
    public enum MotionState { MOVING, STOPPED }
    public enum DoorState { OPEN, CLOSED }

    public record Snapshot(
            int floor, Direction direction, MotionState motion,
            DoorState door, NavigableSet<Integer> pendingStops) {}

    private final int minimumFloor;
    private final int maximumFloor;
    private final NavigableSet<Integer> pendingStops = new TreeSet<>();
    private int currentFloor;
    private Direction direction = Direction.IDLE;
    private MotionState motion = MotionState.STOPPED;
    private DoorState door = DoorState.CLOSED;

    public Elevator(int minimumFloor, int maximumFloor, int initialFloor) {
        if (minimumFloor >= maximumFloor
                || initialFloor < minimumFloor
                || initialFloor > maximumFloor) {
            throw new IllegalArgumentException("Invalid floor range or initial floor");
        }
        this.minimumFloor = minimumFloor;
        this.maximumFloor = maximumFloor;
        this.currentFloor = initialFloor;
    }

    public void requestStop(int floor) {
        validateFloor(floor);
        pendingStops.add(floor);
    }

    public Snapshot step() {
        if (door == DoorState.OPEN) {
            closeDoor();
            chooseDirection();
            return snapshot();
        }

        if (pendingStops.remove(currentFloor)) {
            motion = MotionState.STOPPED;
            direction = Direction.IDLE;
            openDoor();
            return snapshot();
        }

        chooseDirection();
        if (direction == Direction.IDLE) {
            motion = MotionState.STOPPED;
            return snapshot();
        }

        motion = MotionState.MOVING;
        currentFloor += direction == Direction.UP ? 1 : -1;
        if (pendingStops.remove(currentFloor)) {
            motion = MotionState.STOPPED;
            direction = Direction.IDLE;
            openDoor();
        }
        return snapshot();
    }

    private void chooseDirection() {
        if (pendingStops.isEmpty()) {
            direction = Direction.IDLE;
            return;
        }
        if (direction == Direction.UP && pendingStops.higher(currentFloor) != null) {
            return;
        }
        if (direction == Direction.DOWN && pendingStops.lower(currentFloor) != null) {
            return;
        }
        Integer above = pendingStops.higher(currentFloor);
        Integer below = pendingStops.lower(currentFloor);
        if (above == null) {
            direction = Direction.DOWN;
        } else if (below == null) {
            direction = Direction.UP;
        } else {
            direction = above - currentFloor <= currentFloor - below
                    ? Direction.UP : Direction.DOWN;
        }
    }

    public void openDoor() {
        if (motion == MotionState.MOVING) {
            throw new IllegalStateException("Cannot open doors while moving");
        }
        door = DoorState.OPEN;
    }

    public void closeDoor() {
        door = DoorState.CLOSED;
    }

    public Snapshot snapshot() {
        return new Snapshot(
                currentFloor, direction, motion, door,
                Collections.unmodifiableNavigableSet(new TreeSet<>(pendingStops)));
    }

    private void validateFloor(int floor) {
        if (floor < minimumFloor || floor > maximumFloor) {
            throw new IllegalArgumentException("Floor outside served range: " + floor);
        }
    }

    public static void main(String[] args) {
        Elevator elevator = new Elevator(0, 10, 3);
        elevator.requestStop(6);
        elevator.requestStop(0);

        check(elevator.step().floor() == 4, "distance tie should establish upward travel");
        check(elevator.step().floor() == 5, "elevator should continue upward");
        Snapshot atSix = elevator.step();
        check(atSix.floor() == 6 && atSix.door() == DoorState.OPEN, "doors should open at stop");
        check(atSix.motion() == MotionState.STOPPED, "open doors require stopped motion");

        Snapshot closing = elevator.step();
        check(closing.door() == DoorState.CLOSED, "a step should close doors before travel");
        check(closing.direction() == Direction.DOWN, "scheduler should reverse toward remaining stop");
        check(elevator.step().floor() == 5, "travel should resume only after doors close");

        while (!elevator.snapshot().pendingStops().isEmpty()
                || elevator.snapshot().door() == DoorState.OPEN) {
            elevator.step();
        }
        check(elevator.snapshot().floor() == 0, "all scheduled stops should be served");
        expectFailure(() -> elevator.requestStop(11), IllegalArgumentException.class);

        Elevator safety = new Elevator(0, 5, 0);
        safety.requestStop(2);
        safety.step();
        expectFailure(safety::openDoor, IllegalStateException.class);
        System.out.println("Elevator self-tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void expectFailure(Runnable action, Class<? extends Throwable> type) {
        try {
            action.run();
            throw new AssertionError("Expected " + type.getSimpleName());
        } catch (Throwable error) {
            if (!type.isInstance(error)) {
                throw error;
            }
        }
    }
}
