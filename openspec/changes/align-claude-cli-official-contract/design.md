# Design: Claude CLI Official Contract Alignment

## Architecture

```text
ClaudeCodeClient compatibility facade
            |
request/default merge
            |
command intent + capability validation
            |
process runtime / execution handle
      +-----+------+
      |            |
 stdin writer   stdout/stderr pumps
                    |
              protocol decoder
                    |
       typed event + raw envelope
                    |
    blocking aggregate / live stream
```

## 1. Command model

Do not keep growing a single bag of boolean options. Internally classify invocations into explicit intents:

- local print;
- background dispatch;
- cloud create;
- cloud append;
- self-hosted dispatch;
- management command;
- interactive terminal;
- long-running service.

Validation runs before spawn. Illegal combinations fail with a structured validation error and spawn count remains zero.

## 2. Configuration merge

High-level APIs use one deterministic merge order:

```text
SDK defaults -> client defaults -> per-request explicit overrides
```

The request model must retain whether a field was explicitly supplied. `false`, an empty list, and an explicitly empty tools value are distinct from "unset".

Raw low-level execution remains available but does not promise high-level policy merging.

## 3. Message protocol

Decode each NDJSON line to a raw JSON envelope first. Dispatch by `type` and applicable subtype. Never deserialize to a lossy generic message and then attempt to reconstruct a richer result.

Required properties:

- preserve final result text, session id, cost and usage;
- support nested assistant/user message bodies and content blocks;
- preserve incremental stream events without double-appending full messages;
- use official cache token names, with explicit compatibility aliases where required;
- preserve unknown events and raw envelopes;
- distinguish malformed frames, missing final results and agent/business failures.

Public API types must not expose a Jackson-major-specific type.

## 4. Process runtime

The current blocking APIs may remain, but should aggregate on top of one bounded runtime.

Runtime requirements:

- real-time stdout event delivery before process exit;
- independent stderr drain;
- incremental UTF-8 decoding across byte boundaries;
- per-run cwd and environment snapshot;
- separate probe/start/run/drain timeouts;
- bounded queues and diagnostic buffers;
- explicit backpressure/overflow behavior;
- cancellation with one terminal state;
- idempotent close;
- cleanup only for processes/resources owned by this SDK.

Java 8, Java 17 and Java 21 implementations must expose equivalent observable behavior.

## 5. Session control

Keep these concepts separate:

1. resume/continue a persisted Claude session by starting a new CLI invocation;
2. keep one CLI process alive and send multiple streaming-input turns.

Persistent streaming control is a later capability and must not be inferred from `executeWithStdin(String,...)`.

Approval callbacks, user-input callbacks, hook control and checkpoint control are capability-gated until the target CLI/SDK wire contract is fixed and tested.

## 6. Management commands

MCP, plugin, auth, background/session, Remote Control, gateway and runner operations should receive typed request/response contracts where stable, while keeping raw passthrough for forward compatibility.

Destructive commands require explicit targets and must not run against real user state in automated tests.

## 7. Compatibility strategy

Shared tests and fixtures define behavior. Branch-specific differences are limited to:

- JDK APIs;
- Jackson 2 vs Jackson 3 codec implementation;
- platform process implementation where unavoidable.

The same normalized fixtures must produce equivalent normalized outputs on all three branches.

## 8. Security

- never silently downgrade permission/tool restrictions;
- do not auto-login, install or update Claude during client construction;
- redact credentials, headers and sensitive environment values from default logs;
- do not automatically replay an entire agent run after an uncertain failure;
- document trusted-working-directory and settings/hook loading behavior.

## 9. Evidence

Tests are separated into:

- codec/argv unit tests;
- controlled fake-process lifecycle tests;
- real Claude CLI compatibility fixtures pinned to an actual CLI version/platform;
- security/operational tests.

Static source review is not reported as runtime compatibility success.
