# CLI Command Contract

## Purpose

Define safe and deterministic construction of Claude CLI invocations.

## Requirements

### Requirement: Command intent validation

The SDK MUST classify the intended invocation before spawning a process and MUST reject confirmed-invalid option combinations.

#### Scenario: Print combined with background
- GIVEN a request using print mode and background mode
- WHEN the SDK validates the request
- THEN validation fails before process spawn
- AND the error identifies the conflicting options.

#### Scenario: Supported cloud/session operation
- GIVEN an operation documented as valid for the pinned CLI version
- WHEN the SDK validates it
- THEN it is not rejected merely because it contains cloud-related options.

### Requirement: Deterministic default merge

High-level APIs SHALL merge SDK defaults, client defaults and explicit request overrides in that order.

#### Scenario: Model-only override
- GIVEN client-level permission, tools and budget defaults
- WHEN a call overrides only the model
- THEN the security and budget defaults remain effective.

#### Scenario: Explicit false
- GIVEN a client default whose effective value is true
- WHEN the request explicitly supplies false
- THEN false is preserved and not interpreted as unset.

### Requirement: Argument boundary preservation

Ordinary CLI arguments MUST be passed as argv boundaries without shell interpolation.

#### Scenario: Prompt contains spaces and Unicode
- WHEN the prompt is executed
- THEN it reaches the child process as one argument with unchanged content.
