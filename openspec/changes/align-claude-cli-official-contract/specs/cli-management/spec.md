# cli-management

状态：Proposed；SHALL/MUST 为拟议行为，并非实现完成声明。

## ADDED Requirements

### Requirement: ADM-001 管理与透传边界

The SDK SHALL 声明类型化管理的 scope、交互、返回语义与风险；MUST 保留 raw 但不能等同完整验证能力，管理命令不无条件加 print。

#### Scenario: 只读管理

- **GIVEN** 请求版本或配置状态
- **WHEN** 生成命令
- **THEN** 不注入 Agent print 或无关任务选项。

#### Scenario: raw 调用

- **GIVEN** 显式使用 raw passthrough
- **WHEN** 记录结果
- **THEN** 标明兼容边界，仍遵循资源与日志规则。

### Requirement: ADM-002 MCP 配置和 scope

The SDK SHALL 按官方入口支持传输、明确作用域和敏感字段处理；MUST 分隔父 CLI 选项与 stdio 子命令，不臆造 transport 参数。

#### Scenario: stdio 参数

- **GIVEN** 服务命令含 -- 开头参数
- **WHEN** 构建配置
- **THEN** 父选项与子命令清楚分隔，子参数不变。

#### Scenario: JSON 传输

- **GIVEN** 传输仅可由目标版本 JSON 表达
- **WHEN** 创建请求
- **THEN** 走 JSON 入口或拒绝，不编造 --transport 值。

### Requirement: ADM-003 插件操作与评测

The SDK SHALL 为安装/卸载/启停/更新/校验指定 scope 和专属结果；评测 MUST 独立表达部分完成、失败和中断，不复用 print 结果规则。

#### Scenario: 项目 scope

- **GIVEN** 用户要求项目范围插件操作
- **WHEN** 准备请求
- **THEN** 明确项目目标，不默认修改全局/managed 配置。

#### Scenario: 评测部分完成

- **GIVEN** 支持该命令的 CLI 返回部分完成
- **WHEN** 读取结果
- **THEN** 保留状态/报告，不误报启动失败或普通成功。

### Requirement: ADM-004 认证与敏感输出

The SDK SHALL 分离认证探测与状态修改；MUST NOT 在构造/普通探测时自动登录、注销、安装或升级，令牌结果默认敏感。

#### Scenario: 仅查状态

- **GIVEN** 构造客户端并查询认证
- **WHEN** 执行
- **THEN** 只读，不打开登录流程或改凭据。

#### Scenario: 显式令牌流程

- **GIVEN** 用户明确要求 token 设置/生成
- **WHEN** 收到结果
- **THEN** 仅向明确接收者返回，不进普通日志/默认包。

### Requirement: ADM-005 后台与远程生命周期

The SDK SHALL 区分派发、TTY attach、Remote Control 会话选项、server 和 runner；MUST 分别选择句柄、期限与交互模式。

#### Scenario: 无 TTY attach

- **GIVEN** 无所需终端且无已验证替代协议
- **WHEN** 请求 attach
- **THEN** 立即报告交互需求，不在空 stdin 伪成功或静默等待。

#### Scenario: 常驻服务

- **GIVEN** 显式启动 server 或 runner
- **WHEN** 持续运行
- **THEN** 返回可观测可关闭的自有句柄，不等有限 print 结果。

### Requirement: ADM-006 配置导入和危险操作

The SDK SHALL 按配置导入真实语义命名新入口并保留旧误名弃用别名；删除、清理、强停、丢弃未推送更改 MUST 有明确目标/scope/授权。

#### Scenario: 旧 importSessions

- **GIVEN** 调用旧别名
- **WHEN** 兼容执行
- **THEN** 仍是配置导入并有迁移说明，不宣称导入聊天 transcript。

#### Scenario: 缺少目标

- **GIVEN** 危险请求无目标或 scope
- **WHEN** 验证
- **THEN** 拒绝执行，支持预览时可提供非破坏入口。
