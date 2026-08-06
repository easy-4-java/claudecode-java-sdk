# claudecode-java-sdk

[![Java](https://img.shields.io/badge/Java-17-orange)] [![License](https://img.shields.io/badge/license-Apache%202.0-green)](https://www.apache.org/licenses/LICENSE-2.0.txt)

> Java SDK for the [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI:
> subprocess integration that drives the `claude` command line agent
> (print / json / stream-json output, session lifecycle) from Java.

## Table of Contents

- [1. Project Overview](#1-project-overview)
- [2. Features & Status](#2-features--status)
- [3. Requirements & Compatibility](#3-requirements--compatibility)
- [4. Architecture & Modules](#4-architecture--modules)
- [5. Installation](#5-installation)
- [6. Quick Start](#6-quick-start)
- [7. Configuration](#7-configuration)
- [8. Core Usage / API](#8-core-usage--api)
- [9. Testing & Build](#9-testing--build)
- [10. Versioning & Branches](#10-versioning--branches)
- [11. Contributing & License](#11-contributing--license)

## 1. Project Overview

`claudecode-java-sdk` lets Java applications run the
[Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI agent (`claude`)
as a local subprocess. It is a **CLI wrapper**, not a direct API client — every call
maps to a real `claude` command line invocation.

The SDK covers:

- **Print mode** — `claude -p <prompt>` with text, `json` and `stream-json` output
  formats, JSON Schema constrained output, and bidirectional stream-json piping.
- **Session lifecycle** — `continue` / `resume` / `fork` / named sessions and
  `--no-session-persistence` for one-shot runs.
- **Parsed models** — `ClaudeMessage` / `ClaudeResult` / `ClaudeAgent` objects parsed
  from the stream-json output, including usage and cost fields.
- **Environment flags** — model, effort level, permission mode, tools allow/deny
  lists, MCP config, worktree / bare modes, debug options and more.

What it is **not**:

- Not an Anthropic API client (no direct HTTP calls to the Anthropic API).
- Not a replacement for the `claude` binary — the CLI must be installed and runnable.

Typical scenarios:

| Scenario | What you use |
| :--- | :--- |
| One-shot code task with text output | `ClaudeCodeClient.print(prompt)` |
| Machine-readable results | `printJson(prompt)` / `printStreamJson(prompt)` / `printStreamJsonAndParse(prompt)` |
| Multi-turn conversation with persistence | `continueSession(...)` / `resumeSession(...)` |
| Structured output against a JSON Schema | `printWithSchema(prompt, jsonSchema)` |

## 2. Features & Status

| Capability | Status | Notes |
| :--- | :--- | :--- |
| `claude -p` print mode | Active development | `print`, `print(model)`, `print(PrintOptions)` |
| JSON / stream-json output | Active development | `printJson`, `printStreamJson`, `printBidirectional` |
| Structured output (JSON Schema) | Active development | `printWithSchema(prompt, jsonSchema)` |
| Stream parsing | Active development | `printStreamJsonAndParse` → `List<ClaudeMessage>` + final `ClaudeResult` |
| Session lifecycle | Active development | `continueSession`, `resumeSession`, `continueForkSession`, `resumeForkSession`, `withSessionId`, `namedSession`, `printNoPersistence` |
| Config model | Active development | `ClaudeCodeClientConfig` POJO (plain, Spring-bindable) |
| CLI availability probe | Active development | `ClaudeCodeCliExecutor.probe()` |

> **Assumption**: the capability statuses above reflect the current state of the
> 1.0.x branch; the module is under active development.

## 3. Requirements & Compatibility

| Requirement | Version / Notes |
| :--- | :--- |
| JDK | 17+ |
| Maven | 3.0+ (enforced; Maven Wrapper `./mvnw` included) |
| Claude Code CLI | `claude` must be installed and available (`localExecutable` configures the path) |

Version lines:

| Branch | JDK | Version |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. Architecture & Modules

```text
+------------------+   +------------------------------------------+
| Java application |   | claudecode-java-sdk                      |
|                  |-->|  ClaudeCodeClient (facade)               |
| prompt / options |   |    | ClaudeCodeCli (command mapping)     |
|                  |   |    |   | ClaudeCodeCliExecutor           |
|                  |   |    |   |   `claude` child process        |
|                  |   |    |   ClaudeCodeCliResult               |
+------------------+   |    | ClaudeMessage/ClaudeResult/Agent    |
                       +-------------------+----------------------+
                                           |
                                           v
                     +-------------------------------------------+
                     | Local `claude` CLI (print mode, session   |
                     | commands, --version, --help)              |
                     +-------------------------------------------+
```

Single-module Maven project (`packaging: jar`). No child modules.

| Artifact | Responsibility |
| :--- | :--- |
| `io.github.easy4j:claudecode-java-sdk` | CLI facade, command mapping, subprocess executor, result & stream models |

Key packages:

| Package | Content |
| :--- | :--- |
| `io.github.easy4j.claudecode` | `ClaudeCodeClient`, `ClaudeCodeClientConfig` |
| `io.github.easy4j.claudecode.cli` | `ClaudeCodeCli`, `ClaudeCodeCliExecutor`, `ClaudeCodeCliResult` |
| `io.github.easy4j.claudecode.model` | `ClaudeMessage`, `ClaudeResult`, `ClaudeAgent` |

## 5. Installation

The project is **not yet published to Maven Central**. Snapshots/releases are
distributed through the Aliyun Maven repository and GitHub Releases.

Maven:

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>claudecode-java-sdk</artifactId>
    <version>2.0.x.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle:

```groovy
implementation 'io.github.easy4j:claudecode-java-sdk:2.0.x.x.20260630-SNAPSHOT'
```

## 6. Quick Start

```java
import io.github.easy4j.claudecode.ClaudeCodeClient;
import io.github.easy4j.claudecode.ClaudeCodeClientConfig;
import io.github.easy4j.claudecode.cli.ClaudeCodeCliResult;

public class ClaudeCodeDemo {

    public static void main(String[] args) {
        ClaudeCodeClientConfig config = new ClaudeCodeClientConfig();
        config.setLocalExecutable("claude");   // or an absolute path
        config.setLocalTimeoutSeconds(600);

        try (ClaudeCodeClient client = new ClaudeCodeClient(config)) {
            ClaudeCodeCliResult result = client.print("Write a Java hello world");
            System.out.println("exit=" + result.getExitCode());
            System.out.println(result.getStdout());
        }
    }
}
```

Expected result: the `claude -p "Write a Java hello world"` command runs locally;
`result.getExitCode()` is `0` on success and `result.getStdout()` contains the
agent's text answer.

## 7. Configuration

`ClaudeCodeClientConfig` is a plain POJO (Spring `@ConfigurationProperties`-bindable).
There is no configuration file of its own. Key fields:

| Field | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `localExecutable` | String | `claude` | CLI executable name or absolute path |
| `localTimeoutSeconds` | int | `600` | Command execution timeout (seconds) |
| `localProbeTimeoutSeconds` | int | `5` | Timeout for the CLI availability probe |
| `defaultModel` | String | - | Default model (e.g. `sonnet`, `opus`) |
| `defaultEffort` | String | - | Effort level (`low`, `medium`, `high`, `xhigh`, `max`) |
| `defaultPermissionMode` | String | - | Permission mode (`default`, `acceptEdits`, `bypassPermissions`, `plan`, `auto`) |
| `defaultOutputFormat` | String | `stream-json` | Default output format (`text`, `json`, `stream-json`) |
| `includePartialMessages` | boolean | `true` | Include partial message blocks |
| `noSessionPersistence` | boolean | `false` | One-shot run without session persistence |
| `sessionName` | String | - | Session display name prefix |
| `maxBudgetUsd` | Double | - | Maximum budget in USD |
| `jsonSchema` | String | - | JSON Schema file path for structured output |
| `worktree` / `bare` | boolean | `false` | Worktree isolation / Bare mode |
| `systemPrompt` / `systemPromptFile` | String | - | Custom system prompt |
| `allowedTools` / `disallowedTools` / `tools` | String | - | Tool restrictions |
| `mcpConfig` / `strictMcpConfig` | String / boolean | - | MCP configuration |
| `debug` / `debugFilter` / `debugFile` | - | - | Debug options |

## 8. Core Usage / API

### 8.1 Machine-readable output

```java
try (ClaudeCodeClient client = new ClaudeCodeClient(config)) {
    // parse stream-json into typed messages + final result
    ClaudeCodeClient.StreamResult parsed = client.printStreamJsonAndParse("Summarize the attached file");
    parsed.getMessages().forEach(msg -> System.out.println(msg.getType() + " -> " + msg.getMessage()));
    if (parsed.getResult() != null) {
        System.out.println("cost USD: " + parsed.getResult().getTotalCostUsd());
    }
}
```

### 8.2 Session lifecycle

```java
try (ClaudeCodeClient client = new ClaudeCodeClient(config)) {
    client.continueSession("continue the previous conversation");
    client.resumeSession("session-id-123");       // resume by id
    // client.continueForkSession();              // continue, forking a new session id
    // client.resumeForkSession("session-id-123");
}
```

## 9. Testing & Build

```bash
./mvnw clean verify
```

- The build is configured with the JaCoCo Maven plugin (report + `check` goal with a
  90% line-coverage rule bound to the `verify` phase; `haltOnFailure=false`).
- **Assumption**: the 1.0.x branch currently checks in no test sources under
  `src/test`; coverage thresholds are therefore enforced only when tests exist.
- No CI workflow files are present under `.github/` in this worktree.

## 10. Versioning & Branches

| Branch | JDK | Version | Notes |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | Current branch, JDK 8 baseline, active development |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 line |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 line |

Maintenance policy: the `1.0.x` line receives bug fixes and compatibility updates
for the JDK 8 baseline. New features targeting newer JDKs land on the `2.0.x` /
`3.0.x` lines. Releases are published to the Aliyun Maven repository and as
GitHub Releases; the project is not yet published to Maven Central.

## 11. Contributing & License

Contributions are welcome — please open issues or pull requests on GitHub.

Licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
