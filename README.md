# Software Engineering

This repository is a structured learning and reference library for software engineers. It connects computer-science foundations with backend and frontend development, system and object-oriented design, applied case studies, and production operations.

## Table of contents

- [Topics covered](#topics-covered)
- [Learning roadmaps](#learning-roadmaps)
  - [Interview preparation](#interview-preparation)
  - [Day-to-day engineering](#day-to-day-engineering)
- [Repository statistics](#repository-statistics)
- [Contributing](#contributing)
- [License](#license)

## Topics covered

- [Computer Science](01-Computer-Science/README.md) — operating systems, networking, databases, and concurrency.
- [Backend Engineering](02-Backend/README.md) — Spring, Node.js, APIs, authentication, and authorization.
- [System Design](03-System-Design/README.md) — scalability, caching, messaging, distributed systems, load balancing, and rate limiting.
- [Low-Level Design](04-Low-Level-Design/README.md) — design principles, SOLID, patterns, and UML.
- [Case Studies](05-Case-Studies/README.md) — applied designs and runnable examples for common engineering problems.
- [Frontend Engineering](06-Frontend/README.md) — browser architecture, state, performance, accessibility, security, and testing.
- [DevOps](07-DevOps/README.md) — delivery, infrastructure, observability, reliability, and operations.

## Learning roadmaps

### Interview preparation

1. Build fundamentals with [Computer Science](01-Computer-Science/README.md).
2. Learn API and service boundaries in [Backend Engineering](02-Backend/README.md).
3. Practice object modelling through [Low-Level Design](04-Low-Level-Design/README.md).
4. Study scale, reliability, and trade-offs in [System Design](03-System-Design/README.md).
5. Time-box complete solutions from [Case Studies](05-Case-Studies/README.md), then compare requirements, diagrams, code, and trade-offs.
6. Review [Frontend Engineering](06-Frontend/README.md) or [DevOps](07-DevOps/README.md) according to the target role.

### Day-to-day engineering

1. Start with the relevant [backend](02-Backend/README.md) or [frontend](06-Frontend/README.md) domain.
2. Follow dependencies into [computer-science foundations](01-Computer-Science/README.md) before choosing an implementation.
3. Use [low-level design](04-Low-Level-Design/README.md) to clarify responsibilities and interfaces.
4. Evaluate cross-service and operational effects with [system design](03-System-Design/README.md) and [DevOps](07-DevOps/README.md).
5. Consult [case studies](05-Case-Studies/README.md) for concrete flows, failure modes, and runnable reference implementations.
6. Record assumptions and trade-offs, test the smallest useful change, and update documentation with behavior.

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
