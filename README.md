# Software Engineering

This repository is a structured learning and reference library for software engineers. It explains how computer-science foundations connect to backend and frontend development, system and object-oriented design, applied case studies, and production operations.

**Migration provenance:** This handbook consolidates material from [Learning-Engineering](https://github.com/RishabhMishra19/Learning-Engineering) and the low-level design (LLD) repository [Learning-LLD](https://github.com/RishabhMishra19/Learning-LLD). Migrated, newly authored, and professionally corrected content is identified according to the [contribution policy](CONTRIBUTING.md#provenance-policy).

## A beginner's mental model

- **Everyday mental model:** Software engineering is the work of turning a human need into instructions a computer can run, then keeping those instructions understandable, safe, and useful as people and conditions change.
- **Deeper mechanism:** A user-facing application commonly has a frontend that handles interaction, a backend that applies rules, databases that preserve information, networks that connect parts, and operational systems that build, release, and monitor the result.
- **Example:** In an online shop, the frontend displays a cart, the backend verifies prices and stock, a database saves the order, and delivery automation releases updates while monitoring failures.
- **Edge cases:** Networks fail, users submit unexpected input, two updates happen at once, devices differ, dependencies become unavailable, and a safe code change can still conflict with real production data.
- **Production trade-offs:** There is rarely one universally best design. Engineers balance correctness, speed, simplicity, security, accessibility, reliability, cost, and the ability to change the system later.

Each topic uses a layered reading path: begin with the everyday mental model, continue into the mechanism, test the idea with an example, examine edge cases, and finish with production trade-offs. For interview preparation, answer with the key point first and then justify it with constraints and failure modes.

## Table of contents

- [A beginner's mental model](#a-beginners-mental-model)
- [Topics covered](#topics-covered)
- [Learning roadmaps](#learning-roadmaps)
  - [Interview preparation](#interview-preparation)
  - [Day-to-day engineering](#day-to-day-engineering)
  - [Key-point interview answer](#key-point-interview-answer)
- [Repository statistics](#repository-statistics)
- [Contributing](#contributing)
- [License](#license)

## Topics covered

- [Computer Science](01-Computer-Science/README.md) — operating systems, networking, databases, and concurrency.
- [Backend Engineering](02-Backend/README.md) — Spring, Node.js, application programming interfaces (APIs), authentication, and authorization.
- [System Design](03-System-Design/README.md) — scalability, caching, messaging, distributed systems, load balancing, and rate limiting.
- [Low-Level Design](04-Low-Level-Design/README.md) — design principles; SOLID, a mnemonic for the single-responsibility, open–closed, Liskov-substitution, interface-segregation, and dependency-inversion principles; patterns; and Unified Modeling Language (UML).
- [Case Studies](05-Case-Studies/README.md) — applied designs and runnable examples for common engineering problems.
- [Frontend Engineering](06-Frontend/README.md) — browser architecture, state, performance, accessibility, security, and testing.
- [Development and operations (DevOps)](07-DevOps/README.md) — delivery, infrastructure, observability, reliability, and operations.

## Learning roadmaps

### Interview preparation

Follow these steps in order:

1. Build the fundamentals with [Computer Science](01-Computer-Science/README.md).
2. Learn API and service boundaries in [Backend Engineering](02-Backend/README.md).
3. Practice object modeling through [Low-Level Design](04-Low-Level-Design/README.md).
4. Study scale, reliability, and trade-offs in [System Design](03-System-Design/README.md).
5. Set a fixed time limit and complete solutions from [Case Studies](05-Case-Studies/README.md). Then compare your requirements, diagrams, code, and trade-offs with the references.
6. Review [Frontend Engineering](06-Frontend/README.md) or [DevOps](07-DevOps/README.md), depending on the target role.

### Day-to-day engineering

Use this flow when approaching an engineering task:

1. Start with the relevant [backend](02-Backend/README.md) or [frontend](06-Frontend/README.md) domain.
2. Trace its dependencies into [computer-science foundations](01-Computer-Science/README.md) before choosing an implementation.
3. Use [low-level design](04-Low-Level-Design/README.md) to clarify responsibilities and interfaces.
4. Evaluate cross-service and operational effects with [system design](03-System-Design/README.md) and [DevOps](07-DevOps/README.md).
5. Consult [case studies](05-Case-Studies/README.md) for concrete flows, failure modes, and runnable reference implementations.
6. Record assumptions and trade-offs.
7. Test the smallest useful change, then update the documentation to match the behavior.

### Key-point interview answer

**How do the learning areas fit together?** Computer science explains the foundations; frontend and backend engineering build the product; low-level and system design organize responsibilities and scale; case studies apply the ideas; and development and operations practices release and operate the result. A strong answer names the user need, system boundary, failure modes, and trade-offs before naming tools.

## Repository statistics

The initial migration contains:

- 72 tracked handbook files;
- 66 Markdown documents;
- 5 runnable Java 17 reference implementations;
- 7 learning areas and 6 case studies;
- no empty placeholders or duplicated documents.

Regenerate the counts after changing the repository:

```sh
files() { git ls-files --cached --others --exclude-standard; }
printf 'Repository files: '; files | wc -l
printf 'Markdown files: '; files | awk '/\.md$/' | wc -l
printf 'Java files: '; files | awk '/\.java$/' | wc -l
printf 'Top-level learning areas: '; files | awk -F/ '$1 ~ /^[0-9][0-9]-/ {print $1}' | sort -u | wc -l
```

## Contributing

See the [contribution guide](CONTRIBUTING.md) for content structure, naming, linking, validation, and attribution requirements.

## License

**No open-source license is granted for this repository or its contents.** Unless a separate license is added, applicable copyright law reserves all rights; viewing the repository does not imply permission to use, copy, modify, or redistribute its contents.
