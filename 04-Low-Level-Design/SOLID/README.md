# SOLID

## Overview

SOLID is a set of five object-oriented design principles for keeping change localized and contracts dependable. The principles are heuristics, not rules that require an interface for every class or a pattern for every variation. Apply them where the cost of change, testing, or substitution justifies the abstraction.

## Why do we need it?

Growing systems often develop classes with unrelated responsibilities, conditionals that must be edited for every feature, unsafe inheritance, oversized interfaces, and business logic coupled to infrastructure. SOLID provides vocabulary for diagnosing those pressures:

- **SRP:** changes for different reasons are mixed together.
- **OCP:** adding a supported variation repeatedly edits stable policy.
- **LSP:** a purported subtype cannot honor its supertype's contract.
- **ISP:** clients depend on operations they do not use.
- **DIP:** high-level policy directly depends on volatile low-level details.

The goal is not the maximum number of classes. It is high cohesion, deliberate dependencies, and change at the correct boundary.

## How does it work?

### Single Responsibility Principle (SRP)

> A module should be responsible to one actor—a cohesive reason to change.

“One responsibility” does not mean one method. A class may expose several operations that serve one business capability. Split it when unrelated stakeholders or change cycles are coupled.

```java
final class UserApplicationService {
    private final UserRepository users;
    private final WelcomeNotifier notifier;

    UserApplicationService(UserRepository users, WelcomeNotifier notifier) {
        this.users = users;
        this.notifier = notifier;
    }

    User register(Registration request) {
        User user = User.register(request);
        users.save(user);
        notifier.welcome(user);
        return user;
    }
}
```

The application service coordinates registration; persistence and notification details remain separate. Splitting every line into a service would reduce cohesion rather than improve it.

**Signals:** a class changes for unrelated business policies, owns unrelated terminology, has many unrelated dependencies, or requires broad tests for a small change.

**Mistakes:** equating SRP with “small class,” producing anemic pass-through classes, or splitting behavior that protects one invariant.

### Open/Closed Principle (OCP)

> Software entities should be open for extension but closed for modification at a chosen axis of change.

Identify a variation that is genuinely expected, define a stable contract, and add implementations without rewriting the consuming policy.

```java
interface FarePolicy {
    Money calculate(Ride ride);
}

final class FareCalculator {
    private final FarePolicy policy;

    FareCalculator(FarePolicy policy) {
        this.policy = policy;
    }

    Money calculate(Ride ride) {
        return policy.calculate(ride);
    }
}
```

Adding surge or membership pricing adds a policy. Some wiring or registration must still change; OCP does not mean that no code can ever be modified.

**Signals:** a stable switch grows whenever a provider, algorithm, format, or rule is added.

**Mistakes:** predicting every possible variation, hiding a switch inside a “factory” without improving extensibility, or introducing polymorphism for a closed two-case rule.

### Liskov Substitution Principle (LSP)

> Any value of a subtype must be usable wherever its supertype is expected without violating the client's reasonable assumptions.

LSP is behavioral, not merely syntactic. A subtype must preserve:

- accepted inputs—do not strengthen preconditions;
- promised outputs—do not weaken postconditions;
- invariants and observable side effects;
- failure semantics and history constraints relevant to the contract.

```java
interface PaymentGateway {
    Receipt charge(Charge request) throws PaymentDeclined;
}
```

A gateway that always throws `UnsupportedOperationException`, silently ignores the charge, or returns success before durable acceptance is not substitutable. If some providers cannot refund, separate `ChargeGateway` and `RefundGateway` capabilities rather than supplying fake methods.

**Signals:** subtype-specific checks, overridden methods that reject ordinary parent inputs, no-op implementations, or unsupported operations.

**Mistakes:** treating “it compiles” as substitutability or using inheritance only for code reuse.

### Interface Segregation Principle (ISP)

> Clients should not depend on methods they do not use.

Interfaces are client-specific contracts, not merely smaller versions of a large implementation API.

```java
interface Charger {
    Receipt charge(Charge request);
}

interface Refunder {
    RefundReceipt refund(Refund request);
}
```

A provider may implement one or both capabilities. Modern Java interfaces may contain `default`, `static`, and private methods, but instance fields are implicitly `public static final`; they do not hold per-object mutable state. An abstract class can hold instance state and constructors and is appropriate for a genuine shared implementation or invariant. Java permits implementing multiple interfaces but extending only one class.

