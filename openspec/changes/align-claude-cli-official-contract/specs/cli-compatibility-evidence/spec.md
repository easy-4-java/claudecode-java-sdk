# cli-compatibility-evidence

状态：Proposed；SHALL/MUST 为拟议行为，并非实现完成声明。

## ADDED Requirements

### Requirement: COM-001 三线共同契约

The SDK SHALL 保持三版本公共行为一致；MUST 以同一协议和 argv 夹具验证，仅允许已记录的 JDK/JSON/平台差异。

#### Scenario: 相同协议输入

- **GIVEN** 三分支读取相同 JSON
- **WHEN** 运行契约测试
- **THEN** 规范化输出相同，允许差异有证据。

#### Scenario: 低版本限制

- **GIVEN** 低 JVM 不支持高版本内部实现
- **WHEN** 移植
- **THEN** 兼容实现或明确不可用，不静默改变语义。

### Requirement: COM-002 版本和夹具可追踪

The SDK SHALL 记录源码提交、CLI/SDK、OS 与来源；MUST 区分合成数据、假进程和真实录制，无证据能力不标已验证。

#### Scenario: 人工夹具

- **GIVEN** 新增合成 JSON
- **WHEN** 记录来源
- **THEN** 标 synthetic，不当真实 CLI 回放。

#### Scenario: 滚动文档

- **GIVEN** 能力仅见当日官方页
- **WHEN** 发布说明
- **THEN** 记录日期/来源，不宣称全部目标版本已验证。

### Requirement: COM-003 能力证据与版本检测

The SDK SHALL 综合官方契约、版本、已验证探测与运行时元数据判断能力；MUST 解释未知和不匹配，不把最新字段当所有版本必有。

#### Scenario: 旧版缺能力字段

- **GIVEN** 旧 init 不含可选 capabilities
- **WHEN** 解析
- **THEN** 保留未知，基础已验证能力仍可用，不崩溃。

#### Scenario: 最低版本不符

- **GIVEN** 实际版本低于已知要求
- **WHEN** 验证类型化请求
- **THEN** 报告版本条件，不用未知组合绕过。

### Requirement: COM-004 验证不得伪装

The SDK SHALL 分别记录协议、假进程、真实 CLI、安全与三线测试；MUST NOT 将文档校验、测试存在、覆盖率配置当功能通过；回归须断言正确结果。

#### Scenario: 旧测试固定丢数据

- **GIVEN** 旧断言接受正文/费用 null
- **WHEN** 修复契约
- **THEN** 先增加正确值失败用例，修复后记录通过。

#### Scenario: 只有文档

- **GIVEN** 没有 Java 或真实 CLI 执行
- **WHEN** 完成声明
- **THEN** 只声明实际文档检查，运行/CI/覆盖率/安全保持未验证。

### Requirement: COM-005 公共 API 兼容与迁移

The SDK SHALL 为旧门面/阻塞入口提供迁移；MUST 说明修复的可见行为变化，公共 API 不要求特定 Jackson 主版本或高于分支基线的 Java API。

#### Scenario: 旧阻塞入口

- **GIVEN** 使用现有 print
- **WHEN** 升级
- **THEN** 兼容入口可调用，错误结果不再是兼容承诺。

#### Scenario: JSON 版本迁移

- **GIVEN** 使用公共模型代码在 1/2 与 3 间迁移
- **WHEN** 编译样例
- **THEN** 不需要在不同 Jackson JsonNode 包间换公共类型。

### Requirement: COM-006 安全诊断与副作用边界

The SDK SHALL 对诊断/遥测/自动化实施最小权限和保留规则；MUST 区分 cwd/worktree/bare/sandbox，不绕过组织规则或自动重放有副作用整轮。

#### Scenario: 默认诊断

- **GIVEN** 请求含凭据、私有 prompt、工具输入或完整事件
- **WHEN** 导出日志/包
- **THEN** 敏感正文不默认暴露；授权 raw 有保留范围和清理。

#### Scenario: 失败后重试

- **GIVEN** 运行可能已写文件/调用外部服务且结果不完整
- **WHEN** 处理失败
- **THEN** 报告副作用不确定性，不擅自重放整轮。
