# cli-message-protocol

状态：Proposed；SHALL/MUST 为拟议行为，并非实现完成声明。

## ADDED Requirements

### Requirement: MSG-001 无损 envelope 与结果保留

The SDK SHALL 保留有效事件原始 envelope，从原始 result 解析正文、费用、usage 和扩展；MUST NOT 经过缺字段的旧消息对象再恢复结果。

#### Scenario: 结果字段不丢失

- **GIVEN** result 含正文 final、费用 0.05 和 usage
- **WHEN** 读取类型化结果
- **THEN** 值与输入一致，未知字段可经受控 raw 接口获取。

#### Scenario: 兼容投影不是原始真相

- **GIVEN** 同时使用旧消息对象和新结果接口
- **WHEN** 解析同一结果
- **THEN** 旧投影不能改变新结果保存的数据。

### Requirement: MSG-002 结构化消息与未知事件

The SDK SHALL 区分嵌套消息、增量事件、system 子类型、result 和内容块；MUST 保留未知类型，不静默丢弃或把不存在字段伪造为空。

#### Scenario: 嵌套 assistant

- **GIVEN** assistant.message 是含 content 的对象
- **WHEN** 解析事件
- **THEN** 得到结构化消息和内容块，不强转为字符串。

#### Scenario: 新类型

- **GIVEN** 输入包含当前模型不认识的事件
- **WHEN** 宽松解析
- **THEN** 生成未知事件并保留类型和 raw，事件数量不减少。

### Requirement: MSG-003 用量字段与结果状态

The SDK SHALL 正确读取 cache_creation_input_tokens 和 cache_read_input_tokens，允许旧输入别名；MUST 区分缺失与零并保留业务状态及未知字段。费用为估算，不是结算值。

#### Scenario: 缓存字段

- **GIVEN** usage 含两个官方缓存字段
- **WHEN** 解析用量
- **THEN** 分别保留，省略指标保持缺失而非零。

#### Scenario: 别名冲突

- **GIVEN** 规范字段和旧别名同时出现且不同
- **WHEN** 解析结果
- **THEN** 规范字段优先并输出冲突诊断，不相加。

### Requirement: MSG-004 结构化输出契约

The SDK SHALL 为 schema 请求提供独立结构化结果；MUST 区分进程执行失败、业务失败、结构化值缺失和本地映射失败，不能统一为成功空值。

#### Scenario: 结构化成功

- **GIVEN** schema 请求返回 structured_output
- **WHEN** 映射目标类型
- **THEN** 结构化值和元数据均可用，正文字符串不是唯一入口。

#### Scenario: 结构化值缺失

- **GIVEN** 请求需要结构化值而响应缺失
- **WHEN** 完成请求
- **THEN** 明确结构化输出失败并保留诊断，不伪造空对象成功。

### Requirement: MSG-005 解析问题可见且受控

The SDK SHALL 提供严格和宽松策略及有界问题清单；MUST 区分空行、未知类型、坏 JSON 和截断，诊断/日志遵循敏感数据规则。

#### Scenario: 宽松遇坏帧

- **GIVEN** 有效帧间夹损坏 JSON
- **WHEN** 宽松解析
- **THEN** 有效帧保留，坏帧数量/位置可见，缺必要 result 不假成功。

#### Scenario: 严格与秘密

- **GIVEN** 坏帧含测试密钥或私有 prompt
- **WHEN** 严格失败并生成日志
- **THEN** 返回解析错误，默认日志不含原始敏感正文。

### Requirement: MSG-006 顺序、轮次与去重

The SDK SHALL 保留运行内顺序、会话/父工具关联，按 turn 区分结果；MUST 不重复聚合增量与完整消息的正文或费用，并保留累计计量的范围和重置边界。

#### Scenario: 增量加全量

- **GIVEN** 同一消息先有多个 delta 后有完整消息
- **WHEN** 聚合展示和用量
- **THEN** 正文不重复，按标识和计量范围去重，不简单逐事件/累计值相加。

#### Scenario: 多轮和子 Agent

- **GIVEN** 持续会话有两个 turn result 和 parent_tool_use_id
- **WHEN** 读取历史
- **THEN** 每轮结果独立保存，父工具可追踪，不推断不存在的全局时间顺序。
