# 能力差异矩阵

基线：2026-09-20，三个固定 HEAD 见 [基线](compatibility-baseline.json)。这不是支持率打分；raw 存在不等于高层完整，缺便利方法不等于完全不能调用。所有目标为 proposed。

| ID | 能力 | 当前情况 | 证据性质 | 关联 | 优先级 | 补充 | 要求 | 依据 |
|---|---|---|---|---|---|---|---|---|
| C01 | 结果正文/费用 | 中间投影丢字段 | 静态确认 | F01 | P0 | 无损原始 result | MSG-001 | [S01](sources.md#s01) [S03](sources.md#s03) [S07](sources.md#s07) |
| C02 | 消息/内容块 | message 为 String | 协议风险待实测 | F02 | P0 | 嵌套 body/delta/unknown | MSG-002 | [S03](sources.md#s03) [S07](sources.md#s07) [D04](sources.md#d04) |
| C03 | 缓存用量 | 映射字段与官方不符 | 静态确认 | F03 | P0 | 规范名/别名/缺失/估算范围 | MSG-003 | [S06](sources.md#s06) [D07](sources.md#d07) |
| C04 | 结构化输出 | 已有 schema 参数 | 低层已有，高层不足 | F06 | P0/P1 | structured result 与错误分类 | MSG-004 | [S05](sources.md#s05) [D06](sources.md#d06) |
| C05 | print/bg/cloud | builder 可同时给 -p/--bg | 静态确认 | F04 | P0 | 意图分离和合法云追加例外 | CMD-001 | [S04](sources.md#s04) [D01](sources.md#d01) [D20](sources.md#d20) [D22](sources.md#d22) |
| C06 | 选项编码 | debug/tmux 编码不统一 | 静态确认 | F06 | P0 | 逐 token 和组合验证 | CMD-002 | [S04](sources.md#s04) [S12](sources.md#s12) [D01](sources.md#d01) |
| C07 | 默认配置 | 重载遗漏，false 未覆盖 | 静态确认 | F05 | P0 | 逐字段和显式赋值 | CMD-003 | [S01](sources.md#s01) [S05](sources.md#s05) [S08](sources.md#s08) |
| C08 | argv/执行路径 | 执行目标仍被 parse | 静态风险需实测 | F06/F09 | P1 | path/argv 分离与边界保真 | CMD-004 | [S02](sources.md#s02) |
| C09 | 实时 stdout | 退出后解析 | 静态确认 | F07 | P1 | 运行中事件与独立 stderr | RUN-001 | [S02](sources.md#s02) [S05](sources.md#s05) [D04](sources.md#d04) |
| C10 | 一次性 stdin | 已有底层入口 | 低层已有，高层不足 | F08 | P1 | 输入/EOF 与持续能力分开 | RUN-002 | [S02](sources.md#s02) [S05](sources.md#s05) [D05](sources.md#d05) |
| C11 | cwd/隔离 | 未设 cwd，引用可变配置 | 静态确认 | F09 | P1 | 每调用 cwd/env 快照 | RUN-003 | [S02](sources.md#s02) [S08](sources.md#s08) |
| C12 | probe/期限 | probe 走普通执行 | 静态确认 | F09 | P1 | probe/start/run/drain 分离 | RUN-004 | [S02](sources.md#s02) [S08](sources.md#s08) |
| C13 | 容量/背压 | 全量内存缓存 | 静态确认 | F07/F09 | P1 | 有界队列/raw/诊断 | RUN-005 | [S02](sources.md#s02) |
| C14 | 取消/close | watchdog 有，client close 空 | 低层已有，高层不足 | F09 | P1 | 自有句柄与幂等限时清理 | RUN-006 | [S01](sources.md#s01) [S02](sources.md#s02) [D28](sources.md#d28) |
| C15 | 失败完整性 | 非零码保留，缺分层 | 低层已有，高层不足 | F10 | P1 | OS/终止/协议/业务分类 | RUN-007 | [S02](sources.md#s02) [D02](sources.md#d02) |
| C16 | 解析诊断 | 坏行可能静默丢弃 | 静态确认 | F02/F10 | P0/P1 | strict/lenient 与问题列表 | MSG-005 | [S01](sources.md#s01) |
| C17 | 文本/计量聚合 | 缺去重契约 | 拟议新增 | F03/F07 | P1 | message/turn/parent 与范围 | MSG-006 | [D04](sources.md#d04) [D07](sources.md#d07) |
| C18 | session/resume | 已有参数方法 | 低层已有，高层不足 | F08 | P1/P2 | 身份/所有权和并发策略 | SES-001 | [S12](sources.md#s12) [D08](sources.md#d08) |
| C19 | 持续双向 | 一次性输入不构成会话 | 静态确认 | F08 | P2 | 多轮/写入/背压/半关闭 | SES-002 | [S02](sources.md#s02) [S05](sources.md#s05) [D05](sources.md#d05) |
| C20 | 控制协商 | 无验证控制通道 | 拟议新增 | F08/F10 | 条件式 P2 | 固定协议，不发猜测消息 | SES-003 | [D02](sources.md#d02) [D05](sources.md#d05) |
| C21 | 审批/用户提问 | 权限参数有，回调不足 | 低层已有，高层不足 | 扩展 | 条件式 P2 | 关联/超时/拒绝/取消/迟到 | SES-004 | [D09](sources.md#d09) [D10](sources.md#d10) |
| C22 | Hook | 观察参数不等于控制 | 低层已有，高层不足 | 扩展 | 条件式 P2 | 配置/观察与控制分离 | SES-005 | [S12](sources.md#s12) [D11](sources.md#d11) [D23](sources.md#d23) |
| C23 | 检查点 | 缺高层 Java 契约 | 拟议新增 | 扩展 | 条件式 P2 | 有限回退范围 | SES-006 | [D19](sources.md#d19) |
| C24 | MCP | 基础方法和 raw 已有 | 低层已有，高层不足 | 扩展 | P1 | 分隔/config/scope/认证对象 | ADM-002 | [S12](sources.md#s12) [D12](sources.md#d12) |
| C25 | Plugin/eval | 基础与 raw 已有 | 低层已有，高层不足 | 扩展 | P1/P2 | 类型化/scope/评测状态 | ADM-003 | [S12](sources.md#s12) [D13](sources.md#d13) [D14](sources.md#d14) |
| C26 | Auth | auth/setup-token 已有 | 低层已有，高层不足 | 扩展 | P1 | 只读/显式交互/秘密保护 | ADM-004 | [S12](sources.md#s12) [D16](sources.md#d16) |
| C27 | 后台/远程 | 同步执行未区分 TTY/server | 低层已有，高层不足 | 扩展 | P2 | 管理/交互/服务句柄 | ADM-005 | [S12](sources.md#s12) [D15](sources.md#d15) [D21](sources.md#d21) [D22](sources.md#d22) |
| C28 | 导入/清理 | importSessions 命名误导 | 语义核对 | 扩展 | P1 | 弃用别名/目标/授权 | ADM-006 | [S12](sources.md#s12) [D01](sources.md#d01) |
| C29 | 版本能力 | probe 为布尔可执行 | 低层已有，高层不足 | 扩展 | P1 | 多态状态/证据/只读探测 | CMD-005,COM-003 | [S02](sources.md#s02) [D01](sources.md#d01) [D02](sources.md#d02) |
| C30 | 三线兼容 | 共享业务与分叉依赖 | 已有维护基础 | 横向 | 全部 | 同夹具/最低 JDK/迁移 | COM-001,COM-005 | [S09](sources.md#s09) [S13](sources.md#s13) [S14](sources.md#s14) [S15](sources.md#s15) |
| C31 | 测试/文档 | 错误断言及 README 漂移 | 静态确认 | 横向 | P0/发布 | 正确回归和分层证据 | COM-002,COM-004 | [S07](sources.md#s07) [S10](sources.md#s10) [S16](sources.md#s16) |
| C32 | 秘密/副作用 | 缺统一诊断与重试契约 | 拟议新增 | 横向 | 全部 | 不降权限/脱敏/不盲重放 | COM-006 | [S02](sources.md#s02) [D16](sources.md#d16) [D18](sources.md#d18) [D24](sources.md#d24) |
| C33 | 管理统一契约 | raw/便利方法并存 | 低层已有，高层不足 | 横向 | P1 | 模式/scope/风险/结果规则 | ADM-001 | [S12](sources.md#s12) |


## 不能误列为全新缺失

advisor、max-turns、permission-prompts、restricted、safe-mode、部分 cloud/background、Hook/子 Agent 选项已出现在 PrintOptions。此方案补的是模式适用、默认一致、参数取值与版本证据，不能把同名 facade 新方法当从零新增能力。MCP/plugin/gateway 已有 raw，类型化补充必须说明新增验证和结果契约。[S04](sources.md#s04) [S12](sources.md#s12)

F01–F06 大部分不依赖高级 Session，应先修明确缺陷；控制协议未固定的审批、Hook、检查点不可随持续输入一起宣称完成。
