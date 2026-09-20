# Claude 官方 CLI 对齐文档

日期：2026-09-20。规格状态：**Proposed，尚未实施**。文档提交不表示代码能力完成。

| 文档 | 用途 |
|---|---|
| [总方案](official-cli-alignment.zh-CN.md) | 官方契约、静态问题、能力扩展与阶段门槛 |
| [能力矩阵](capability-matrix.md) | 33 项能力及支持边界 |
| [验收矩阵](acceptance-matrix.md) | 36 组待执行验收 |
| [证据索引](sources.md) | 官方资料与固定提交源码 |
| [兼容基线](compatibility-baseline.json) | Java/JSON/Maven 与实际验证状态 |
| [变更提案](../../openspec/changes/align-claude-cli-official-contract/proposal.md) | 范围、非目标及风险 |
| [技术设计](../../openspec/changes/align-claude-cli-official-contract/design.md) | 模块、接口、迁移与生命周期 |
| [实施任务](../../openspec/changes/align-claude-cli-official-contract/tasks.md) | 57 项待实施任务 |
| [提交前验证](../../evidence/claude-cli-alignment/documentation-validation.md) | 文档结构检查，不是 Java 或真实 CLI 测试 |

上一轮临时下载包未保留，本次根据对话中的完整内容恢复为仓库文件。保留原结论、6 个规范域、36 条要求、72 个场景、36 组验收与 57 个实施任务；不声称恢复文件与旧下载包逐字节相同。

根 README 和 Java 源码不由本次文档提交覆盖；临时构建工具不进入仓库。规范仅放入 `openspec/changes/`，不提前归档或覆盖当前实现规范。
