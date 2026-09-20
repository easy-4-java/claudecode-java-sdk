# cli-process-runtime

状态：Proposed；SHALL/MUST 为拟议行为，并非实现完成声明。

## ADDED Requirements

### Requirement: RUN-001 真正实时事件输出

The SDK SHALL 在子进程仍运行时投递完整接收的事件；MUST 独立处理 stdout/stderr，正确处理 UTF-8 与 NDJSON 边界。

#### Scenario: 事件先于退出

- **GIVEN** 假进程输出第一帧后等待宿主信号
- **WHEN** 宿主订阅事件
- **THEN** 退出前收到首帧，不依赖结束后的批解析。

#### Scenario: 跨字节边界

- **GIVEN** 中文 UTF-8 和换行拆成多段
- **WHEN** 持续解码
- **THEN** 字符与事件正确，stderr 不混入 JSON 通道。

### Requirement: RUN-002 一次性与持续输入分离

The SDK SHALL 为一次性 stdin 定义写入/EOF；MUST NOT 将预装字符串加 EOF 宣称为持续双向会话；持续模式仅在已实现验证后开放。

#### Scenario: 一次性输入

- **GIVEN** 输入含多字节文本
- **WHEN** 假进程读到 EOF
- **THEN** 字节保真且按约定关闭输入，不等待继承终端。

#### Scenario: 不支持持续输入

- **GIVEN** 当前适配器只支持一次性
- **WHEN** 创建持续会话
- **THEN** 明确报告未实现，不返回伪造可写句柄。

### Requirement: RUN-003 调用隔离与 cwd

The SDK SHALL 按调用固定 cwd、环境覆盖和不可变选项快照；MUST 防止并发修改默认配置影响已运行请求，额外访问目录不是主 cwd。

#### Scenario: 并发工作区

- **GIVEN** 两请求 cwd/env 不同
- **WHEN** 假进程回报 cwd/测试变量
- **THEN** 彼此不串扰，不导出无关敏感变量。

#### Scenario: 修改默认配置

- **GIVEN** 运行开始后修改客户端默认值
- **WHEN** 观察旧运行和新运行
- **THEN** 旧运行保留快照，新运行按明确规则读取新配置。

### Requirement: RUN-004 独立探测与执行期限

The SDK SHALL 区分 probe、start、run、drain 期限；MUST 在到期时返回明确终止原因，不用长任务期限替代专属 probe 期限。

#### Scenario: 探测超时

- **GIVEN** probe 期限短于 run 且假进程挂起
- **WHEN** 调用 probe
- **THEN** 在 probe 期限及清理容差内返回，不等待 run 期限。

#### Scenario: 排空卡住

- **GIVEN** 子进程结束但读取链未在 drain 期限完成
- **WHEN** 收尾
- **THEN** 返回不完整诊断并结束自有资源，不无限等待。

### Requirement: RUN-005 有界内存与可见背压

The SDK SHALL 为事件队列、单帧、raw 和诊断设置上限；MUST 明确慢消费者/超大帧/截断策略，不静默丢关键结果或无限扩容。

#### Scenario: 慢消费者

- **GIVEN** 输出速度超过消费速度且队列满
- **WHEN** 达到容量
- **THEN** 依有期限等待、受控暂存或取消策略处理并诊断。

#### Scenario: raw 超限

- **GIVEN** 累计输出超出保留容量
- **WHEN** 继续执行或限制终止
- **THEN** 保留范围与截断可见，不宣称 raw 完整。

### Requirement: RUN-006 取消关闭与资源所有权

The SDK SHALL 幂等取消和关闭；MUST 限时清理自身进程、流、任务及临时文件，不关闭不属于句柄的远程会话或宿主进程；平台不支持须说明。

#### Scenario: 取消竞争

- **GIVEN** 自有任务运行中
- **WHEN** 并发 cancel、close 与自然完成
- **THEN** 只产生一个终态，可重复收尾且不误杀其他任务。

#### Scenario: 拒绝正常结束

- **GIVEN** 自有进程树拒绝退出
- **WHEN** 清理期限到达
- **THEN** 按平台策略升级终止并记录，未验证不能报完全清理成功。

### Requirement: RUN-007 进程与业务终态分离

The SDK SHALL 分别返回 OS 退出、终止原因、协议完整性和业务结果；MUST 保留失败输出，不能仅凭 exit=0 认定 Agent 结果或依赖加载完整。

#### Scenario: 非零退出

- **GIVEN** 假进程写双路输出并以测试码结束
- **WHEN** 收尾
- **THEN** 实际码和有界 stdout/stderr 保留，不压为无信息 -1。

#### Scenario: 管理与 Agent 区分

- **GIVEN** 版本查询无 result，Agent 流缺必要 result
- **WHEN** 分别判断状态
- **THEN** 管理查询不因缺 Agent envelope 失败；Agent 请求报告协议不完整。
