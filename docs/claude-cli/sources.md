# 官方资料与源码证据索引

研究日期：2026-09-20。范围：Claude Code CLI 与相关官方 Agent SDK 资料，对照 `easy-4-java/claudecode-java-sdk` 三条版本线。

## 证据使用规则

**D** 编号是 Claude 官方滚动文档，**S** 编号是仓库源码或 Git 元数据，**O** 编号是 OpenSpec 格式资料。查阅日期不等于发布日，也不能据此认定本机安装了某个最新 CLI。源代码固定到提交；本次提交恢复上一轮已核对的文档内容，不构成新的官方版本互操作测试。

原分析重新确认三个 HEAD，重点复核 3.x 参数、解析、执行器和测试路径。三分支共有字段和逻辑的结论不能代替三套构建与运行测试。静态确认、待实测协议风险、已有 raw 入口和拟议新能力必须区分。

未成功完整载入的资料包括大型 TypeScript 全量类型页与独立版本化完整 changelog。高阶控制消息必须在实施阶段固定官方 SDK/CLI 版本及协议证据，本文不臆造 control_request/control_response 字段。

## 索引
<a id="d01"></a>
### D01 · CLI reference

来源：`https://code.claude.com/docs/en/cli-reference`

<a id="d02"></a>
### D02 · Run Claude Code programmatically

来源：`https://code.claude.com/docs/en/headless`

<a id="d03"></a>
### D03 · Agent SDK overview

来源：`https://code.claude.com/docs/en/agent-sdk/overview`

<a id="d04"></a>
### D04 · Stream responses in real-time

来源：`https://code.claude.com/docs/en/agent-sdk/streaming-output`

<a id="d05"></a>
### D05 · Streaming Input

来源：`https://code.claude.com/docs/en/agent-sdk/streaming-vs-single-mode`

<a id="d06"></a>
### D06 · Structured outputs

来源：`https://code.claude.com/docs/en/agent-sdk/structured-outputs`

<a id="d07"></a>
### D07 · Track cost and usage

来源：`https://code.claude.com/docs/en/agent-sdk/cost-tracking`

<a id="d08"></a>
### D08 · Session management

来源：`https://code.claude.com/docs/en/agent-sdk/sessions`

<a id="d09"></a>
### D09 · Handle approvals and user input

来源：`https://code.claude.com/docs/en/agent-sdk/user-input`

<a id="d10"></a>
### D10 · Configure permissions

来源：`https://code.claude.com/docs/en/agent-sdk/permissions`

<a id="d11"></a>
### D11 · Control execution with hooks

来源：`https://code.claude.com/docs/en/agent-sdk/hooks`

<a id="d12"></a>
### D12 · MCP reference

来源：`https://code.claude.com/docs/en/mcp`

<a id="d13"></a>
### D13 · Plugins reference

来源：`https://code.claude.com/docs/en/plugins-reference`

<a id="d14"></a>
### D14 · Test plugins with evals

来源：`https://code.claude.com/docs/en/plugin-evals`

<a id="d15"></a>
### D15 · Remote Control

来源：`https://code.claude.com/docs/en/remote-control`

<a id="d16"></a>
### D16 · Authentication

来源：`https://code.claude.com/docs/en/authentication`

<a id="d17"></a>
### D17 · Settings

来源：`https://code.claude.com/docs/en/settings`

<a id="d18"></a>
### D18 · Secure deployment

来源：`https://code.claude.com/docs/en/agent-sdk/secure-deployment`

<a id="d19"></a>
### D19 · File checkpointing

来源：`https://code.claude.com/docs/en/agent-sdk/file-checkpointing`

<a id="d20"></a>
### D20 · Claude Code on the web

来源：`https://code.claude.com/docs/en/claude-code-on-the-web`

<a id="d21"></a>
### D21 · Agent view

来源：`https://code.claude.com/docs/en/agent-view`

<a id="d22"></a>
### D22 · Self-hosted environments reference

来源：`https://code.claude.com/docs/en/self-hosted-environments-reference`

<a id="d23"></a>
### D23 · Hooks reference

来源：`https://code.claude.com/docs/en/hooks`

<a id="d24"></a>
### D24 · Permissions

来源：`https://code.claude.com/docs/en/permissions`

<a id="d25"></a>
### D25 · Custom tools

来源：`https://code.claude.com/docs/en/agent-sdk/custom-tools`

<a id="d26"></a>
### D26 · Observability

来源：`https://code.claude.com/docs/en/agent-sdk/observability`

<a id="d27"></a>
### D27 · Configure the SDK

来源：`https://code.claude.com/docs/en/agent-sdk/configuration`

<a id="d28"></a>
### D28 · Host the Agent SDK

来源：`https://code.claude.com/docs/en/agent-sdk/hosting`

<a id="d29"></a>
### D29 · Official documentation index

来源：`https://code.claude.com/docs/llms.txt`

<a id="s01"></a>
### S01 · 结果解析与默认配置

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/ClaudeCodeClient.java`

<a id="s02"></a>
### S02 · 子进程执行器

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/cli/ClaudeCodeCliExecutor.java`

<a id="s03"></a>
### S03 · 通用消息对象

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/model/ClaudeMessage.java`

<a id="s04"></a>
### S04 · 命令行参数构建

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/cli/ClaudeCodeCli.java`

<a id="s05"></a>
### S05 · 门面重载与输出入口

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/ClaudeCodeClient.java`

<a id="s06"></a>
### S06 · 结果和用量字段

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/model/ClaudeResult.java`

<a id="s07"></a>
### S07 · 把数据丢失固定为正确结果的测试

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/test/java/io/github/easy4j/claudecode/ClaudeCodeClientTest.java`

<a id="s08"></a>
### S08 · 客户端配置

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/ClaudeCodeClientConfig.java`

<a id="s09"></a>
### S09 · 3.x 构建配置

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/pom.xml`

<a id="s10"></a>
### S10 · 3.x 中文说明

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/README.zh-CN.md`

<a id="s11"></a>
### S11 · 3.x CI

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/.github/workflows/ci.yml`

<a id="s12"></a>
### S12 · CLI 命令封装全貌

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/691b3ae61c059187b20d78faf099c231d3e07a39/src/main/java/io/github/easy4j/claudecode/cli/ClaudeCodeCli.java`

<a id="s13"></a>
### S13 · feature/1.0.x 构建配置

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/3d0fe8d669be527a8ae036f62bdfbde74970d22b/pom.xml`

<a id="s14"></a>
### S14 · feature/2.0.x 构建配置

来源：`https://github.com/easy-4-java/claudecode-java-sdk/blob/c717b0b2d475ac4594e44226d6a4bf52bca96b14/pom.xml`

<a id="s15"></a>
### S15 · 三分支 HEAD

来源：`https://api.github.com/repos/easy-4-java/claudecode-java-sdk/branches?per_page=100`

<a id="s16"></a>
### S16 · 3.x 完整目录树

来源：`https://api.github.com/repos/easy-4-java/claudecode-java-sdk/git/trees/691b3ae61c059187b20d78faf099c231d3e07a39?recursive=1`

<a id="o01"></a>
### O01 · OpenSpec Concepts

来源：`https://github.com/Fission-AI/OpenSpec/blob/main/docs/concepts.md`
