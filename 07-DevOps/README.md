# DevOps

> **Provenance:** Editorial addition.

## Overview

Development and operations (DevOps) is a set of technical and organizational practices for moving a reviewed change into reliable production operation. It connects version control, continuous integration (CI), continuous delivery (CD), infrastructure as code, observability, security, incident response, and feedback loops. Continuous integration and continuous delivery are often grouped as CI/CD.

DevOps is not merely a team name or a collection of tools. It defines how people build, release, operate, and learn from software together.

## Beginner mental models

### Continuous integration and continuous delivery

- **Everyday mental model:** Continuous integration and continuous delivery (CI/CD) is an automated assembly line. Each change is inspected, tested, packaged, and kept ready to release instead of relying on a large manual event.
- **Deeper mechanism:** Continuous integration frequently combines changes and runs repeatable checks. Continuous delivery produces a verified artifact and promotes that same artifact through environments; continuous deployment goes one step further and releases every passing change automatically.
- **Example:** A code change starts tests, security checks, and a build. The resulting package is deployed to a test environment, verified, approved, and then gradually released to users.
- **Edge cases:** Passing tests may miss production data or configuration, two independently safe changes may conflict, and a rollback may not reverse data loss.
- **Production trade-offs:** Faster feedback and smaller releases reduce change risk, but weak checks automate mistakes. Stronger gates improve confidence while adding time, cost, and maintenance.

### Infrastructure as code

- **Everyday mental model:** Infrastructure as code (IaC) is a version-controlled blueprint for servers, networks, permissions, and managed services.
- **Deeper mechanism:** A tool compares declared desired state with actual infrastructure, creates a change plan, and uses provider interfaces to add, update, or remove resources. Reviews and history make changes reproducible and auditable.
- **Example:** A reviewed file declares a network, database, and three application instances, so test and production environments can be created consistently.
- **Edge cases:** Manual changes cause drift, shared state can be corrupted, secrets can leak into files or logs, and an apparently small declaration can delete a critical resource.
- **Production trade-offs:** IaC improves repeatability and review, but introduces tool state, provider dependencies, access risk, and the need for guarded plans, backups, and recovery procedures.

### Container

- **Everyday mental model:** A container is a standardized package containing an application and the user-space files it needs, so it runs consistently in different places.
- **Deeper mechanism:** A container image provides read-only filesystem layers and startup instructions. At runtime, operating-system isolation limits the process's view of files, networking, and resources; containers usually share the host's kernel and are not complete virtual machines.
- **Example:** The same signed web-service image runs in testing and production with environment-specific configuration supplied at startup.
- **Edge cases:** Images can contain vulnerabilities, writable data disappears when a container is replaced, resource limits can terminate a process, and shared-kernel isolation has a different security boundary from a virtual machine.
- **Production trade-offs:** Containers improve portability, density, and repeatable releases, but require image patching, registries, runtime security, external persistent storage, and operational tooling.

### Orchestration

- **Everyday mental model:** Orchestration is the dispatcher for many containers: it decides where they run, replaces unhealthy copies, and coordinates updates.
- **Deeper mechanism:** An orchestrator continuously compares desired and actual state, schedules workloads on available machines, routes service traffic, checks health, scales replicas, and rolls out configuration or image changes.
- **Example:** During a release, the orchestrator starts new service copies, waits for health checks, shifts traffic, and removes old copies.
- **Edge cases:** A faulty health check can restart healthy work, a regional capacity shortage can prevent scheduling, and old and new versions may communicate during a rolling release.
- **Production trade-offs:** Orchestration improves automation and resilience at scale, but adds control-plane complexity, platform cost, specialized skills, and failure modes that small systems may not need.

### Telemetry

- **Everyday mental model:** Telemetry is the evidence a running system sends about what happened, much like dashboard instruments and a flight recorder.
- **Deeper mechanism:** Logs record events, metrics aggregate numeric measurements over time, traces connect work across service boundaries, and profiles show where computing resources are spent. Together they support observability: the ability to investigate internal behavior from external evidence.
- **Example:** A slow checkout trace identifies a delayed payment call, its metrics show the scope, and correlated logs explain the rejected requests.
- **Edge cases:** Missing context prevents correlation, sampling can omit a rare failure, excessive labels create high cost, and telemetry can expose personal or secret data.
- **Production trade-offs:** More telemetry speeds diagnosis and supports reliability decisions, but increases storage, processing, network, privacy, and on-call attention costs.

### Service-level objective

- **Everyday mental model:** A service-level objective (SLO) is a reliability promise the team sets for itself, such as “99.9 percent of checkout requests succeed each month.”
- **Deeper mechanism:** A service-level indicator (SLI) measures user-visible behavior. The SLO sets its target over a time window, and the error budget is the allowed gap from perfection. Burn rate shows how quickly that budget is being consumed.
- **Example:** If the success SLO is 99.9 percent, up to 0.1 percent of requests may fail within the chosen window before the objective is missed.
- **Edge cases:** Averages can hide a harmed customer group, a poorly chosen indicator can look healthy while users suffer, and a target without an action policy changes no behavior.
- **Production trade-offs:** A stricter SLO can protect users but costs more capacity and engineering time. A looser SLO permits faster change but accepts more unreliability; the target should reflect user need and business impact.

## Why do we need it?

