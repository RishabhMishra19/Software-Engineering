# DevOps

> **Provenance:** Editorial addition.

## Overview

DevOps is a set of technical and organizational practices that shortens the path from a reviewed change to reliable production operation. It joins version control, continuous integration and delivery, infrastructure as code, observability, security, incident response, and feedback loops; it is not merely a team name or toolchain.

## Why do we need it?

Manual, infrequent releases accumulate risk and make failures difficult to reproduce. Automated, small, observable changes improve deployment frequency and recovery while preserving controls. Operations feedback also exposes architectural costs that design diagrams omit.

## How does it work?

A typical path is: commit → reproducible build → fast tests and static/security checks → immutable signed artifact → staged deployment → health/SLO verification → progressive production rollout → automated halt or rollback.

Infrastructure as code makes environments reviewable and repeatable. Containers package processes but orchestration additionally manages scheduling, service discovery, rollout, secrets, and health. GitOps reconciles declared state; it does not eliminate the need to validate dangerous changes.

Operate with logs, metrics, traces, and profiles tied to service-level indicators. Alert on user-visible symptoms and actionable saturation, then use runbooks, ownership, incident command, and blameless reviews. Use error budgets to balance reliability work with release velocity.

**Production failure modes and practices**

- A green pipeline misses production configuration or data behavior; test migrations, environment contracts, and representative traffic.
- Rolling releases break across versions; make APIs, events, and database changes backward-compatible, using expand/migrate/contract.
- Rollback cannot undo a destructive schema change; rehearse recovery and use backups with restore tests.
- Configuration drift and manually patched servers destroy reproducibility; reconcile declared state and audit emergency changes.
- Alert floods hide the incident; deduplicate, route by ownership, and page only for urgent action.
- Supply-chain compromise enters through dependencies or CI credentials; pin and scan dependencies, generate SBOMs, sign artifacts, use least privilege, and rotate secrets.
- Common mistakes: optimizing deployment frequency without SLOs, using environment-specific artifacts, exposing secrets in logs, treating dashboards as alerts, and adding retries without budgets.

## Advantages

- Smaller, repeatable releases reduce change risk.
- Faster feedback and mean time to recovery.
- Consistent environments and auditable changes.
- Reliability and security become continuous engineering concerns.

## Limitations

- Automation has maintenance and platform costs.
- A fast unsafe pipeline scales mistakes.
- Tooling cannot repair unclear ownership or incentives.
- Distributed delivery systems add credentials, dependencies, and failure modes.

## Real-world examples

- A service uses trunk-based development, ephemeral test environments, canary rollout, SLO gates, and automatic rollback.
- A database migration adds a nullable field, deploys dual-read/write compatibility, backfills, then removes the old field later.
- An incident begins from a burn-rate alert, uses traces to isolate a dependency, sheds optional load, and produces tracked follow-up work.

## Interview Questions

1. Continuous delivery versus continuous deployment?
2. How do canary, blue-green, and rolling deployments trade cost and risk?
3. How would you deploy a backward-incompatible database change safely?
4. What should page an engineer, and how do SLOs and error budgets help?
5. **Interview tip:** walk from commit to recovery, including artifact integrity, rollout gates, observability, failure containment, and data rollback limits.

## References

- [Google Site Reliability Engineering](https://sre.google/books/)
- [DORA Research Program](https://dora.dev/research/)
- [NIST Secure Software Development Framework](https://csrc.nist.gov/Projects/ssdf)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Related: Distributed Systems](../03-System-Design/Distributed-Systems/README.md)
