# CLI Process Runtime

## Purpose

Define bounded, cancellable and observable child-process execution.

## Requirements

### Requirement: Real-time output

The SDK SHALL be able to deliver stdout events while the Claude process is still running.

#### Scenario: Child waits after first event
- GIVEN a child emits event A and waits
- WHEN A is received
- THEN the child is still running
- AND the caller does not need to wait for process exit.

### Requirement: Bounded buffering

Queues and retained output MUST have explicit bounds.

#### Scenario: Slow consumer
- GIVEN output arrives faster than the consumer can process it
- WHEN a configured bound is reached
- THEN the runtime applies its documented backpressure/overflow policy
- AND does not silently drop a required terminal result.

### Requirement: Per-run execution context

Working directory and environment MUST be snapshotted per run.

#### Scenario: Concurrent runs
- GIVEN two executions use different cwd/environment values
- WHEN they overlap
- THEN neither run observes the other's execution context.

### Requirement: Timeout and cancellation

Probe, run and drain lifecycles MUST be independently controllable where their semantics differ.

#### Scenario: Cancellation races normal exit
- WHEN cancellation and normal exit occur concurrently
- THEN exactly one terminal state is published
- AND close remains idempotent.

### Requirement: Owned-resource cleanup

The SDK MUST clean up resources it owns and MUST NOT claim cleanup of unrelated processes.
