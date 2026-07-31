# Operating System

> **Provenance:** Editorial addition.

## Overview

An operating system (OS) sits between applications and hardware. Think of it as a building manager: programs request rooms, time, storage, and equipment, while the manager allocates those shared resources and prevents one tenant from entering another tenant's space. Technically, the OS provides the processes, virtual memory, files, sockets, and permissions that applications use.

The kernel is the privileged core of the OS. Programs outside the kernel, known as user-space programs, request protected operations through system calls.

**Prerequisites:** Hardware is the physical computer. Software is the stored set of instructions. Memory holds data that running instructions need quickly; storage keeps data longer term. A central processing unit (CPU) executes instructions. No command-line or programming experience is required.

## Why do we need it?

Without an OS, each application would need device-specific code, its own resource scheduler, and its own isolation model. The OS provides:

- Safe sharing of the central processing unit (CPU), memory, storage, and devices.
- Isolation between applications and users.
- Portable interfaces over heterogeneous hardware.
- Lifecycle, security, and failure-management primitives.

## How does it work?

### Processes and threads

A process owns an address space and OS resources. Threads within that process share its memory and resources, but each thread has its own stack and execution state.

For example, a web browser may use separate processes for isolation and several threads inside each process. One thread can handle the interface while another waits for network data. If a shared thread changes memory incorrectly, the process may fail; if one isolated process fails, other browser processes may survive.

The scheduler runs threads in a repeating cycle:

1. Identify threads that are ready to run.
2. Select a thread according to the scheduling policy.
3. Give that thread CPU time.
4. Preempt it when its time slice ends or it blocks.
5. Save its state and restore another thread's state during a context switch.

### Privilege and system calls

User mode limits direct hardware access. A system call temporarily transfers control to kernel mode for operations such as file input/output (I/O), networking, memory mapping, and process creation. The kernel performs the protected work and returns control to the program.

Interrupts notify the kernel about hardware events. Traps report synchronous events caused by the current instruction, such as faults.

### Virtual memory

Virtual memory gives each process the impression that it owns a private, continuous address space. Page tables translate those virtual addresses into physical memory frames.

This translation enables paging, copy-on-write, memory-mapped files, and controlled sharing. A page fault occurs when a required mapping is absent or when an access violates permissions.

### I/O and files

File systems organize persistent bytes into files and directories while tracking metadata and permissions. Device drivers translate standard OS operations into hardware-specific commands.

Buffering, caching, and asynchronous I/O reduce how often applications must wait directly for slower hardware.

### Trade-offs

- Processes provide stronger isolation; threads make communication cheaper but increase synchronization risk.
- Smaller scheduler time slices improve responsiveness but increase context-switch overhead.
- Paging expands usable memory but excessive faults cause thrashing.
- Buffered writes improve throughput but require explicit durability boundaries.

### Edge cases and production behavior

- Available memory can appear healthy while a process is constrained by a container limit. Operators must inspect both host and workload limits.
- A file can be deleted from a directory while a process still has it open; on Unix-like systems, its storage is reclaimed only after the final open reference closes.
- A successful `write` system call may update an OS cache rather than durable storage. Applications that require durability use a synchronization operation such as `fsync`, then account for the storage device's guarantees.
- CPU usage does not reveal every bottleneck. A system may be slow because threads wait for disk, locks, memory, or network responses.
- Signals, forced termination, power loss, and kernel failure provide different cleanup opportunities. Production software must not assume its normal shutdown code always runs.

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
- Use least privilege and OS-level isolation such as users, namespaces, and containers. Least privilege means granting only the permissions required for a task.
- Observe saturation, page faults, I/O wait, run queues, and open descriptors.

## Common Mistakes

- Treating threads as isolated when they share mutable memory.
- Assuming a successful write is already durable on physical storage.
- Creating unbounded threads or processes.
- Confusing virtual memory size with resident physical memory.
- Diagnosing high load solely from CPU percentage.

## Real-world examples

- A web server accepts sockets through system calls and uses an event loop or worker threads.
- Containers isolate processes with namespaces and control resource usage with control groups (cgroups) while sharing the host kernel.
- Databases use page caches, direct I/O, locks, and `fsync` to balance performance with durability.
- Copy-on-write lets a newly forked process initially share physical pages with its parent.

## Interview Questions

1. **What is the difference between a process and a thread?** A process has an isolated address space; threads share process memory and resources while maintaining separate execution stacks.
2. **What happens during a context switch?** The OS saves one thread's execution state, selects another runnable thread, restores its state, and resumes execution.
3. **Why is virtual memory useful?** It provides isolation, a uniform address space, demand paging, controlled sharing, and mappings larger than available random-access memory (RAM).
4. **What is the difference between concurrency and parallelism?** Concurrency is managing overlapping work; parallelism is executing work simultaneously.
5. **What causes a deadlock?** Mutual exclusion, hold-and-wait, no preemption, and circular wait must all hold.
6. **What is a system call?** It is a controlled interface through which user-space code requests a privileged kernel operation.

## Interview Tips

Explain mechanisms as a sequence: application action, privilege transition, kernel work, scheduling or I/O, and return. State the performance and correctness trade-off rather than only defining terminology.

## References

- [Operating Systems: Three Easy Pieces](https://pages.cs.wisc.edu/~remzi/OSTEP/)
- [Linux kernel documentation](https://docs.kernel.org/)
- [Portable Operating System Interface (POSIX.1-2024)](https://pubs.opengroup.org/onlinepubs/9799919799/)
- [Microsoft: User mode and kernel mode](https://learn.microsoft.com/en-us/windows-hardware/drivers/gettingstarted/user-mode-and-kernel-mode)
