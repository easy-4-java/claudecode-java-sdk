# CLI Compatibility Evidence

## Purpose

Define what evidence is required before claiming compatibility or production readiness.

## Requirements

### Requirement: Cross-branch parity

The same normalized protocol/argv fixtures MUST run against the 1.0.x, 2.0.x and 3.0.x lines on their supported Java/Jackson baselines.

### Requirement: Evidence provenance

Synthetic fixtures, fake-process tests and real-Claude-CLI captures MUST be labeled separately.

#### Scenario: Real CLI claim
- WHEN documentation states that an operation works with a Claude CLI version
- THEN evidence records the actual CLI version, platform and test result.

### Requirement: Capability uncertainty is explicit

Unknown or unverified capabilities MUST NOT default to supported.

### Requirement: Tests must not encode known data loss

A regression test MUST assert the intended preserved value rather than asserting a known broken null result.

### Requirement: Public compatibility boundary

Public API models SHOULD avoid types tied to one Jackson major version.

### Requirement: Production-ready claims require fresh validation

Build, tests, dependency/security checks, resource lifecycle checks and applicable real-CLI compatibility evidence MUST be current before a production-ready claim.
