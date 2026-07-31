import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SnakeAndLadder {
    private SnakeAndLadder() {}

    public interface Dice {
        int roll();
    }

    public static final class SequenceDice implements Dice {
        private final int sides;
        private final int[] rolls;
        private int index;

        public SequenceDice(int sides, int... rolls) {
            if (sides < 2 || rolls.length == 0) {
                throw new IllegalArgumentException("Dice needs at least two sides and one roll");
            }
            for (int roll : rolls) {
                if (roll < 1 || roll > sides) {
                    throw new IllegalArgumentException("Roll outside dice range: " + roll);
                }
            }
            this.sides = sides;
            this.rolls = rolls.clone();
        }

        @Override
        public int roll() {
            int value = rolls[index % rolls.length];
            index++;
            return value;
        }

        public int sides() {
            return sides;
        }
    }

    public record Player(String id, String name) {
        public Player {
            if (id == null || id.isBlank() || name == null || name.isBlank()) {
                throw new IllegalArgumentException("Player id and name are required");
            }
        }
    }

    public static final class Board {
        private final int lastSquare;
        private final Map<Integer, Integer> transitions;

        public Board(int lastSquare, Map<Integer, Integer> transitions) {
            if (lastSquare < 2) {
                throw new IllegalArgumentException("Board must have at least two squares");
            }
            Objects.requireNonNull(transitions, "transitions");
            Map<Integer, Integer> copy = new LinkedHashMap<>(transitions);
            for (Map.Entry<Integer, Integer> entry : copy.entrySet()) {
                int from = entry.getKey();
                int to = entry.getValue();
                if (from <= 0 || from >= lastSquare || to <= 0 || to > lastSquare || from == to) {
                    throw new IllegalArgumentException("Invalid transition: " + from + " -> " + to);
                }
            }
            for (int start : copy.keySet()) {
                detectCycle(start, copy);
            }
            this.lastSquare = lastSquare;
            this.transitions = Collections.unmodifiableMap(copy);
        }

        private static void detectCycle(int start, Map<Integer, Integer> transitions) {
            int current = start;
            Map<Integer, Boolean> visited = new LinkedHashMap<>();
            while (transitions.containsKey(current)) {
                if (visited.put(current, Boolean.TRUE) != null) {
                    throw new IllegalArgumentException("Transition cycle starts at " + start);
                }
                current = transitions.get(current);
            }
        }

        public int resolve(int square) {
            if (square < 0 || square > lastSquare) {
                throw new IllegalArgumentException("Square outside board: " + square);
            }
            int resolved = square;
            while (transitions.containsKey(resolved)) {
                resolved = transitions.get(resolved);
            }
            return resolved;
        }

        public int lastSquare() {
            return lastSquare;
        }

        public Map<Integer, Integer> transitions() {
            return transitions;
        }
    }

    public enum Status { ACTIVE, WON }

    public record TurnResult(
            Player player, int roll, int from, int landedOn, int finalPosition,
            boolean moved, Status status) {}

    public static final class Game {
        private final Board board;
        private final Dice dice;
        private final List<Player> players;
        private final Map<Player, Integer> positions = new LinkedHashMap<>();
        private int currentPlayerIndex;
        private Status status = Status.ACTIVE;
        private Player winner;

        public Game(Board board, Dice dice, List<Player> players) {
            this.board = Objects.requireNonNull(board, "board");
            this.dice = Objects.requireNonNull(dice, "dice");
            if (players == null || players.size() < 2) {
                throw new IllegalArgumentException("At least two players are required");
            }
            this.players = List.copyOf(players);
            if (this.players.stream().distinct().count() != this.players.size()) {
                throw new IllegalArgumentException("Players must be unique");
            }
            this.players.forEach(player -> positions.put(player, 0));
        }

        public TurnResult playTurn() {
            if (status != Status.ACTIVE) {
                throw new IllegalStateException("Game has already finished");
            }
            Player player = players.get(currentPlayerIndex);
            int from = positions.get(player);
            int roll = dice.roll();
            if (roll < 1) {
                throw new IllegalStateException("Dice returned an invalid roll: " + roll);
            }
            int landedOn = from + roll;
            boolean moved = landedOn <= board.lastSquare();
            int finalPosition = moved ? board.resolve(landedOn) : from;
            positions.put(player, finalPosition);
            if (finalPosition == board.lastSquare()) {
                status = Status.WON;
                winner = player;
            } else {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
            return new TurnResult(player, roll, from, landedOn, finalPosition, moved, status);
        }

        public Player currentPlayer() {
            return players.get(currentPlayerIndex);
        }

        public int positionOf(Player player) {
            Integer position = positions.get(player);
            if (position == null) {
                throw new IllegalArgumentException("Unknown player");
            }
            return position;
        }

        public Status status() {
            return status;
        }

        public Player winner() {
            return winner;
        }
    }

    public static void main(String[] args) {
        Player ada = new Player("p1", "Ada");
        Player linus = new Player("p2", "Linus");
        Board board = new Board(12, Map.of(2, 7, 8, 4, 10, 12));
        Game game = new Game(board, new SequenceDice(6, 2, 5, 1, 6, 6), List.of(ada, linus));

        check(game.playTurn().finalPosition() == 7, "ladder should be applied");
        check(game.currentPlayer().equals(linus), "turn should advance");
        check(game.playTurn().finalPosition() == 5, "normal move should be retained");
        check(game.playTurn().finalPosition() == 4, "snake should be applied");
        check(game.playTurn().finalPosition() == 11, "turns should continue deterministically");
        check(game.playTurn().status() == Status.WON, "transition to final square should win");
        check(game.winner().equals(ada), "winner should be recorded");
        expectFailure(game::playTurn, IllegalStateException.class);

        Game overshoot = new Game(
                new Board(10, Map.of()), new SequenceDice(6, 6, 1, 5), List.of(ada, linus));
        overshoot.playTurn();
        overshoot.playTurn();
        check(!overshoot.playTurn().moved(), "overshoot should not move player");
        check(overshoot.positionOf(ada) == 6, "overshoot must preserve position");
        expectFailure(() -> new Board(10, Map.of(2, 4, 4, 2)), IllegalArgumentException.class);
        System.out.println("SnakeAndLadder self-tests passed");
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
