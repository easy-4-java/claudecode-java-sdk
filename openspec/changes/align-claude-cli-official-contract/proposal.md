# Align Claude CLI Official Contract

## Why

`claudecode-java-sdk` already wraps a broad Claude Code CLI surface, but static comparison with current official Claude CLI and Agent SDK documentation found correctness and lifecycle gaps that should be fixed before adding more convenience methods.

The highest-risk gaps are:

- final `result`, cost and usage data can be lost during `stream-json` parsing;
- message envelopes are modeled too loosely for nested and incremental events;
- cache token field names do not match the official usage contract;
- `PrintOptions` can form invalid command combinations such as print mode with background mode;
- high-level overloads do not consistently inherit client defaults;
- current stream helpers buffer until process exit and are not a real-time managed stream;
- process working directory, probe timeout, cancellation and owned-process cleanup need explicit contracts.

## What Changes

1. Define a lossless Claude CLI message/result protocol model.
2. Introduce command-intent validation before process spawn.
3. Make client-default merging consistent across high-level entry points.
4. Add a bounded real-time process/event runtime while preserving blocking compatibility APIs.
5. Separate one-shot stdin from persistent multi-turn session control.
6. Add typed management contracts for MCP, plugin, auth, session/background and long-running commands.
7. Add capability/version evidence so unsupported or unverified behavior is never presented as supported.
8. Keep behavior aligned across `feature/1.0.x`, `feature/2.0.x`, and `feature/3.0.x`; only JDK/Jackson/platform adapters may differ.

## Scope

This change specifies behavior and implementation work. It does not claim that Claude CLI integration, Agent SDK control protocols, Windows/POSIX process-tree cleanup, or production readiness have already been verified.

## Non-goals

- Replacing the official Claude CLI.
- Calling Anthropic HTTP APIs directly.
- Claiming Java support for every official Agent SDK callback before the underlying protocol is evidenced.
- Treating worktree isolation as an operating-system sandbox.
