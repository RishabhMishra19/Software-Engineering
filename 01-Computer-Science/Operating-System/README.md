# Operating System

> **Provenance:** Editorial addition.

## Overview

An operating system (OS) manages hardware resources and provides abstractions—processes, virtual memory, files, sockets, and permissions—on which applications depend. The kernel executes with elevated privilege; user-space programs access protected services through system calls.

## Why do we need it?

Without an OS, each application would need device-specific code, its own resource scheduler, and its own isolation model. The OS provides:

- Safe sharing of CPU, memory, storage, and devices.
- Isolation between applications and users.
- Portable interfaces over heterogeneous hardware.
- Lifecycle, security, and failure-management primitives.

## How does it work?

### Processes and threads

A process owns an address space and operating-system resources. Threads share a process's memory and resources but have independent stacks and execution state. The scheduler selects runnable threads, preempts them, and performs context switches.

### Privilege and system calls

User mode limits direct hardware access. A system call transitions execution into kernel mode for operations such as file I/O, networking, memory mapping, and process creation. Interrupts notify the kernel of hardware events; traps handle synchronous events such as faults.

### Virtual memory

Page tables map virtual addresses to physical frames. This gives each process an isolated address space and enables paging, copy-on-write, memory-mapped files, and controlled sharing. A page fault occurs when a mapping is absent or violates access permissions.

### I/O and files

File systems organize persistent bytes into files and directories while tracking metadata and permissions. Buffering, caching, asynchronous I/O, and device drivers hide hardware latency and implementation details.

### Trade-offs

- Processes provide stronger isolation; threads make communication cheaper but increase synchronization risk.
- Smaller scheduler time slices improve responsiveness but increase context-switch overhead.
- Paging expands usable memory but excessive faults cause thrashing.
- Buffered writes improve throughput but require explicit durability boundaries.

## Advantages

- Resource isolation and access control.
- Hardware abstraction and application portability.
- Concurrent execution and fair resource allocation.
- Standard interfaces for storage, networking, and devices.
- Centralized observability and lifecycle management.

## Limitations

- Scheduling, system calls, and context switches add overhead.
- Kernel defects can affect the entire machine.
- Resource exhaustion can still degrade otherwise isolated workloads.
- Filesystem caches and delayed writes complicate durability reasoning.
- OS behavior and APIs vary across platforms.

## Best Practices

- Bound CPU, memory, file-descriptor, and process usage.
- Prefer asynchronous or batched I/O only after measuring blocking time.
- Handle signals and shutdown gracefully; release resources deterministically.
- Use least privilege and OS-level isolation such as users, namespaces, and containers.
- Observe saturation, page faults, I/O wait, run queues, and open descriptors.

## Common Mistakes

- Treating threads as isolated when they share mutable memory.
- Assuming a successful write is already durable on physical storage.
- Creating unbounded threads or processes.
- Confusing virtual memory size with resident physical memory.
- Diagnosing high load solely from CPU percentage.

## Real-world examples

- A web server accepts sockets through system calls and uses an event loop or worker threads.
- Containers isolate processes with namespaces and control resource usage with cgroups while sharing the host kernel.
- Databases use page caches, direct I/O, locks, and `fsync` to balance performance with durability.
- Copy-on-write lets a newly forked process initially share physical pages with its parent.

## Interview Questions

1. **What is the difference between a process and a thread?** A process has an isolated address space; threads share process memory and resources while maintaining separate execution stacks.
2. **What happens during a context switch?** The OS saves one thread's execution state, selects another runnable thread, restores its state, and resumes execution.
3. **Why is virtual memory useful?** It provides isolation, a uniform address space, demand paging, controlled sharing, and mappings larger than available RAM.
4. **What is the difference between concurrency and parallelism?** Concurrency is managing overlapping work; parallelism is executing work simultaneously.
5. **What causes a deadlock?** Mutual exclusion, hold-and-wait, no preemption, and circular wait must all hold.
6. **What is a system call?** It is a controlled interface through which user-space code requests a privileged kernel operation.

## Interview Tips

Explain mechanisms as a sequence: application action, privilege transition, kernel work, scheduling or I/O, and return. State the performance and correctness trade-off rather than only defining terminology.

## References

- [Operating Systems: Three Easy Pieces](https://pages.cs.wisc.edu/~remzi/OSTEP/)
- [Linux kernel documentation](https://docs.kernel.org/)
- [POSIX.1-2024](https://pubs.opengroup.org/onlinepubs/9799919799/)
- [Microsoft: User mode and kernel mode](https://learn.microsoft.com/en-us/windows-hardware/drivers/gettingstarted/user-mode-and-kernel-mode)