**Signals:** blank methods, exceptions for unsupported operations, broad mocks, or clients recompiled for unrelated methods.

**Mistakes:** one interface per implementation, indiscriminately splitting every method, or choosing an interface solely because “interfaces are better.”

### Dependency Inversion Principle (DIP)

> High-level policy should not depend directly on low-level detail; both should depend on abstractions owned around the policy boundary.

```java
interface PaymentPort {
    Receipt charge(Charge request);
}

final class CheckoutService {
    private final PaymentPort payments;

    CheckoutService(PaymentPort payments) {
        this.payments = payments;
    }
}
```

The abstraction should express checkout's needs, not mirror a vendor SDK. A Stripe adapter can implement the port. Dependency injection supplies that adapter; DI is a construction technique, while DIP determines dependency direction. Depending on an interface is not sufficient if the high-level module imports and instantiates the concrete implementation or if the interface is shaped by the vendor.

**Signals:** business classes call constructors for infrastructure, tests need real networks or databases, or provider replacement changes policy code.

**Mistakes:** creating an interface for every class, service locators that hide dependencies, field injection, or assuming a DI container automatically creates good boundaries.

### How the principles reinforce each other

- SRP discovers cohesive boundaries.
- OCP introduces extension points at proven variation axes.
- LSP makes implementations safe behind those extension points.
- ISP keeps each contract aligned with its clients.
- DIP points dependencies from volatile details toward stable policy.

Patterns such as Strategy, Adapter, Factory, and Decorator can support these principles, but a pattern does not prove that a design is SOLID.

### Source-derived example clinic

The original tutorial teaches each principle through a deliberately small bad/good contrast. These examples are retained here alongside the fuller contract-oriented treatment above.

#### SRP: unrelated reasons to change

Bad:

```java
class UserService {
    void createUser() {}
    void sendEmail() {}
    void generateReport() {}
}
```

User policy, email delivery, and reporting evolve for different reasons. The tutorial separates them into:

```text
UserService
EmailService
ReportService
```

Amazon-style `OrderService`, `PaymentService`, and `NotificationService` provide the same real-world separation. The benefits are easier maintenance, focused tests, and smaller cohesive classes.

> **Professional correction:** SRP means one cohesive reason to change, not one method or universally small classes. An application service may legitimately coordinate a use case, and invariant-preserving behavior should not be split into anemic pass-through services.

#### OCP: a proven axis of variation

Bad:

```java
if (type == GO) {}
else if (type == XL) {}
else if (type == PREMIER) {}
```

If cab types form an open, behavior-rich variation, every addition edits the same conditional and risks regression. The tutorial's Uber pricing example introduces:

```text
PricingStrategy
 ├── GoPricing
 ├── XLPricing
 └── PremierPricing
```

Strategy commonly supports OCP by letting a new pricing policy implement a stable contract.

> **Professional correction:** OCP is relative to a chosen, expected axis of change. It does not prohibit all modification, and a closed enum or data table can be simpler than polymorphism.

#### LSP: child behavior must honor the parent contract

Bad:

```java
interface PaymentGateway {
    void pay();
}

class DummyGateway implements PaymentGateway {
    public void pay() {
        throw new UnsupportedOperationException();
    }
}
```

The child compiles but breaks the contract. Stripe, Razorpay, and PayPal should all work wherever a charge-capable `PaymentGateway` is expected.

> **Professional correction:** LSP is behavioral: subtypes must preserve reasonable input, output, invariant, side-effect, and failure expectations. If a provider cannot perform an operation, segregate the capability instead of supplying an implementation that always fails.

#### ISP: avoid forcing unsupported capabilities

Bad:

```java
interface Worker {
    void work();
    void eat();
    void sleep();
}

class Robot implements Worker {
    // A robot cannot meaningfully eat or sleep.
}
```

The source splits this into `Workable`, `Eatable`, and `Sleepable`. A payment-system equivalent separates `Chargeable`, `Refundable`, and `RecurringPaymentSupported`, allowing a provider to implement only what it supports. The goal is to avoid fat interfaces.

> **Professional correction:** ISP is client-specific, not “make every interface tiny.” Split contracts where clients have distinct needs; do not create one interface per implementation or fragment cohesive operations indiscriminately.

#### DIP: policy should not construct volatile details

Bad:

```java
class PaymentService {
    RazorpayGateway gateway = new RazorpayGateway();
}
```

This is tightly coupled, difficult to test, and difficult to move to another provider. The source's first improvement is:

