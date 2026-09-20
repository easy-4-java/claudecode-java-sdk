# Claude CLI alignment acceptance matrix

This file defines future acceptance criteria. It is **not** a test report.

| ID | Requirement area | Acceptance |
|---|---|---|
| T01 | Command intent | print + background is rejected before spawn; valid pinned cloud/session forms are not blanket-rejected |
| T02 | Arg encoding | debug/tmux/worktree/schema options match pinned CLI token expectations |
| T03 | Default merge | model-only override preserves configured permissions/tools/budget; explicit false stays false |
| T04 | argv boundary | spaces, quotes, Unicode and flag-like prompt text reach child as intended without shell interpolation |
| T05 | Capability | supported / unsupported / unknown / unverified are distinguishable |
| T06 | Final result | result text, session id, cost and usage survive parsing |
| T07 | Message protocol | nested content, incremental events and unknown types are preserved/diagnosable |
| T08 | Usage | official cache input-token fields parse; historical alias conflict is explicit |
| T09 | Structured output | structured result, agent failure, decode failure and invalid schema are distinct |
| T10 | Malformed stream | bad frames are diagnosable and do not create false success |
| T11 | Multi-turn accounting | repeated/full/delta events do not double-count text or usage |
| T12 | Live output | event A is delivered while the child is still running |
| T13 | One-shot stdin | input bytes and EOF semantics are deterministic |
| T14 | Per-run context | concurrent cwd/environment snapshots do not leak across runs |
| T15 | Deadlines | probe/run/drain deadlines are independently testable where configured |
| T16 | Backpressure | output growth is bounded; terminal result is not silently discarded |
| T17 | Cancellation | cancel/exit races publish one terminal state; close is idempotent |
| T18 | Failure model | start/timeout/cancel/nonzero/protocol/agent failures are distinguishable |
| T19 | Session identity | persisted session identity is separate from live execution identity |
| T20 | Persistent input | if implemented, ordered turns, half-close and backpressure are proven |
| T21 | Control capability | unknown control protocol never causes guessed messages |
| T22 | Approval/input | allow/deny/timeout/cancel/late reply affect only the correlated request |
| T23 | Hooks | observation capability is not mislabeled as programmatic control |
| T24 | Checkpoint | file rollback claims are limited to verified file effects |
| T25 | Management | management/version/status commands do not inherit print assumptions |
| T26 | MCP | stdio argument separator, HTTP/JSON config, scope/header/env behavior are validated |
| T27 | Plugin/eval | plugin scope and evaluation status are interpreted by their own contracts |
| T28 | Auth | read-only status has no login/install/update side effect and secrets are redacted |
| T29 | Long-running | attach/Remote Control/gateway/runner lifecycle is modeled explicitly |
| T30 | Destructive ops | explicit target/range is required and automated tests use isolated state |
| T31 | Branch parity | normalized fixtures pass equivalently on Java 8/17/21 lines |
| T32 | Provenance | synthetic, fake process and real CLI evidence remain distinguishable |
| T33 | Version gating | older/unknown CLI versions never default to supporting every current feature |
| T34 | Test quality | tests do not encode known null/data-loss behavior as expected success |
| T35 | API/build | public types remain usable across Jackson 2/3 branch boundaries |
| T36 | Security/ops | credentials are redacted, permissions are not downgraded, uncertain agent runs are not blindly replayed |

## Evidence required for a production-ready claim

1. Fresh full build/test output for each branch on its minimum supported JDK/build baseline.
2. Dependency and vulnerability checks.
3. Controlled process/resource lifecycle tests.
4. Cross-platform process checks where support is claimed.
5. Real Claude CLI fixtures recording actual CLI version and platform for version-specific behavior.
6. README and compatibility tables updated to match the evidence.
