# CLI Management

## Purpose

Define typed management behavior without removing forward-compatible raw access.

## Requirements

### Requirement: Raw and typed management coexist

The SDK MAY retain raw command passthrough while typed operations define validated contracts for stable command families.

#### Scenario: Unknown future subcommand
- WHEN callers need a subcommand not yet modeled
- THEN raw execution remains available
- AND the SDK does not label that raw path as fully typed support.

### Requirement: Management commands do not inherit print assumptions

Management, interactive and long-running commands MUST NOT be forced through print-mode argument construction.

### Requirement: Destructive management is explicit

Destructive operations MUST require an explicit target/range and tests MUST use isolated state.

### Requirement: Authentication side effects are explicit

Constructing a client or reading auth status MUST NOT implicitly log in, install, update or mutate credentials.

### Requirement: Long-running services have lifecycle handles

Remote Control server, gateway and runner-style commands SHOULD expose explicit long-running lifecycle semantics rather than a completed one-shot result.
