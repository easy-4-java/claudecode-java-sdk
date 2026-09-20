# Tasks

All implementation tasks are initially unchecked.

## P0 - Correctness

- [ ] Add failing regression tests proving final result text/cost/usage must survive stream-json parsing.
- [ ] Replace lossy `ClaudeMessage -> ClaudeResult` conversion with raw-envelope typed decoding.
- [ ] Model nested assistant/user/content events and unknown envelopes.
- [ ] Correct cache usage names to `cache_creation_input_tokens` / `cache_read_input_tokens`, retaining deliberate compatibility aliases if needed.
- [ ] Add command-intent validation; reject print + background and other confirmed-invalid combinations before spawn.
- [ ] Unify client-default merge behavior across print/json/stream convenience entry points.
- [ ] Make explicit `false` and empty-value overrides observable.
- [ ] Add golden argv tests for debug, tmux/worktree, schema and security-related options.

## P1 - Runtime

- [ ] Introduce an execution handle with event subscription/callback and cancellation.
- [ ] Stream stdout incrementally and drain stderr independently.
- [ ] Add incremental UTF-8 decoder tests, including split multibyte sequences.
- [ ] Add per-run working directory and immutable environment snapshot.
- [ ] Separate probe/start/run/drain timeout settings.
- [ ] Add bounded queues/output retention and explicit overflow policy.
- [ ] Make close/cancel idempotent and define owned-process cleanup.
- [ ] Add controlled fake-process tests for timeout, cancellation, backpressure and process exit races.
- [ ] Separate OS/transport/protocol/business termination classifications.

## P1/P2 - Typed CLI management

- [ ] Add typed structured-output request/result.
- [ ] Add typed MCP transport/scope/auth management while retaining raw passthrough.
- [ ] Add typed plugin management and plugin-eval result handling.
- [ ] Add read-only auth status plus explicit login/token setup flows.
- [ ] Add typed background/session operations and explicit destructive-operation targets.
- [ ] Separate Remote Control session options from long-running server lifecycle.
- [ ] Treat gateway/self-hosted runner as long-running or dispatch-specific operations.
- [ ] Correct misleading import API naming with a migration-compatible alias.

## Conditional P2 - Persistent control

- [ ] Pin target Claude CLI/official SDK versions and record control-protocol evidence.
- [ ] Implement persistent streaming-input handle and ordered multi-turn send.
- [ ] Implement approval/user-input callbacks only after wire behavior is verified.
- [ ] Implement programmatic hook/checkpoint control only after capability evidence exists.
- [ ] Add timeout/cancel/late-response tests for host callbacks.

## Cross-branch and release

- [ ] Keep behavior and protocol fixtures identical across 1.0.x / 2.0.x / 3.0.x.
- [ ] Maintain only JDK/Jackson/platform adapter differences.
- [ ] Run each branch on its minimum JDK/build baseline.
- [ ] Add real CLI compatibility fixtures with recorded CLI version and platform.
- [ ] Correct README statements about Maven, tests and CI where stale.
- [ ] Run dependency/security/resource-leak checks before production-ready claims.
- [ ] Do not mark this OpenSpec change implemented until every applicable acceptance item has fresh evidence.
