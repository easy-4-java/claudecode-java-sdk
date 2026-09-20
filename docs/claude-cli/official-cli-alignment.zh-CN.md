# claudecode-java-sdk：Claude 官方 CLI 能力对齐与补充方案

**版本：V1.0 · 研究日期：2026-09-20 · 状态：待评审的文档方案，未实施**  
适用仓库：`easy-4-java/claudecode-java-sdk`  
适用版本线：`feature/1.0.x`、`feature/2.0.x`、`feature/3.0.x`

## 1. 结论与本轮交付边界

**下一步不应继续单纯堆积 `printWithXXX()`，而应先修复数据契约和命令语义，再建立真正的流式执行与会话控制。**

现有项目已经覆盖不少 CLI 命令，并保留原始参数调用入口；主要问题不是“命令数量太少”，而是部分入口没有兑现名称所表达的能力、不同门面方法的默认配置不一致，以及测试把信息丢失固化为正确行为。[S01](sources.md#s01) [S05](sources.md#s05) [S07](sources.md#s07) [S12](sources.md#s12)

本方案区分三个层级，后续宣传、README 和验收均须沿用此区分：

| 层级 | 所谓“支持”的含义 | 本项目下一步定位 |
|---|---|---|
| CLI 参数封装 | 能生成参数并启动进程 | 保留、修正、补充类型化管理入口 |
| CLI 运行与消息协议 | 能持续读写、解析事件、保留结果、取消和清理 | 优先完善的核心能力 |
| 官方 Agent SDK 程序化控制 | 工具审批、交互提问、Hook 回调、自定义工具等 | 分阶段适配；须另行确认底层协议与版本，不能靠设置同名参数宣称已支持 |

官方将 CLI 脚本入口与 Python/TypeScript 的程序化接口并列介绍；本项目是第三方 Java 适配层，不因此自动具备官方 SDK 所有能力。[D03](sources.md#d03) [D09](sources.md#d09) [D25](sources.md#d25)

本轮仅交付分析、设计和 OpenSpec 变更规范。没有修改 Java 实现，没有运行真实 Claude 会话、Maven 测试或 CodeGraph，也没有提交、推送 GitHub。文档中的接口名是设计建议，不是已经可调用的 API。

## 2. 核对基线与证据强度

| 分支 | 固定提交 | Java | JSON 依赖基线 | 构建基线 |
|---|---|---:|---|---|
| `feature/1.0.x` | `3d0fe8d669be527a8ae036f62bdfbde74970d22b` | 8 | Jackson 2 | Maven 3 |
| `feature/2.0.x` | `c717b0b2d475ac4594e44226d6a4bf52bca96b14` | 17 | Jackson 2 | Maven 3 |
| `feature/3.0.x` | `691b3ae61c059187b20d78faf099c231d3e07a39` | 21 | Jackson 3 | Maven 4 |

来源：[S09](sources.md#s09) [S13](sources.md#s13) [S14](sources.md#s14) [S15](sources.md#s15)。机器可读基线见 [compatibility-baseline.json](compatibility-baseline.json)。

本轮重新确认三个 HEAD 未变化，重点复核 3.x 的关键路径。前轮文件 SHA 比对显示命令构建、配置、消息模型和结果模型三线一致；客户端主要区别是 Jackson 导入，执行器主要区别是 Java 8 的 UTF-8 API 适配。因此下列共享问题应在三分支同步解决，但不能把静态一致当成三个运行环境测试通过。

本文采用四种状态：**静态确认**、**协议风险待实测**、**已有低层入口但缺少高层契约**、**拟议新增**。官方文档是查阅当日滚动页面，不是用户安装版本的证明。

## 3. 先修复的正确性问题

### F01 · 最终结果、费用和 usage 在解析链中丢失｜P0｜静态确认

当前路径为：

```text
原始 result JSON
    → ClaudeMessage（没有 result / total_cost_usd / usage 字段）
    → 忽略未知字段
    → findResult：convertValue(ClaudeMessage, ClaudeResult)
    → 字段无法恢复
```

问题不是“缺少一个 getter”，而是信息在第一次映射时已经被丢弃。现有 `shouldPrintStreamJsonAndParseReturnFinalResult` 甚至明确断言最终正文和费用为 `null`，这类测试应改成缺陷回归，而不是原样保留。[S01](sources.md#s01) [S03](sources.md#s03) [S07](sources.md#s07)

**补充方案：**先读原始 JSON 树，依据 `type/subtype` 分派；result 从原始节点独立解析；保留完整原始 envelope。旧通用消息仅作为兼容投影，不再作为无损数据源。正文缺失、业务失败和结果缺帧分别表达，禁止用“成功且空正文”掩盖问题。

**验收：**输入含 `result="final"`、费用、usage 的结果，解析后逐字段一致；未知扩展字段可读；缺帧与损坏帧可诊断；最后一个结果只作为一次性执行的最终结果，在持续会话中按 turn 保留每个结果。

### F02 · 消息结构与嵌套 assistant envelope 不一致｜P0｜协议风险待实测

`ClaudeMessage.message` 声明为 `String`，测试采用简化的字符串消息。官方流模型包含完整消息对象与增量事件，二者不能用一个根级 `String message` 概括。[S03](sources.md#s03) [S07](sources.md#s07) [D04](sources.md#d04)

**补充方案：**统一 envelope 元数据与 body 分离；assistant/user 的嵌套消息、stream_event 的增量 body、system 子类型、result 分别解析。工具调用和工具结果首先按内容块处理，不假定都是顶层事件。未知类型进入 `UnknownEvent`，保留 raw JSON；宽松模式也必须提供解析问题清单。

真实 Jackson 2/3 反序列化行为应各自以协议夹具验证，不用此次静态核对冒充实际复现。

### F03 · 缓存 token 字段名称错误，结果模型不完整｜P0｜静态确认

当前 `ClaudeResult.Usage` 映射 `cache_creation_tokens`、`cache_read_tokens`；官方用量字段是 `cache_creation_input_tokens`、`cache_read_input_tokens`。[S06](sources.md#s06) [D07](sources.md#d07)

**补充方案：**按官方字段解析，旧名称可作为兼容输入别名；缺失用量与数值 0 区分。结果增加可选结构化输出、业务状态、错误详情、轮次和耗时等字段，未经确认的字段保留为扩展，不假造默认值。费用保留估算语义，不作为结算账本。持续输入场景还须区分累计结果与本轮增量，保留计量范围和重置边界，不把每轮累计值相加。

### F04 · PrintOptions 可以生成官方拒绝的参数组合｜P0｜静态确认

`toArgs()` 无条件加入 `-p`，之后还能加入 `--bg` 和云端相关参数。官方明确区分创建云任务与给既有云会话追加消息，且禁止 `-p` 与 `--bg` 组合。[S04](sources.md#s04) [D01](sources.md#d01) [D20](sources.md#d20)

**补充方案：**拆分命令意图，而不是再加布尔开关。至少区分本地 print、后台派发、新建云任务、既有云会话追加、自托管派发、管理命令、交互终端、常驻服务。

重要例外：不得笼统认定所有云端参数都与 `-p` 冲突。`--cloud` 指向既有 session 的追加消息，以及自托管 `--environment` 派发，应按照各自官方契约判断。`--exec` 执行 shell 内容的风险也不能套用普通 argv 安全论断。[D20](sources.md#d20) [D22](sources.md#d22)

**验收：**非法组合在启动子进程之前失败；合法云追加和自托管派发有正例；报错列出冲突字段，不静默删除权限或任务模式。

### F05 · 默认配置不一致，可能绕过调用方设置的约束｜P0｜静态确认

`print(prompt, model)` 创建全新的选项，只设置 model；流式、JSON 等若干便利入口直接调用低层 CLI。它们没有一致继承客户端层的权限、工具、预算和提示词默认配置。执行器层的 executable/environment 仍然生效，不能误写成“全部配置都失效”。[S05](sources.md#s05)

此外，`defaultPrintOptions()` 只在配置为 true 时写入 partial 标志，而 `PrintOptions` 本身默认 true；因此设置 `includePartialMessages=false` 无法按预期关闭。[S01](sources.md#s01) [S08](sources.md#s08)

**补充方案：**高层入口采用一致的配置合并：SDK 默认值 → 客户端默认值 → 本次显式覆盖。显式 false、空集合和 `tools=""` 不是未设置。新增请求对象保存“是否显式赋值”的信息。保留低层原始调用，但清楚标注其不提供同一高层策略承诺；旧完整 `PrintOptions` 的兼容行为见设计文档。

**验收：**仅覆盖 model 不改变既有权限、工具限制、预算等；false 真正生效；不同高层输出入口对安全相关默认配置保持一致。

### F06 · 细节参数编码缺乏契约测试｜P0/P1｜静态确认与版本验证并存

目前过滤式 debug 以两个参数输出；官方描述要求绑定形式。`tmux("classic")` 的 builder 路径丢失 classic，而直接便利方法使用了另一种编码。此类问题证明“两个 API 都存在”不能替代行为一致。[S04](sources.md#s04) [S12](sources.md#s12) [D01](sources.md#d01)

应修复 debug、tmux/worktree 组合并增加逐 token golden tests。对 JSON Schema 便利接口补充结构化结果契约；对 stream-json 快捷入口补充官方推荐输出组合与版本验证，不在没有实测时断言所有历史版本都会失败。

## 4. 补齐真正的运行能力

### F07 · 实时流式输出，而不是退出后解析｜P1｜静态确认

当前执行器先完整缓存 stdout/stderr，再返回；客户端随后拆行。方法名中的 StreamJson 表示输出格式，不代表用户能在进程运行期间收到事件。[S02](sources.md#s02) [S05](sources.md#s05)

新增事件回调/订阅入口和可取消执行句柄，底层持续读取 stdout；stderr 独立排空；增量 UTF-8 解码覆盖跨字节边界；按运行内序号有序投递。旧阻塞接口可以在同一引擎上聚合结果。实时消息与完整消息的语义依官方流文档区分，不重复追加文本。[D04](sources.md#d04)

默认使用有界队列和有界诊断保留。慢消费者进入明确的等待期限或取消策略，不能无限扩张内存，也不能静默丢弃 result。磁盘暂存是可选策略，须有容量、清理和访问控制。

### F08 · 持续 stdin 与托管会话｜P1/P2｜现有入口不等于完整双向流

`executeWithStdin(String, …)` 一次性写入字节并关闭输入；`printBidirectional(prompt)` 只设置输入输出格式，未建立可持续写入通道。**前轮把它描述成完整双向流支持不准确。**[S02](sources.md#s02) [S05](sources.md#s05)

第一阶段提供明确的一次性 stdin 输入接口；第二阶段才增加可持续写入的会话句柄、顺序发送、多轮结果、输入半关闭、取消与关闭。官方持续输入与单次调用的区别应成为验收条件。[D05](sources.md#d05)

会话恢复标识不等于进程句柄；CLI `--resume` 创建一次新运行与同一个进程里的多轮输入是两条不同路径。不得用一个全局“当前 session”隐式复用所有请求。[D08](sources.md#d08)

### F09 · cwd、探测超时、取消和资源释放｜P1｜静态确认及设计补强

执行器没有显式设置工作目录；`probe()` 沿用普通执行超时，没有使用独立 probe timeout。`close()` 当前为空，不能据此承诺可以终止正在运行的任务。[S01](sources.md#s01) [S02](sources.md#s02) [S08](sources.md#s08)

应增加每次调用的 cwd、独立启动/执行/排空期限、取消 token、并发上限和执行句柄。`--add-dir` 是额外目录访问配置，不替代进程 cwd。工作树隔离不等于系统级安全沙箱。

清理必须按“本 SDK 创建并拥有的进程”定义边界。句柄 close 幂等；先结束输入或请求正常结束，再按期限升级终止，最后结束流泵与任务。Java 8、Windows 与 POSIX 的子进程树控制应有各自策略和验证记录，不承诺未经测试的全平台零残留。[D18](sources.md#d18) [D28](sources.md#d28)

### F10 · 区分传输、协议和业务结果｜P1｜拟议新增

统一结果对象应分别暴露：实际退出码、终止原因、协议完整性、业务结果、可选最终 envelope、解析诊断、初始化依赖状态和受控 raw 输出。超时、用户取消、启动失败不再只能共享一个 `-1`。

管理命令不要求有 Agent result；一次性 JSON/stream 任务缺少必要 result 不能算完整成功；持续会话收到某一轮 result 不等于整个进程已经退出。依赖加载诊断应允许调用者配置为必需项或可选项，而非一刀切失败。官方编程指南提供了初始化诊断与退出行为的依据。[D02](sources.md#d02)

## 5. 已有命令基础上补充类型化能力

以下不是全部从零实现。现有 `execute`、`mcp(...)`、`plugin(...)` 等 raw 入口可以调用部分能力，真正缺少的是验证、作用域、错误分类和稳定返回对象。[S12](sources.md#s12)

| 能力域 | 补充方向 | 建议阶段 | 官方依据 |
|---|---|---|---|
| 结构化输出 | 显式 JSON 输出、schema、structured result、解析失败与业务失败分离 | P0/P1 | [D06](sources.md#d06) |
| MCP | 类型化 stdio/HTTP 配置、scope、环境和 header；参数分隔；认证与状态对象 | P1 | [D12](sources.md#d12) |
| Plugin | 类型化安装/卸载/启停/更新/校验/marketplace；作用域与版本条件 | P1/P2 | [D13](sources.md#d13) |
| Plugin eval | 单独的评测请求和结果分类，不沿用普通 print 成功规则 | P2 | [D14](sources.md#d14) |
| Auth | 状态探测、明确的登录流程及凭据来源；不自动修改宿主登录状态 | P1 | [D16](sources.md#d16) |
| Session/Agent | 命名、恢复、分叉、列表与后台派发结果；终端 attach 独立 | P1/P2 | [D08](sources.md#d08) [D21](sources.md#d21) |
| Remote Control | 区分开启远程的会话参数与常驻 server 子命令，单独生命周期 | P2 | [D15](sources.md#d15) |
| Gateway / Runner | 显式配置、自托管作用域、可终止长进程；不经默认 print 路径 | P2 | [D22](sources.md#d22) |
| 配置导入 | 校正 `importSessions` 命名，按 CLI 配置导入语义提供新入口，保留弃用别名 | P1 | [D01](sources.md#d01) |
| Skills / Hooks 配置 | 管理配置与加载诊断；区分“接收到事件”和“能返回控制决定” | P1/P2 | [D23](sources.md#d23) [D11](sources.md#d11) |

MCP 的 WebSocket 配置不能凭空生成 `--transport ws`；按官方 JSON 入口处理。SSE 与 HTTP 应保留各自兼容状态，不把历史接口“删除”当成升级。破坏性管理操作必须带清楚的目标与范围，支持 dry-run 的命令先暴露 dry-run，绝不在测试中操作真实用户配置。[D12](sources.md#d12)

## 6. Agent SDK 级扩展：有价值，但不冒充 CLI 现成功能

### 工具审批与用户提问

只有 `--permission-mode` 或 `--permission-prompt-tool` 并不代表 Java 已能接收和回答审批。拟议模块应关联 request/session/turn/tool，处理允许、拒绝、输入更新、取消和超时；默认无处理器或等待超时必须明确拒绝或报告未支持，不能自动放行。

官方审批回调并非每一次工具执行都会调用；自动批准路径可能跳过它。需要完整工具审计时应结合其他事件或适用 Hook，不把审批回调当作“所有工具执行前的总拦截器”。[D09](sources.md#d09) [D10](sources.md#d10)

### Hook 回调、自定义工具与 MCP

先完成文件/CLI 配置层的 Hook 和 MCP 管理，再评估 Java 回调。程序化 Hook、自定义工具或进程内 MCP 服务都需要请求—响应协议与异常语义；观察 `hook_*` 或 `tool_use` 事件不是完成这些能力的证据。[D11](sources.md#d11) [D25](sources.md#d25)

### 文件检查点和回退

作为条件式 P2 扩展，不混入会话恢复的基础承诺。文件回退不是事务回滚，也不是聊天历史回滚；不能保证撤销 Bash 等外部操作的所有副作用。[D19](sources.md#d19)

**阶段门槛：**获得固定版本的官方 SDK/CLI 控制协议证据，完成最小契约测试后，再开放相关能力。不能因为公开页面列出 `interrupt()`，就在 Java 适配层臆造 wire 消息格式。本轮未完成该验证。

## 7. 建议技术组织与兼容策略

保留单模块 Maven 工程，先按职责拆包，不急于拆成多个发布物：

```text
保留的 ClaudeCodeClient / ClaudeCodeCli 兼容入口
                         │
                 请求解析与默认配置合并
                         │
          命令意图分类 → 能力判断 → 参数校验
                         │
                 进程运行器与执行句柄
                         │
          stdin writer / stdout decoder / stderr drain
                         │
            协议事件路由 → typed body + raw envelope
                         │
          阻塞聚合 / 实时事件 / 条件式持续会话控制
```

公共模型避免暴露某一 Jackson 主版本的 `JsonNode`。可提供 SDK 自有 JSON value 抽象或原始 JSON 字符串，内部保留不同版本 codec。公共最低能力使用 Java 8 可实现的接口；3.x 内部优化不得改变跨分支协议语义。具体接口和迁移约束见 [OpenSpec 技术设计](../../openspec/changes/align-claude-cli-official-contract/design.md)。

三种路线比较：继续扩展便利方法最省短期工作，但不能解决协议和生命周期；改为官方 Python/TypeScript sidecar 可减少部分协议维护，却增加部署、依赖、认证和进程链；**优先推荐在现有 Java CLI 适配器上补齐运行与协议层**，对未经验证的官方 SDK 高阶控制采取能力门控，而非承诺全面替代。

## 8. 安全边界与运行默认值

本方案要求：安全相关配置不因更换便利方法而丢失；不主动绕过权限；不在构造客户端时登录、安装或更新 CLI；不隐式启动外部服务或删除会话/工作树。令牌、header、环境密钥、prompt 与完整事件内容默认不进普通日志。必要诊断带大小、保留期限和可见范围。

非交互运行可能加载工作目录中的配置与 Hook。宿主调用方必须明确选择可信目录、配置加载策略与隔离方式。`--bare` 的上下文和认证行为不同，应作为显式配置档，而不是本 SDK 为“修复问题”静默加入；组织管理策略始终是外部权威，不在 Java 层提供绕过路径。[D16](sources.md#d16) [D17](sources.md#d17) [D18](sources.md#d18) [D24](sources.md#d24)

SDK 不应在失败后自动重放可能已执行写文件、Shell 或外部工具的整轮请求。重试与续跑需要调用方明确决定，并保留既有结果和副作用不确定性。

## 9. 实施顺序与验收门槛

| 阶段 | 必须完成的内容 | 退出条件 |
|---|---|---|
| A：正确性修复 | F01–F06；真实结构夹具；纠正“期望 null”的测试；默认配置与参数验证 | 三分支离线契约测试一致；数据不丢失；冲突参数零进程启动 |
| B：可控执行 | 实时输出、一次性 stdin、cwd、probe timeout、有界缓存、取消和清理 | 受控假进程测试证明边运行边收事件、期限生效及资源回收 |
| C：原生命令补齐 | 结构化输出、MCP、Plugin、Auth、Session 与后台操作类型化 | 每项有精确 argv、错误分支、作用域和版本证据 |
| D：条件式持续会话 | 多轮输入、审批、Hook 控制、检查点及远程常驻服务 | 对目标 CLI 的协议与平台兼容验证完成；未验证能力明确不可用 |
| E：发布与文档 | 版本兼容表、迁移说明、示例、三分支 CI、依赖与安全验证 | 未把静态分析写成测试通过；没有虚假的“全功能兼容”声明 |

每个阶段都遵循“先写能暴露缺陷的测试，再实现，再回归”。[验收矩阵](acceptance-matrix.md) 列出离线协议、假进程、真实 CLI 和安全测试的区别；[任务清单](../../openspec/changes/align-claude-cli-official-contract/tasks.md) 中实施任务全部未勾选。

不得把覆盖率规则存在当成覆盖率达标，不能把某一次 CI 成功当成跨平台进程生命周期通过。README 中遗留的“没有测试”“没有 CI”“3.x 使用 Maven 3”等描述需依据实际仓库校正；制品是否已发布则另行查询发布系统，不从旧 README 推断。[S09](sources.md#s09) [S10](sources.md#s10) [S11](sources.md#s11) [S16](sources.md#s16)

## 10. 本轮可以确认与尚待验证的内容

已经确认：官方资料范围、三个固定 HEAD、关键源码和测试中的静态问题、补充优先级、目标行为与验收路径。

尚未确认：用户本机 CLI 版本、真实 CLI 全部输出与退出行为、每个高阶控制消息的 wire schema、三分支实际测试结果、Windows/POSIX 子进程清理效果、动态依赖漏洞扫描结果、OpenSpec CLI 校验结果。

**因此这是一份可实施的补充规范，不是一份“已经生产就绪”的认证报告。**
