# claudecode-java-sdk

[English](./README.md) | [简体中文](./README.zh-CN.md)

[![License](https://img.shields.io/badge/license-Apache%202.0-green)

> [Claude Code](https://docs.anthropic.com/en/docs/claude-code) CLI 的 Java SDK：
> 通过子进程集成驱动本地 `claude` 命令行智能体
> （print / json / stream-json 输出、会话生命周期管理）。

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 功能与状态](#2-功能与状态)
- [3. 环境要求与兼容性](#3-环境要求与兼容性)
- [4. 架构与模块](#4-架构与模块)
- [5. 安装](#5-安装)
- [6. 快速开始](#6-快速开始)
- [7. 配置](#7-配置)
- [8. 核心用法 / API](#8-核心用法--api)
- [9. 测试与构建](#9-测试与构建)
- [10. 版本与分支](#10-版本与分支)
- [11. 贡献与许可](#11-贡献与许可)

## 1. 项目概述

`claudecode-java-sdk` 让 Java 应用把 [Claude Code](https://docs.anthropic.com/en/docs/claude-code)
CLI 智能体（`claude`）作为本地子进程运行。它是 **CLI 封装**，不是直连 API 客户端——
每次调用都对应一次真实的 `claude` 命令行执行。

SDK 覆盖：

- **Print 模式** — `claude -p <prompt>`，支持 text、`json`、`stream-json` 输出格式、
  JSON Schema 约束输出，以及双向 stream-json 管道。
- **会话生命周期** — `continue` / `resume` / `fork`、命名会话与 `--no-session-persistence`
  一次性运行。
- **解析模型** — 从 stream-json 输出解析出 `ClaudeMessage` / `ClaudeResult` /
  `ClaudeAgent` 对象，含用量与费用字段。
- **环境参数** — 模型、effort 级别、权限模式、工具允许 / 禁用列表、MCP 配置、
  worktree / bare 模式、debug 选项等。

它不是：

- Anthropic API 客户端（不直接调用 Anthropic API）。
- `claude` 二进制的替代品——必须安装并可直接运行的 CLI。

典型场景：

| 场景 | 使用内容 |
| :--- | :--- |
| 一次性代码任务（文本输出） | `ClaudeCodeClient.print(prompt)` |
| 机器可读结果 | `printJson(prompt)` / `printStreamJson(prompt)` / `printStreamJsonAndParse(prompt)` |
| 多轮对话持久化 | `continueSession(...)` / `resumeSession(...)` |
| 按 JSON Schema 结构化输出 | `printWithSchema(prompt, jsonSchema)` |

## 2. 功能与状态

| 能力 | 状态 | 说明 |
| :--- | :--- | :--- |
| `claude -p` print 模式 | 活跃开发 | `print`、`print(model)`、`print(PrintOptions)` |
| JSON / stream-json 输出 | 活跃开发 | `printJson`、`printStreamJson`、`printBidirectional` |
| 结构化输出（JSON Schema） | 活跃开发 | `printWithSchema(prompt, jsonSchema)` |
| 流解析 | 活跃开发 | `printStreamJsonAndParse` → `List<ClaudeMessage>` + 最终 `ClaudeResult` |
| 会话生命周期 | 活跃开发 | `continueSession`、`resumeSession`、`continueForkSession`、`resumeForkSession`、`withSessionId`、`namedSession`、`printNoPersistence` |
| 配置模型 | 活跃开发 | `ClaudeCodeClientConfig` POJO（纯对象，可绑定 Spring 配置） |
| CLI 可用性探测 | 活跃开发 | `ClaudeCodeCliExecutor.probe()` |

> **假设**：以上能力状态反映 1.0.x 分支当前情况；该模块处于活跃开发中。

## 3. 环境要求与兼容性

| 要求 | 版本 / 说明 |
| :--- | :--- |
| JDK | 8+ |
| Maven | 3.0+（enforcer 强制；项目内置 Maven Wrapper `./mvnw`） |
| Claude Code CLI | 必须安装且可执行（`localExecutable` 可配置路径） |

版本线：

| 分支 | JDK | 版本 |
| :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` |
| `feature/2.0.x` | 17 | `2.0.x.*` |
| `feature/3.0.x` | 21 | `3.0.x.*` |

## 4. 架构与模块

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

单模块 Maven 工程（`packaging: jar`），无子模块。

| 构件 | 职责 |
| :--- | :--- |
| `io.github.easy4j:claudecode-java-sdk` | CLI 门面、命令映射、子进程执行器、结果与流模型 |

关键包：

| 包 | 内容 |
| :--- | :--- |
| `io.github.easy4j.claudecode` | `ClaudeCodeClient`、`ClaudeCodeClientConfig` |
| `io.github.easy4j.claudecode.cli` | `ClaudeCodeCli`、`ClaudeCodeCliExecutor`、`ClaudeCodeCliResult` |
| `io.github.easy4j.claudecode.model` | `ClaudeMessage`、`ClaudeResult`、`ClaudeAgent` |

## 5. 安装

项目**尚未发布到 Maven Central**。快照 / 发布版本通过阿里云 Maven 仓库与 GitHub
Releases 分发。

Maven：

```xml
<dependency>
    <groupId>io.github.easy4j</groupId>
    <artifactId>claudecode-java-sdk</artifactId>
    <version>1.0.x.20260630-SNAPSHOT</version>
</dependency>
```

Gradle：

```groovy
implementation 'io.github.easy4j:claudecode-java-sdk:1.0.x.20260630-SNAPSHOT'
```

## 6. 快速开始

```java
import io.github.easy4j.claudecode.ClaudeCodeClient;
import io.github.easy4j.claudecode.ClaudeCodeClientConfig;
import io.github.easy4j.claudecode.cli.ClaudeCodeCliResult;

public class ClaudeCodeDemo {

    public static void main(String[] args) {
        ClaudeCodeClientConfig config = new ClaudeCodeClientConfig();
        config.setLocalExecutable("claude");   // 或绝对路径
        config.setLocalTimeoutSeconds(600);

        try (ClaudeCodeClient client = new ClaudeCodeClient(config)) {
            ClaudeCodeCliResult result = client.print("Write a Java hello world");
            System.out.println("exit=" + result.getExitCode());
            System.out.println(result.getStdout());
        }
    }
}
```

预期结果：本地执行 `claude -p "Write a Java hello world"`；成功时
`result.getExitCode()` 为 `0`，`result.getStdout()` 包含智能体的文本回答。

## 7. 配置

`ClaudeCodeClientConfig` 是纯 POJO（可绑定 Spring `@ConfigurationProperties`），
本身没有配置文件。关键字段：

| 字段 | 类型 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- |
| `localExecutable` | String | `claude` | CLI 可执行文件名或绝对路径 |
| `localTimeoutSeconds` | int | `600` | 命令执行超时（秒） |
| `localProbeTimeoutSeconds` | int | `5` | CLI 可用性探测超时（秒） |
| `defaultModel` | String | - | 默认模型（如 `sonnet`、`opus`） |
| `defaultEffort` | String | - | Effort 级别（`low`、`medium`、`high`、`xhigh`、`max`） |
| `defaultPermissionMode` | String | - | 权限模式（`default`、`acceptEdits`、`bypassPermissions`、`plan`、`auto`） |
| `defaultOutputFormat` | String | `stream-json` | 默认输出格式（`text`、`json`、`stream-json`） |
| `includePartialMessages` | boolean | `true` | 是否包含部分消息块 |
| `noSessionPersistence` | boolean | `false` | 不持久化会话（一次性运行） |
| `sessionName` | String | - | 会话显示名称前缀 |
| `maxBudgetUsd` | Double | - | 最大预算（美元） |
| `jsonSchema` | String | - | 结构化输出的 JSON Schema 文件路径 |
| `worktree` / `bare` | boolean | `false` | worktree 隔离 / Bare 模式 |
| `systemPrompt` / `systemPromptFile` | String | - | 自定义 system prompt |
| `allowedTools` / `disallowedTools` / `tools` | String | - | 工具限制 |
| `mcpConfig` / `strictMcpConfig` | String / boolean | - | MCP 配置 |
| `debug` / `debugFilter` / `debugFile` | - | - | 调试选项 |

## 8. 核心用法 / API

### 8.1 机器可读输出

```java
try (ClaudeCodeClient client = new ClaudeCodeClient(config)) {
    // 将 stream-json 解析为类型化消息 + 最终结果
    ClaudeCodeClient.StreamResult parsed = client.printStreamJsonAndParse("Summarize the attached file");
    parsed.getMessages().forEach(msg -> System.out.println(msg.getType() + " -> " + msg.getMessage()));
    if (parsed.getResult() != null) {
        System.out.println("cost USD: " + parsed.getResult().getTotalCostUsd());
    }
}
```

### 8.2 会话生命周期

```java
try (ClaudeCodeClient client = new ClaudeCodeClient(config)) {
    client.continueSession("continue the previous conversation");
    client.resumeSession("session-id-123");       // 按 ID 恢复
    // client.continueForkSession();              // 继续并 fork 出新的 session id
    // client.resumeForkSession("session-id-123");
}
```

## 9. 测试与构建

```bash
./mvnw clean verify
```

- 构建配置了 JaCoCo Maven 插件（报告 + 绑定在 `verify` 阶段的 `check` 目标，
  行覆盖率规则为 90%；`haltOnFailure=false`）。
- **假设**：1.0.x 分支当前 `src/test` 下未提交测试源码；覆盖率门禁仅在存在测试时生效。
- 本 worktree 的 `.github/` 下无 CI 工作流文件。

## 10. 版本与分支

| 分支 | JDK | 版本 | 说明 |
| :--- | :--- | :--- | :--- |
| `feature/1.0.x` | 8 | `1.0.x.*` | 当前分支，JDK 8 基线，活跃开发 |
| `feature/2.0.x` | 17 | `2.0.x.*` | JDK 17 版本线 |
| `feature/3.0.x` | 21 | `3.0.x.*` | JDK 21 版本线 |

维护策略：`1.0.x` 版本线接收针对 JDK 8 基线的缺陷修复与兼容性更新；面向新 JDK 的
新特性在 `2.0.x` / `3.0.x` 版本线开发。发布物通过阿里云 Maven 仓库与 GitHub
Releases 分发；项目尚未发布到 Maven Central。

## 11. 贡献与许可

欢迎通过 GitHub Issue 或 Pull Request 参与贡献。

本项目基于 [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt) 许可。