Manual, infrequent releases accumulate risk and make failures difficult to reproduce. Small, automated, and observable changes can improve deployment frequency and recovery while preserving controls.

Feedback from production operations also exposes architectural costs that design diagrams may omit.

## How does it work?

A typical delivery flow is:

1. Commit a reviewed change to version control.
2. Create a reproducible build.
3. Run fast tests, static analysis, and security checks.
4. Produce an immutable, signed artifact.
5. Deploy the artifact to a staged environment.
6. Verify health and service-level objectives (SLOs), which are reliability targets for user-visible behavior.
7. Roll out progressively to production.
8. Halt automatically or roll back if the verification gates fail.

Infrastructure as code makes environments reviewable and repeatable. Containers package processes. Orchestration also manages scheduling, service discovery, rollout, secrets, and health. GitOps—a practice that manages systems from version-controlled desired state—uses automated reconciliation, but teams must still validate dangerous changes.

Operate services with logs, metrics, traces, and profiles tied to service-level indicators (SLIs), which are measurements of user-visible reliability. Alert on user-visible symptoms and resource saturation that requires action. During incidents, use runbooks, clear ownership, incident command, and blameless reviews. Use error budgets—the permitted amount of unreliability—to balance reliability work with release velocity.

**Production failure modes and practices**

- A green pipeline misses production configuration or data behavior; test migrations, environment contracts, and representative traffic.
- Rolling releases can break when old and new versions interact. Make application programming interfaces (APIs), events, and database changes backward-compatible by using the expand/migrate/contract sequence.
- Rollback cannot undo a destructive schema change; rehearse recovery and use backups with restore tests.
- Configuration drift and manually patched servers destroy reproducibility; reconcile declared state and audit emergency changes.
- Alert floods hide the incident; deduplicate, route by ownership, and page only for urgent action.
- Supply-chain compromise can enter through dependencies or CI credentials. Pin and scan dependencies, generate software bills of materials (SBOMs), sign artifacts, use least privilege, and rotate secrets.
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

- **Trunk-based development** means developers merge small changes into one
  shared main branch frequently instead of maintaining long-lived branches.
  A **canary rollout** sends a small percentage of production traffic to a new
  version before wider release. An **ephemeral environment** is a temporary
  test environment created for a change and removed afterward.
- A service combines trunk-based development, ephemeral test environments,
  canary rollout, SLO gates, and automatic rollback.
- A database migration adds a nullable field, deploys dual-read/write compatibility, backfills, then removes the old field later.
- An incident begins from a burn-rate alert, uses traces to isolate a dependency, sheds optional load, and produces tracked follow-up work.

## Interview Questions

1. **Continuous delivery versus continuous deployment?**
   - Continuous delivery keeps every validated change ready for production, but a person or business process may approve the release.
   - Continuous deployment automatically releases every change that passes the pipeline.
   - Both require reproducible builds, automated checks, observability, and a safe recovery path.
2. **How do canary, blue-green, and rolling deployments trade cost and risk?**
   - Canary deployment exposes a small traffic share first. It limits impact but requires traffic control and reliable comparison signals.
   - Blue-green deployment keeps old and new environments available. It enables fast switching but costs more and still needs data compatibility.
   - Rolling deployment replaces instances gradually. It uses less extra capacity but old and new versions coexist, so interfaces must remain compatible.
3. **How would you deploy a backward-incompatible database change safely?**
   - Expand the schema with a backward-compatible addition.
   - Deploy code that can work with both old and new representations.
   - Migrate or backfill data while monitoring correctness and load.
   - Switch reads and writes to the new representation.
   - Remove the old representation only after rollback is no longer needed.
4. **What should page an engineer, and how do SLOs and error budgets help?**
   - Page only for urgent, user-visible problems that need immediate human action.
   - SLOs define the reliability target, while burn-rate alerts show how quickly an incident is consuming the error budget.
   - The remaining error budget helps teams decide whether to continue releases or prioritize reliability work.
5. **Interview tip:** walk from commit to recovery, including artifact integrity, rollout gates, observability, failure containment, and data rollback limits.

### Key-point interview answers

- **What is CI/CD?** It frequently integrates changes, validates them automatically, and keeps one reproducible artifact ready for controlled delivery; deployment may still require approval.
- **What is IaC?** Infrastructure as code declares infrastructure in reviewed, version-controlled files so environments can be planned, reproduced, reconciled, and audited.
- **What is a container?** It is an image-based package run as an isolated process that usually shares the host operating-system kernel.
- **What is orchestration?** It continuously schedules, replaces, scales, connects, and updates many workloads toward a declared desired state.
- **What is telemetry?** Logs, metrics, traces, and profiles provide evidence for understanding production behavior while requiring cost, privacy, and sampling controls.
- **What is an SLO?** It is a measurable reliability target based on a user-visible indicator; its error budget guides release and reliability decisions.

## References

- [Google Site Reliability Engineering](https://sre.google/books/)
- [DevOps Research and Assessment (DORA) Research Program](https://dora.dev/research/)
- [United States National Institute of Standards and Technology (NIST) Secure Software Development Framework](https://csrc.nist.gov/Projects/ssdf)
- [OpenTelemetry Documentation](https://opentelemetry.io/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/home/)
- [Related: Distributed Systems](../03-System-Design/Distributed-Systems/README.md)
