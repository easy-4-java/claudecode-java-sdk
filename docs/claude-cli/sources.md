# Claude CLI alignment sources

Research date: 2026-09-20.

## Official Claude documentation

- D01 CLI reference: https://code.claude.com/docs/en/cli-reference
- D02 Headless / programmatic CLI: https://code.claude.com/docs/en/headless
- D03 Agent SDK overview: https://code.claude.com/docs/en/agent-sdk/overview
- D04 Streaming output: https://code.claude.com/docs/en/agent-sdk/streaming-output
- D05 Streaming vs single mode: https://code.claude.com/docs/en/agent-sdk/streaming-vs-single-mode
- D06 Structured outputs: https://code.claude.com/docs/en/agent-sdk/structured-outputs
- D07 Cost and usage: https://code.claude.com/docs/en/agent-sdk/cost-tracking
- D08 Sessions: https://code.claude.com/docs/en/agent-sdk/sessions
- D09 User input and approvals: https://code.claude.com/docs/en/agent-sdk/user-input
- D10 SDK permissions: https://code.claude.com/docs/en/agent-sdk/permissions
- D11 SDK hooks: https://code.claude.com/docs/en/agent-sdk/hooks
- D12 MCP: https://code.claude.com/docs/en/mcp
- D13 Plugin reference: https://code.claude.com/docs/en/plugins-reference
- D14 Plugin evals: https://code.claude.com/docs/en/plugin-evals
- D15 Remote Control: https://code.claude.com/docs/en/remote-control
- D16 Authentication: https://code.claude.com/docs/en/authentication
- D17 Settings: https://code.claude.com/docs/en/settings
- D18 Secure deployment: https://code.claude.com/docs/en/agent-sdk/secure-deployment
- D19 File checkpointing: https://code.claude.com/docs/en/agent-sdk/file-checkpointing
- D20 Claude Code on the web: https://code.claude.com/docs/en/claude-code-on-the-web
- D21 Agent view: https://code.claude.com/docs/en/agent-view
- D22 Self-hosted environments: https://code.claude.com/docs/en/self-hosted-environments-reference
- D23 Hooks reference: https://code.claude.com/docs/en/hooks
- D24 CLI permissions: https://code.claude.com/docs/en/permissions
- D25 Custom tools: https://code.claude.com/docs/en/agent-sdk/custom-tools
- D26 Observability: https://code.claude.com/docs/en/agent-sdk/observability
- D27 SDK configuration: https://code.claude.com/docs/en/agent-sdk/configuration
- D28 Agent SDK hosting: https://code.claude.com/docs/en/agent-sdk/hosting
- D29 Documentation index: https://code.claude.com/docs/llms.txt

Official documentation is rolling documentation. It is evidence for intended behavior, not proof of the Claude CLI version installed on a user's machine.

## Repository evidence

Pinned branches at research time:

- S13 feature/1.0.x: `3d0fe8d669be527a8ae036f62bdfbde74970d22b`
- S14 feature/2.0.x: `c717b0b2d475ac4594e44226d6a4bf52bca96b14`
- S15 feature/3.0.x: `691b3ae61c059187b20d78faf099c231d3e07a39`

Important 3.x source evidence:

- S01 `ClaudeCodeClient.java`: default option merge, stream parsing and final-result projection
- S02 `ClaudeCodeCliExecutor.java`: subprocess, stdin, stdout/stderr, timeout and probe
- S03 `ClaudeMessage.java`: current generic message representation
- S04 `ClaudeCodeCli.java`: argv generation and command mapping
- S05 facade overloads and output helpers in `ClaudeCodeClient.java`
- S06 `ClaudeResult.java`: cost and usage properties
- S07 `ClaudeCodeClientTest.java`: existing regression expectations
- S08 `ClaudeCodeClientConfig.java`: configured defaults and timeout fields
- S09 `pom.xml`: Java 21 / Jackson 3 / Maven 4 baseline
- S10 `README.zh-CN.md`
- S11 `.github/workflows/ci.yml`
- S12 raw CLI-management mappings in `ClaudeCodeCli.java`

## Evidence rule

Static source inspection, synthetic fixtures, controlled fake-process tests and real Claude CLI captures are different evidence classes and MUST NOT be reported interchangeably.
