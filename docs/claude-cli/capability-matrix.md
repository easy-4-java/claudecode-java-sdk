# Claude CLI capability matrix

| Area | Current repository state | Gap / required action | Priority |
|---|---|---|---|
| Basic print / JSON / stream-json flags | Present | unify defaults and validate combinations | P0 |
| Final result parsing | Present but lossy | decode final result from raw envelope | P0 |
| Usage / cost | Partial | official cache input-token names and missing-vs-zero semantics | P0 |
| Nested/incremental messages | Generic model | typed envelopes + raw unknown event preservation | P0 |
| Background mode | Parameter exposed | separate from print intent; reject invalid combinations | P0 |
| Cloud/self-hosted modes | Parameters/raw entry exist | intent-specific validation and capability evidence | P1 |
| Default configuration inheritance | Inconsistent across helpers | deterministic merge for every high-level entry | P0 |
| Real-time output | Not exposed as managed live stream | execution handle + bounded event delivery | P1 |
| One-shot stdin | Present | clarify contract and EOF behavior | P1 |
| Persistent multi-turn stdin | Not proven | separate capability; implement only with verified protocol | P2 |
| cwd | Not explicit per run | add per-run working directory | P1 |
| Probe timeout | config field exists; executor behavior needs alignment | separate probe/run deadlines | P1 |
| Cancellation / close | no managed running handle | cancellation, idempotent close, owned-resource cleanup | P1 |
| MCP raw management | Broad raw mapping exists | typed transport/scope/auth contracts | P1 |
| Plugin raw management | Present | typed lifecycle and eval result contract | P1/P2 |
| Auth | commands present | explicit side-effect boundaries and typed status | P1 |
| Background/session management | commands present | typed state/results and destructive-target validation | P1 |
| Remote Control / gateway / runner | commands/options present | long-running lifecycle semantics | P2 |
| Approval/user input callbacks | not established | capability-gated until wire protocol is pinned | Conditional P2 |
| Programmatic hook/checkpoint control | not established | do not infer from CLI flags/events | Conditional P2 |
| Cross-branch parity | source mostly synchronized | shared behavior fixtures on Java 8/17/21 | All |
| Production readiness | not established by this review | build/test/security/resource/real-CLI evidence | Release |
