# 验收矩阵与证据规则

**本轮所有测试均未执行。** 每组对应一条行为要求，包含正负场景，可扩展为参数化用例；表格行数不等于实际执行测试数量。

| 测试 | 要求 | 测试层 | 优先级 | 输入与操作 | 通过条件 | 依据 | 状态 |
|---|---|---|---|---|---|---|---|
| T01 | [CMD-001](../../openspec/changes/align-claude-cli-official-contract/specs/cli-command-contract/spec.md) | ARGV/CLI | P0 | 请求同时选择本地 print 和后台启动 → 请求被验证；目标 CLI 能力证据覆盖云创建、既有会话追加和自托管派发 → 分别提交三种请求 | 返回列明冲突的输入错误，子进程启动次数为 0。；按各自契约编码；合法追加和自托管 print 不得被笼统拒绝。 | [S04](sources.md#s04) [D01](sources.md#d01) [D20](sources.md#d20) [D22](sources.md#d22) | 未执行 |
| T02 | [CMD-002](../../openspec/changes/align-claude-cli-official-contract/specs/cli-command-contract/spec.md) | ARGV | P0 | 选择 classic tmux 并提供 worktree → 构建命令；提供非有限预算、无效轮次或损坏 schema JSON → 请求被验证 | 采用目标 CLI 支持的绑定式参数并保留 classic；缺少依赖时拒绝。；返回具体字段错误，不启动收费或有副作用的执行。 | [S04](sources.md#s04) [D01](sources.md#d01) | 未执行 |
| T03 | [CMD-003](../../openspec/changes/align-claude-cli-official-contract/specs/cli-command-contract/spec.md) | ARGV | P0 | 客户端设置工具限制、权限和预算 → 高层重载仅覆盖 model；客户端设 partial=false 或请求设 tools 为空 → 构建高层调用 | 只有 model 改变，其他默认值保留。；显式值不被默认 true 或非空集合覆盖，空 tools 不被当成未设置。 | [S01](sources.md#s01) [S05](sources.md#s05) [S08](sources.md#s08) | 未执行 |
| T04 | [CMD-004](../../openspec/changes/align-claude-cli-official-contract/specs/cli-command-contract/spec.md) | PROCESS/ARGV | P1 | 参数含空格、换行、引号、中文、空值或类似选项的 prompt → 假进程接收调用；可执行路径包含空格 → 执行只读探测 | 接收向量符合入口约定，无意外命令展开。；正确定位文件，启动参数不从文件路径中误拆。 | [S02](sources.md#s02) | 未执行 |
| T05 | [CMD-005](../../openspec/changes/align-claude-cli-official-contract/specs/cli-command-contract/spec.md) | CONTRACT/CLI | P1 | 选项未出现在 help 且无其他不支持证据 → 查询能力；目标 CLI 明确不支持必需安全选项 → 构建类型化请求 | 报告未知或依据其他可信证据判断，不仅凭 help 缺失断言不支持。；拒绝并报告兼容问题，不去掉安全选项继续执行。 | [D01](sources.md#d01) | 未执行 |
| T06 | [MSG-001](../../openspec/changes/align-claude-cli-official-contract/specs/cli-message-protocol/spec.md) | CODEC | P0 | result 含正文 final、费用 0.05 和 usage → 读取类型化结果；同时使用旧消息对象和新结果接口 → 解析同一结果 | 值与输入一致，未知字段可经受控 raw 接口获取。；旧投影不能改变新结果保存的数据。 | [S01](sources.md#s01) [S03](sources.md#s03) [S07](sources.md#s07) | 未执行 |
| T07 | [MSG-002](../../openspec/changes/align-claude-cli-official-contract/specs/cli-message-protocol/spec.md) | CODEC | P0 | assistant.message 是含 content 的对象 → 解析事件；输入包含当前模型不认识的事件 → 宽松解析 | 得到结构化消息和内容块，不强转为字符串。；生成未知事件并保留类型和 raw，事件数量不减少。 | [S03](sources.md#s03) [S07](sources.md#s07) [D04](sources.md#d04) | 未执行 |
| T08 | [MSG-003](../../openspec/changes/align-claude-cli-official-contract/specs/cli-message-protocol/spec.md) | CODEC | P0 | usage 含两个官方缓存字段 → 解析用量；规范字段和旧别名同时出现且不同 → 解析结果 | 分别保留，省略指标保持缺失而非零。；规范字段优先并输出冲突诊断，不相加。 | [S06](sources.md#s06) [D07](sources.md#d07) | 未执行 |
| T09 | [MSG-004](../../openspec/changes/align-claude-cli-official-contract/specs/cli-message-protocol/spec.md) | CODEC/CLI | P0/P1 | schema 请求返回 structured_output → 映射目标类型；请求需要结构化值而响应缺失 → 完成请求 | 结构化值和元数据均可用，正文字符串不是唯一入口。；明确结构化输出失败并保留诊断，不伪造空对象成功。 | [D06](sources.md#d06) [S05](sources.md#s05) | 未执行 |
| T10 | [MSG-005](../../openspec/changes/align-claude-cli-official-contract/specs/cli-message-protocol/spec.md) | CODEC/SECURITY | P0/P1 | 有效帧间夹损坏 JSON → 宽松解析；坏帧含测试密钥或私有 prompt → 严格失败并生成日志 | 有效帧保留，坏帧数量/位置可见，缺必要 result 不假成功。；返回解析错误，默认日志不含原始敏感正文。 | [S01](sources.md#s01) | 未执行 |
| T11 | [MSG-006](../../openspec/changes/align-claude-cli-official-contract/specs/cli-message-protocol/spec.md) | CODEC | P1 | 同一消息先有多个 delta 后有完整消息 → 聚合展示和用量；持续会话有两个 turn result 和 parent_tool_use_id → 读取历史 | 正文不重复，按标识和计量范围去重，不简单逐事件/累计值相加。；每轮结果独立保存，父工具可追踪，不推断不存在的全局时间顺序。 | [D04](sources.md#d04) [D07](sources.md#d07) | 未执行 |
| T12 | [RUN-001](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS | P1 | 假进程输出第一帧后等待宿主信号 → 宿主订阅事件；中文 UTF-8 和换行拆成多段 → 持续解码 | 退出前收到首帧，不依赖结束后的批解析。；字符与事件正确，stderr 不混入 JSON 通道。 | [S02](sources.md#s02) [S05](sources.md#s05) | 未执行 |
| T13 | [RUN-002](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS | P1 | 输入含多字节文本 → 假进程读到 EOF；当前适配器只支持一次性 → 创建持续会话 | 字节保真且按约定关闭输入，不等待继承终端。；明确报告未实现，不返回伪造可写句柄。 | [S02](sources.md#s02) [D05](sources.md#d05) | 未执行 |
| T14 | [RUN-003](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS/CONCURRENCY | P1 | 两请求 cwd/env 不同 → 假进程回报 cwd/测试变量；运行开始后修改客户端默认值 → 观察旧运行和新运行 | 彼此不串扰，不导出无关敏感变量。；旧运行保留快照，新运行按明确规则读取新配置。 | [S02](sources.md#s02) [S08](sources.md#s08) | 未执行 |
| T15 | [RUN-004](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS | P1 | probe 期限短于 run 且假进程挂起 → 调用 probe；子进程结束但读取链未在 drain 期限完成 → 收尾 | 在 probe 期限及清理容差内返回，不等待 run 期限。；返回不完整诊断并结束自有资源，不无限等待。 | [S02](sources.md#s02) [S08](sources.md#s08) | 未执行 |
| T16 | [RUN-005](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS/LOAD | P1 | 输出速度超过消费速度且队列满 → 达到容量；累计输出超出保留容量 → 继续执行或限制终止 | 依有期限等待、受控暂存或取消策略处理并诊断。；保留范围与截断可见，不宣称 raw 完整。 | [S02](sources.md#s02) | 未执行 |
| T17 | [RUN-006](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS/PLATFORM | P1 | 自有任务运行中 → 并发 cancel、close 与自然完成；自有进程树拒绝退出 → 清理期限到达 | 只产生一个终态，可重复收尾且不误杀其他任务。；按平台策略升级终止并记录，未验证不能报完全清理成功。 | [S01](sources.md#s01) [S02](sources.md#s02) [D28](sources.md#d28) | 未执行 |
| T18 | [RUN-007](../../openspec/changes/align-claude-cli-official-contract/specs/cli-process-runtime/spec.md) | PROCESS/CODEC | P1 | 假进程写双路输出并以测试码结束 → 收尾；版本查询无 result，Agent 流缺必要 result → 分别判断状态 | 实际码和有界 stdout/stderr 保留，不压为无信息 -1。；管理查询不因缺 Agent envelope 失败；Agent 请求报告协议不完整。 | [S02](sources.md#s02) [D02](sources.md#d02) | 未执行 |
| T19 | [SES-001](../../openspec/changes/align-claude-cli-official-contract/specs/cli-session-control/spec.md) | SESSION | P2 | 通过 session ID 恢复 → 创建运行；两个调用向不可并行 turn 的句柄发送 → 进入会话 | 关联旧会话，运行/进程所有权独立可查。；按队列或 BUSY 规则处理，不混写 NDJSON。 | [D08](sources.md#d08) | 未执行 |
| T20 | [SES-002](../../openspec/changes/align-claude-cli-official-contract/specs/cli-session-control/spec.md) | SESSION/PROCESS | P2 | 先后接收两个输入 → 分别收到完成；调用方结束发送并等结果 → 再次发送 | 两轮独立关联，首轮 result 不等于进程结束。；明确输入已关闭，既有输出仍按规则消费。 | [D05](sources.md#d05) | 未执行 |
| T21 | [SES-003](../../openspec/changes/align-claude-cli-official-contract/specs/cli-session-control/spec.md) | PROTOCOL/CLI | 条件式 P2 | Hook/plugin 事件先于 system/init → 初始化；目标缺少能力且无验证适配规则 → 调用高级控制 | 保留事件等待能力信息，不要求 init 必须第一帧。；返回未知或不支持，不发送猜测消息。 | [D02](sources.md#d02) | 未执行 |
| T22 | [SES-004](../../openspec/changes/align-claude-cli-official-contract/specs/cli-session-control/spec.md) | PROTOCOL/SECURITY | 条件式 P2 | 审批等待超出宿主期限 → 结束等待；审批已取消，或有效允许响应带修改输入 → 处理响应 | 拒绝或失败并保留原因，不默认允许。；迟到无效；合法修改只影响对应请求且可审计。 | [D09](sources.md#d09) [D10](sources.md#d10) | 未执行 |
| T23 | [SES-005](../../openspec/changes/align-claude-cli-official-contract/specs/cli-session-control/spec.md) | PROTOCOL | 条件式 P2 | 可读 Hook 事件但无控制协议 → 查询能力或请求控制；CLI 既有规则自动批准工具 → 审计依赖订阅 | 只报告观察；控制请求明确不可用。；不承诺每次触发审批，审计由其他已验证事件/Hook 承担。 | [D11](sources.md#d11) [D23](sources.md#d23) | 未执行 |
| T24 | [SES-006](../../openspec/changes/align-claude-cli-official-contract/specs/cli-session-control/spec.md) | CLI/FILESYSTEM | 条件式 P2 | 已验证检查点且产生受跟踪编辑 → 请求回退；外部命令/服务产生未跟踪效果 → 要求全部撤销 | 报告影响文件及结果，会话控制保持独立语义。；明确范围限制，不承诺撤销未跟踪行为。 | [D19](sources.md#d19) | 未执行 |
| T25 | [ADM-001](../../openspec/changes/align-claude-cli-official-contract/specs/cli-management/spec.md) | ARGV/RESULT | P1 | 请求版本或配置状态 → 生成命令；显式使用 raw passthrough → 记录结果 | 不注入 Agent print 或无关任务选项。；标明兼容边界，仍遵循资源与日志规则。 | [S12](sources.md#s12) | 未执行 |
| T26 | [ADM-002](../../openspec/changes/align-claude-cli-official-contract/specs/cli-management/spec.md) | ARGV/CLI | P1 | 服务命令含 -- 开头参数 → 构建配置；传输仅可由目标版本 JSON 表达 → 创建请求 | 父选项与子命令清楚分隔，子参数不变。；走 JSON 入口或拒绝，不编造 --transport 值。 | [D12](sources.md#d12) [S12](sources.md#s12) | 未执行 |
| T27 | [ADM-003](../../openspec/changes/align-claude-cli-official-contract/specs/cli-management/spec.md) | ARGV/CLI | P1/P2 | 用户要求项目范围插件操作 → 准备请求；支持该命令的 CLI 返回部分完成 → 读取结果 | 明确项目目标，不默认修改全局/managed 配置。；保留状态/报告，不误报启动失败或普通成功。 | [D13](sources.md#d13) [D14](sources.md#d14) | 未执行 |
| T28 | [ADM-004](../../openspec/changes/align-claude-cli-official-contract/specs/cli-management/spec.md) | MANAGEMENT/SECURITY | P1 | 构造客户端并查询认证 → 执行；用户明确要求 token 设置/生成 → 收到结果 | 只读，不打开登录流程或改凭据。；仅向明确接收者返回，不进普通日志/默认包。 | [D16](sources.md#d16) [S12](sources.md#s12) | 未执行 |
| T29 | [ADM-005](../../openspec/changes/align-claude-cli-official-contract/specs/cli-management/spec.md) | PROCESS/CLI | P2 | 无所需终端且无已验证替代协议 → 请求 attach；显式启动 server 或 runner → 持续运行 | 立即报告交互需求，不在空 stdin 伪成功或静默等待。；返回可观测可关闭的自有句柄，不等有限 print 结果。 | [D15](sources.md#d15) [D21](sources.md#d21) [D22](sources.md#d22) | 未执行 |
| T30 | [ADM-006](../../openspec/changes/align-claude-cli-official-contract/specs/cli-management/spec.md) | ARGV/SAFETY | P1 | 调用旧别名 → 兼容执行；危险请求无目标或 scope → 验证 | 仍是配置导入并有迁移说明，不宣称导入聊天 transcript。；拒绝执行，支持预览时可提供非破坏入口。 | [D01](sources.md#d01) [S12](sources.md#s12) | 未执行 |
| T31 | [COM-001](../../openspec/changes/align-claude-cli-official-contract/specs/cli-compatibility-evidence/spec.md) | CI/PARITY | 所有阶段 | 三分支读取相同 JSON → 运行契约测试；低 JVM 不支持高版本内部实现 → 移植 | 规范化输出相同，允许差异有证据。；兼容实现或明确不可用，不静默改变语义。 | [S09](sources.md#s09) [S13](sources.md#s13) [S14](sources.md#s14) | 未执行 |
| T32 | [COM-002](../../openspec/changes/align-claude-cli-official-contract/specs/cli-compatibility-evidence/spec.md) | EVIDENCE | 所有阶段 | 新增合成 JSON → 记录来源；能力仅见当日官方页 → 发布说明 | 标 synthetic，不当真实 CLI 回放。；记录日期/来源，不宣称全部目标版本已验证。 | [S15](sources.md#s15) | 未执行 |
| T33 | [COM-003](../../openspec/changes/align-claude-cli-official-contract/specs/cli-compatibility-evidence/spec.md) | CONTRACT/CLI | P1 | 旧 init 不含可选 capabilities → 解析；实际版本低于已知要求 → 验证类型化请求 | 保留未知，基础已验证能力仍可用，不崩溃。；报告版本条件，不用未知组合绕过。 | [D01](sources.md#d01) [D02](sources.md#d02) | 未执行 |
| T34 | [COM-004](../../openspec/changes/align-claude-cli-official-contract/specs/cli-compatibility-evidence/spec.md) | TEST-QUALITY | P0/发布 | 旧断言接受正文/费用 null → 修复契约；没有 Java 或真实 CLI 执行 → 完成声明 | 先增加正确值失败用例，修复后记录通过。；只声明实际文档检查，运行/CI/覆盖率/安全保持未验证。 | [S07](sources.md#s07) [S10](sources.md#s10) | 未执行 |
| T35 | [COM-005](../../openspec/changes/align-claude-cli-official-contract/specs/cli-compatibility-evidence/spec.md) | API/BUILD | 所有阶段 | 使用现有 print → 升级；使用公共模型代码在 1/2 与 3 间迁移 → 编译样例 | 兼容入口可调用，错误结果不再是兼容承诺。；不需要在不同 Jackson JsonNode 包间换公共类型。 | [S09](sources.md#s09) [S13](sources.md#s13) [S14](sources.md#s14) | 未执行 |
| T36 | [COM-006](../../openspec/changes/align-claude-cli-official-contract/specs/cli-compatibility-evidence/spec.md) | SECURITY/OPERATIONS | 所有阶段 | 请求含凭据、私有 prompt、工具输入或完整事件 → 导出日志/包；运行可能已写文件/调用外部服务且结果不完整 → 处理失败 | 敏感正文不默认暴露；授权 raw 有保留范围和清理。；报告副作用不确定性，不擅自重放整轮。 | [D16](sources.md#d16) [D17](sources.md#d17) [D18](sources.md#d18) [D24](sources.md#d24) | 未执行 |


## 先纠正测试证明错误的情况

T06 首先把 `shouldPrintStreamJsonAndParseReturnFinalResult` 等测试的正文/费用 null 断言改成实际值断言，在旧实现上看到失败，再修复并记录三线通过。[S07](sources.md#s07)

T07 加入嵌套 message/content，不只用字符串 message；T03 检查完整有效默认权限、工具、预算，不只测 model 和 -p 是否出现。

## 合成示例与真实录制

[示例 NDJSON](../../evidence/claude-cli-alignment/synthetic-protocol-examples.ndjson) 是人工构造、未执行的说明数据，不是任何 CLI 的完整真实录制；[元数据](../../evidence/claude-cli-alignment/synthetic-protocol-examples.metadata.json) 明确标记来源。

真实录制须记录 CLI 完整版本、OS、模式、录制命令、脱敏规则与必要环境；不保留秘密或无关仓库内容。合成数据、假进程测试和真实互操作证据不能互相替代。

## 运行证据与验收记录

Codec/argv 证明本地映射；假进程证明读写、期限、背压、取消与清理；真实 CLI 证明特定版本的互操作。网络失败分清认证、限流、兼容或 SDK 缺陷，不统一推给环境，不盲重试副作用。

T17 在支持平台反复执行，记录进程、句柄/线程、临时文件基线及明确容差；一次无异常不能证明无泄漏。

每条记录至少含 requirement_id、test_id、源码 commit、CLI/SDK、JDK、OS、输入来源、命令摘要、预期、实际、退出状态、诊断位置与限制。跳过写原因，未运行写 NOT_RUN，不记 PASS。

## 发布门槛

按各分支 Maven/Java 基线构建，3.x 使用对应 Maven 4 wrapper。真实 CLI 测试显式启用、限制工作区/预算，默认单测不用用户全局 Claude 配置。三个对应提交的证据齐全后才能宣称同步完成。

本次文档结构检查不等于 `openspec validate --strict`、Maven 或真实 CLI 通过。
