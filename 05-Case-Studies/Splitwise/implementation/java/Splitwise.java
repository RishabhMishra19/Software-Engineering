import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** In-memory, thread-safe reference model for a single-currency expense group. */
public final class Splitwise {
    record Money(BigDecimal value) implements Comparable<Money> {
        static final int SCALE = 2;
        static final Money ZERO = new Money(BigDecimal.ZERO.setScale(SCALE));

        Money {
            Objects.requireNonNull(value);
            value = value.setScale(SCALE, RoundingMode.UNNECESSARY);
        }

        static Money of(String value) { return new Money(new BigDecimal(value)); }
        Money add(Money other) { return new Money(value.add(other.value)); }
        Money subtract(Money other) { return new Money(value.subtract(other.value)); }
        Money negate() { return new Money(value.negate()); }
        boolean isPositive() { return value.signum() > 0; }
        boolean isNegative() { return value.signum() < 0; }
        @Override public int compareTo(Money other) { return value.compareTo(other.value); }
        @Override public String toString() { return value.toPlainString(); }
    }

    sealed interface Split permits EqualSplit, ExactSplit {
        Map<String, Money> allocate(Money total);
    }

    record EqualSplit(List<String> participants) implements Split {
        EqualSplit {
            if (participants.isEmpty() || participants.stream().distinct().count() != participants.size()) {
                throw new IllegalArgumentException("Participants must be unique and non-empty");
            }
            participants = participants.stream().sorted().toList();
        }

        @Override public Map<String, Money> allocate(Money total) {
            long cents = total.value().movePointRight(Money.SCALE).longValueExact();
            long base = cents / participants.size();
            long remainder = cents % participants.size();
            Map<String, Money> shares = new LinkedHashMap<>();
            for (int i = 0; i < participants.size(); i++) {
                long share = base + (i < remainder ? 1 : 0);
                shares.put(participants.get(i),
                        new Money(BigDecimal.valueOf(share, Money.SCALE)));
            }
            return Map.copyOf(shares);
        }
    }

    record ExactSplit(Map<String, Money> shares) implements Split {
        ExactSplit {
            if (shares.isEmpty() || shares.values().stream().anyMatch(Money::isNegative)) {
                throw new IllegalArgumentException("Exact shares must be non-negative");
            }
            shares = Map.copyOf(shares);
        }

        @Override public Map<String, Money> allocate(Money total) {
            Money sum = shares.values().stream().reduce(Money.ZERO, Money::add);
            if (sum.compareTo(total) != 0) {
                throw new IllegalArgumentException("Exact shares must equal expense total");
            }
            return shares;
        }
    }

    record Expense(String id, String payerId, Money total, Map<String, Money> shares) {
        Expense {
            shares = Map.copyOf(shares);
        }
    }

    record Settlement(String debtorId, String creditorId, Money amount) {}

    static final class ExpenseLedger {
        private final Map<String, Expense> expenses = new HashMap<>();
        // Positive means the group owes the user; negative means the user owes the group.
        private final Map<String, Money> netBalances = new HashMap<>();

        synchronized Expense addExpense(String expenseId, String payerId, Money total, Split split) {
            Objects.requireNonNull(expenseId);
            Objects.requireNonNull(payerId);
            if (!total.isPositive()) throw new IllegalArgumentException("Total must be positive");
            Map<String, Money> shares = split.allocate(total);
            if (!shares.containsKey(payerId)) {
                throw new IllegalArgumentException("Payer must participate in the expense");
            }
            Expense proposed = new Expense(expenseId, payerId, total, shares);
            Expense existing = expenses.get(expenseId);
            if (existing != null) {
                if (!existing.equals(proposed)) {
                    throw new IllegalStateException("Expense id reused with different payload");
                }
                return existing;
            }

            expenses.put(expenseId, proposed);
            netBalances.merge(payerId, total, Money::add);
            shares.forEach((user, share) -> netBalances.merge(user, share.negate(), Money::add));
            removeZeros();
            assertBalanced();
            return proposed;
        }

        synchronized Map<String, Money> balances() {
            return Map.copyOf(netBalances);
        }

        /**
         * Greedy O(u log u) simplification minimizes transfer count in many
         * practical cases, but globally minimum edge count is not guaranteed.
         */
        synchronized List<Settlement> simplify() {
            List<Map.Entry<String, Money>> debtors = netBalances.entrySet().stream()
                    .filter(e -> e.getValue().isNegative())
                    .sorted(Map.Entry.comparingByKey())
                    .map(e -> Map.entry(e.getKey(), e.getValue().negate()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            List<Map.Entry<String, Money>> creditors = netBalances.entrySet().stream()
                    .filter(e -> e.getValue().isPositive())
                    .sorted(Comparator.comparing(Map.Entry<String, Money>::getKey))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

            List<Settlement> result = new ArrayList<>();
            int debtor = 0;
            int creditor = 0;
            while (debtor < debtors.size() && creditor < creditors.size()) {
                var debt = debtors.get(debtor);
                var credit = creditors.get(creditor);
                Money transfer = debt.getValue().compareTo(credit.getValue()) <= 0
                        ? debt.getValue() : credit.getValue();
                result.add(new Settlement(debt.getKey(), credit.getKey(), transfer));
                Money debtLeft = debt.getValue().subtract(transfer);
                Money creditLeft = credit.getValue().subtract(transfer);
                debtors.set(debtor, Map.entry(debt.getKey(), debtLeft));
                creditors.set(creditor, Map.entry(credit.getKey(), creditLeft));
                if (debtLeft.compareTo(Money.ZERO) == 0) debtor++;
                if (creditLeft.compareTo(Money.ZERO) == 0) creditor++;
            }
            return List.copyOf(result);
        }

        private void removeZeros() {
            netBalances.entrySet().removeIf(e -> e.getValue().compareTo(Money.ZERO) == 0);
        }

        private void assertBalanced() {
            Money net = netBalances.values().stream().reduce(Money.ZERO, Money::add);
            if (net.compareTo(Money.ZERO) != 0) throw new IllegalStateException("Unbalanced ledger");
        }
    }

    public static void main(String[] args) {
        ExpenseLedger ledger = new ExpenseLedger();
        Expense dinner = ledger.addExpense("expense-1", "alice", Money.of("10.00"),
                new EqualSplit(List.of("alice", "bob", "carol")));
        check(dinner.shares().get("alice").equals(Money.of("3.34")), "remainder is deterministic");
        check(dinner.shares().get("bob").equals(Money.of("3.33")), "equal split preserves cents");

        Expense replay = ledger.addExpense("expense-1", "alice", Money.of("10.00"),
                new EqualSplit(List.of("carol", "alice", "bob")));
        check(replay.equals(dinner), "expense creation is idempotent");

        ledger.addExpense("expense-2", "bob", Money.of("5.00"),
                new ExactSplit(Map.of("alice", Money.of("1.25"), "bob", Money.of("3.75"))));
        Money sum = ledger.balances().values().stream().reduce(Money.ZERO, Money::add);
        check(sum.equals(Money.ZERO), "ledger remains balanced");
        Money transfers = ledger.simplify().stream().map(Settlement::amount)
                .reduce(Money.ZERO, Money::add);
        check(transfers.isPositive(), "simplification produces settlements");

        boolean conflictRejected = false;
        try {
            ledger.addExpense("expense-1", "alice", Money.of("11.00"),
                    new EqualSplit(List.of("alice", "bob", "carol")));
        } catch (IllegalStateException expected) {
            conflictRejected = true;
        }
        check(conflictRejected, "conflicting idempotency payload is rejected");
        System.out.println("Splitwise self-tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
