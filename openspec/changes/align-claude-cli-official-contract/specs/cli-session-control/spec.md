# cli-session-control

状态：Proposed；SHALL/MUST 为拟议行为，并非实现完成声明。

## ADDED Requirements

### Requirement: SES-001 会话身份与进程所有权分离

The SDK SHALL 区分持久 session、run 与进程句柄；MUST 串行化或明确拒绝同进程并发写入，resume 不隐式改变其他调用会话。

#### Scenario: 恢复新运行

- **GIVEN** 通过 session ID 恢复
- **WHEN** 创建运行
- **THEN** 关联旧会话，运行/进程所有权独立可查。

#### Scenario: 并发发送

- **GIVEN** 两个调用向不可并行 turn 的句柄发送
- **WHEN** 进入会话
- **THEN** 按队列或 BUSY 规则处理，不混写 NDJSON。

### Requirement: SES-002 持续多轮与半关闭

The SDK SHALL 在声明持续会话时支持顺序发送、多轮结果、发送背压和输入半关闭；MUST 拒绝半关闭后新输入并限时排空旧输出。

#### Scenario: 两个回合

- **GIVEN** 先后接收两个输入
- **WHEN** 分别收到完成
- **THEN** 两轮独立关联，首轮 result 不等于进程结束。

#### Scenario: 半关闭

- **GIVEN** 调用方结束发送并等结果
- **WHEN** 再次发送
- **THEN** 明确输入已关闭，既有输出仍按规则消费。

### Requirement: SES-003 控制协议能力确认

The SDK SHALL 仅在固定版本协议证据和适配器验证满足后开放 interrupt/审批等控制；MUST 兼容 init 前合法事件与未知能力，不自创 wire 格式。

#### Scenario: init 前事件

- **GIVEN** Hook/plugin 事件先于 system/init
- **WHEN** 初始化
- **THEN** 保留事件等待能力信息，不要求 init 必须第一帧。

#### Scenario: 能力未知

- **GIVEN** 目标缺少能力且无验证适配规则
- **WHEN** 调用高级控制
- **THEN** 返回未知或不支持，不发送猜测消息。

### Requirement: SES-004 审批和提问生命周期

The SDK SHALL 关联审批 request/session/turn；MUST 处理取消、超时和迟到响应，无处理器不隐式批准，宿主规则不能放宽 CLI 组织权限。

#### Scenario: 审批无人响应

- **GIVEN** 审批等待超出宿主期限
- **WHEN** 结束等待
- **THEN** 拒绝或失败并保留原因，不默认允许。

#### Scenario: 迟到与输入修改

- **GIVEN** 审批已取消，或有效允许响应带修改输入
- **WHEN** 处理响应
- **THEN** 迟到无效；合法修改只影响对应请求且可审计。

### Requirement: SES-005 观察与控制 Hook 分离

The SDK SHALL 区分观察事件和可返回决定的 Hook；MUST NOT 将收到事件等同于具有阻止/修改能力。

#### Scenario: 仅观察

- **GIVEN** 可读 Hook 事件但无控制协议
- **WHEN** 查询能力或请求控制
- **THEN** 只报告观察；控制请求明确不可用。

#### Scenario: 审批不覆盖全部工具

- **GIVEN** CLI 既有规则自动批准工具
- **WHEN** 审计依赖订阅
- **THEN** 不承诺每次触发审批，审计由其他已验证事件/Hook 承担。

### Requirement: SES-006 检查点范围

The SDK SHALL 仅开放已验证范围的文件回退；MUST 区分文件、对话与外部副作用，不能宣传为全局事务撤销。

#### Scenario: 受支持编辑

- **GIVEN** 已验证检查点且产生受跟踪编辑
- **WHEN** 请求回退
- **THEN** 报告影响文件及结果，会话控制保持独立语义。

#### Scenario: 外部副作用

- **GIVEN** 外部命令/服务产生未跟踪效果
- **WHEN** 要求全部撤销
- **THEN** 明确范围限制，不承诺撤销未跟踪行为。
