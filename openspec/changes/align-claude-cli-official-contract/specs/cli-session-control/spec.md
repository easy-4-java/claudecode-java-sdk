# CLI Session Control

## Purpose

Separate persisted-session resume from persistent in-process multi-turn control.

## Requirements

### Requirement: Session identity and execution identity

A persisted Claude session id MUST NOT be treated as the same concept as a live process/execution handle.

#### Scenario: Resume session
- WHEN a caller resumes a session id using a new CLI invocation
- THEN the SDK exposes the new execution independently from the persisted session identity.

### Requirement: Persistent streaming input is capability-gated

The SDK MUST NOT claim persistent bidirectional conversation support solely because one-shot stdin or stream-json flags exist.

#### Scenario: Unsupported persistent input
- GIVEN the runtime has only one-shot stdin support
- WHEN persistent multi-turn input is requested
- THEN the SDK reports the capability as unsupported rather than returning a fake persistent handle.

### Requirement: Host callbacks require protocol evidence

Approval, user-input, hook-control and checkpoint-control callbacks MUST remain unavailable until their target wire behavior is pinned and verified.

#### Scenario: Capability unknown
- WHEN the installed CLI/control protocol cannot prove a requested host callback
- THEN the SDK does not send guessed control messages.