```java
class PaymentService {
    PaymentGateway gateway;
}
```

Spring dependency injection is a familiar wiring example: an external constructor receives a gateway implementation.

> **Professional correction:** DIP is not achieved merely by naming a field with an interface. The abstraction should express the high-level policy's needs and be owned at that boundary; dependency injection is the construction technique that supplies an implementation.

### Source-derived critical learnings

- SRP reduces class complexity by keeping reasons to change cohesive.
- OCP reduces repeated edits at an intentional extension seam.
- LSP makes inheritance and interface substitution safe.
- ISP keeps client contracts focused.
- DIP reduces coupling between policy and volatile detail.
- The source identifies OCP and DIP as especially common in real projects; all five remain contextual heuristics.

### Source-derived fast revision

| Principle | Remember |
| --- | --- |
| SRP | One cohesive reason to change |
| OCP | Extend at a chosen seam without rewriting stable policy |
| LSP | A child behaves as its parent contract promises |
| ISP | Focused, client-specific interfaces |
| DIP | Policy depends on stable abstractions |

Most-asked checks:

| Question | Answer |
| --- | --- |
| Huge service class violates? | Usually SRP |
| Growing variation `if/else` violates? | Often OCP |
| Wrong or unsupported subtype violates? | LSP |
| Fat interface violates? | ISP |
| High-level policy tied to a vendor violates? | DIP |

## Advantages

- Localizes change and reduces regression risk.
- Makes dependencies and contracts easier to test.
- Supports provider and policy replacement at intentional seams.
- Improves communication during reviews and design interviews.
- Encourages cohesive modules and stable boundaries.

## Limitations

- Additional indirection, types, and wiring increase cognitive cost.
- Premature abstractions can freeze the wrong model.
- Principles can conflict locally; preserving an invariant may be more important than minimizing class size.
- Performance, security, consistency, and delivery constraints can outweigh structural elegance.
- SOLID primarily addresses module design; it does not replace domain modelling or distributed-systems reasoning.

## Real-world examples

- A checkout policy depends on `PaymentPort`; Stripe and Razorpay adapters translate vendor contracts.
- A pricing engine receives a `FarePolicy`, allowing independently tested normal, surge, and promotional policies.
- A notification provider implements `Sender`, while only providers that support delivery tracking implement a separate `DeliveryStatusReader`.
- An order aggregate keeps state-transition invariants together instead of moving each method into a separate service in the name of SRP.

## Interview Questions

1. What is a “reason to change” in SRP, and why is one-method-per-class a poor interpretation?
2. Does OCP mean existing code is never modified? Where would you place an extension boundary?
3. How can a subtype violate LSP while satisfying the language's type checker?
4. How does ISP differ from simply making every interface tiny?
5. Explain DIP versus dependency injection and inversion of control.
6. Why does “depend on interfaces” sometimes fail to achieve DIP?
7. When would an abstract class be more appropriate than an interface?
8. **Interview tip:** name the change pressure, show the smallest useful boundary, and discuss the extra indirection it costs.

## References

- [Robert C. Martin, *Design Principles and Design Patterns*](https://web.archive.org/web/20150905081103/http://www.objectmentor.com/resources/articles/Principles_and_Patterns.pdf)
- [Barbara Liskov and Jeannette Wing, *A Behavioral Notion of Subtyping*](https://www.cs.cmu.edu/~wing/publications/LiskovWing94.pdf)
- [Java Language Specification: Interfaces](https://docs.oracle.com/javase/specs/jls/se21/html/jls-9.html)
- [Martin Fowler: Inversion of Control Containers and Dependency Injection](https://martinfowler.com/articles/injection.html)
- [Related: Principles](../Principles/README.md)
- [Related: Design Patterns](../Design-Patterns/README.md)

## Provenance

- **Source-derived:** “Source-derived example clinic,” critical learnings, fast-revision card, bad/good code, real-world examples, and short interview checks derive from `01-core/solid.md`.
- **Editorial synthesis:** The repository topic template, expanded signals/mistakes, principle interaction section, consolidated tables, links, and removal of exact repeated definitions.
- **Professional correction:** Every callout explicitly labeled **Professional correction**, together with the existing actor/cohesion interpretation of SRP, axis-of-change interpretation of OCP, behavioral-contract definition of LSP, client-specific definition of ISP, and policy-owned abstraction definition of DIP. These retain the tutorial examples while correcting oversimplified rules.
