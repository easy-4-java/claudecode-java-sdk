# Proposal: 对齐 Claude 官方 CLI 契约并补强 Java 运行适配层

状态：**Proposed / 待评审**。日期：2026-09-20。变更 ID：`align-claude-cli-official-contract`。

## Why

现有 SDK 提供较宽的 CLI 封装，但结果经通用消息投影后丢失正文、费用和用量，测试还将这种丢失固定为正确结果；部分模式组合违反官方契约；高层入口默认配置不一致；stream-json 在退出后才解析，尚非实时或持续双向协议。这些问题影响正确性、权限配置可预测性、运行观测及 API 名称可信度。

依据：[官方核对报告](../../../docs/claude-cli/official-cli-alignment.zh-CN.md) 与 [来源索引](../../../docs/claude-cli/sources.md)。

## What Changes

修复原始 envelope/result 解析、缓存用量字段、配置合并及命令组合；补充实时、有界、可取消、带 cwd 和独立探测期限的执行层。已有 raw 入口上补充结构化输出、MCP、Plugin、Auth、Session 与后台管理的稳定契约。多轮、审批、Hook 控制、检查点仅在协议和版本证据充分时开放。

三个版本线共同语义一致，JDK/JSON/平台适配分别验证。当前提交只有文档，不实施以上功能。

## Capabilities

### New Capabilities

这里的新增是正式行为规范新增，不代表已有代码全部从零实现。

- `cli-command-contract`：命令意图、参数、配置合并与能力验证。
- `cli-message-protocol`：无损 envelope、消息、结果、结构化输出、用量和诊断。
- `cli-process-runtime`：实时 I/O、cwd、期限、背压、清理及终态。
- `cli-session-control`：持续输入、身份/所有权和条件式控制。
- `cli-management`：MCP/插件/认证/后台/导入及危险操作。
- `cli-compatibility-evidence`：三线一致性、测试来源、迁移与安全声明。

### Modified Capabilities

尚无确认需要原位修改的当前 OpenSpec 规范。实施前若发现既有规范，应先映射后合并，不直接覆盖。拟议要求仅放在 changes，不提前写入当前实现 specs 或归档。

## Non-Goals

不改为 Anthropic HTTP API SDK；不在文档提交阶段编码；不自动更新 CLI、登录、安装插件或发布制品；不默认引入 Python/TypeScript sidecar；不宣称所有官方 SDK wire 协议已验证；不设计新的企业 Agent 平台或 UI/收费系统。

## Impact

后续会影响 Client、CLI、Executor、模型和测试。保留旧门面及阻塞入口，但信息丢失、非法参数和安全配置失效不能作为兼容承诺。单 Maven 模块先按职责拆包，共同 API 满足 Java 8 基线并隔离 Jackson 2/3。

## Rollout and Gates

依次完成正确性、可控执行、类型化管理和条件式高阶协议。每阶段先有暴露缺陷的失败测试，再实现，再三线回归；无证据能力保持 UNKNOWN/UNSUPPORTED/未验证。

## Risks

滚动官方文档可能领先于安装版本；配置修复改变部分调用行为；持久进程引入并发与清理问题；高阶协议尚需证据；目录配置/Hook 具有安全影响。缓解措施见 [设计](design.md)、[任务](tasks.md) 和 [验收矩阵](../../../docs/claude-cli/acceptance-matrix.md)。

## Current Delivery Status

仅文档，Java 实现未修改。静态缺陷与协议风险已区分；Maven、真实 CLI、动态漏洞扫描、CodeGraph 与 OpenSpec 官方校验没有因本次文档提交而获得通过状态。
