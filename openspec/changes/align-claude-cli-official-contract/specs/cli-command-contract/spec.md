# cli-command-contract

状态：Proposed；SHALL/MUST 为拟议行为，并非实现完成声明。

## ADDED Requirements

### Requirement: CMD-001 命令意图与合法模式

The SDK SHALL 根据明确的命令意图区分本地非交互运行、后台派发、云任务创建、既有云会话追加、自托管派发、管理、交互终端和常驻服务；MUST 在启动前拒绝已知冲突组合，不得把所有模式统一强制为 print。

#### Scenario: 禁止后台与 print 混用

- **GIVEN** 请求同时选择本地 print 和后台启动
- **WHEN** 请求被验证
- **THEN** 返回列明冲突的输入错误，子进程启动次数为 0。

#### Scenario: 云创建和云追加不同

- **GIVEN** 目标 CLI 能力证据覆盖云创建、既有会话追加和自托管派发
- **WHEN** 分别提交三种请求
- **THEN** 按各自契约编码；合法追加和自托管 print 不得被笼统拒绝。

### Requirement: CMD-002 类型化选项与组合校验

The SDK SHALL 校验类型化选项的格式、取值、依赖与互斥；MUST 返回字段级诊断，不得静默删除调用方权限或运行约束。

#### Scenario: 依赖和编码

- **GIVEN** 选择 classic tmux 并提供 worktree
- **WHEN** 构建命令
- **THEN** 采用目标 CLI 支持的绑定式参数并保留 classic；缺少依赖时拒绝。

#### Scenario: 无效数值与 schema

- **GIVEN** 提供非有限预算、无效轮次或损坏 schema JSON
- **WHEN** 请求被验证
- **THEN** 返回具体字段错误，不启动收费或有副作用的执行。

### Requirement: CMD-003 一致的高层默认配置

The SDK SHALL 对高层入口逐字段合并默认配置；MUST 区分未设置、显式 false、显式空值和显式值。仅覆盖 model/输出格式不得丢失客户端权限、工具、预算等默认值；完整低层参数入口的不同语义必须记录。

#### Scenario: 仅覆盖模型

- **GIVEN** 客户端设置工具限制、权限和预算
- **WHEN** 高层重载仅覆盖 model
- **THEN** 只有 model 改变，其他默认值保留。

#### Scenario: 显式关闭或空值

- **GIVEN** 客户端设 partial=false 或请求设 tools 为空
- **WHEN** 构建高层调用
- **THEN** 显式值不被默认 true 或非空集合覆盖，空 tools 不被当成未设置。

### Requirement: CMD-004 参数与执行目标保真

The SDK SHALL 分离执行目标与参数向量；MUST 保留空字符串、Unicode、引号和参数边界，不把普通调用转换为 shell 插值。显式 shell 内容执行是独立风险类型。

#### Scenario: 特殊参数

- **GIVEN** 参数含空格、换行、引号、中文、空值或类似选项的 prompt
- **WHEN** 假进程接收调用
- **THEN** 接收向量符合入口约定，无意外命令展开。

#### Scenario: 执行路径含空格

- **GIVEN** 可执行路径包含空格
- **WHEN** 执行只读探测
- **THEN** 正确定位文件，启动参数不从文件路径中误拆。

### Requirement: CMD-005 能力未知的显式处理

The SDK SHALL 区分支持、不支持、未知、需交互/实验条件；MUST 对安全关键或控制能力在未知时明确不可用，不得通过删除参数或放宽权限降级。raw passthrough 不提供同等类型化保证。

#### Scenario: 帮助缺项

- **GIVEN** 选项未出现在 help 且无其他不支持证据
- **WHEN** 查询能力
- **THEN** 报告未知或依据其他可信证据判断，不仅凭 help 缺失断言不支持。

#### Scenario: 安全选项不可用

- **GIVEN** 目标 CLI 明确不支持必需安全选项
- **WHEN** 构建类型化请求
- **THEN** 拒绝并报告兼容问题，不去掉安全选项继续执行。
