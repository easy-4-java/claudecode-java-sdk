package io.github.hiwepy.claudecode;

import lombok.Data;

/**
 * Claude Code CLI 客户端配置（纯 POJO，可与 Spring {@code @ConfigurationProperties} 映射）。
 */
@Data
public class ClaudeCodeClientConfig {

    /** 本地 CLI 可执行文件名或绝对路径 */
    private String localExecutable = "claude";

    /** 命令执行超时（秒） */
    private int localTimeoutSeconds = 600;

    /** 探测 CLI 是否可用的超时（秒） */
    private int localProbeTimeoutSeconds = 5;

    /** 默认模型（如 sonnet, opus, claude-sonnet-4-6） */
    private String defaultModel;

    /** 默认 effort level（low, medium, high, xhigh, max） */
    private String defaultEffort;

    /** 默认权限模式（default, acceptEdits, bypassPermissions, plan, auto） */
    private String defaultPermissionMode;

    /** 默认输出格式（text, json, stream-json） */
    private String defaultOutputFormat = "stream-json";

    /** 是否包含部分消息块 */
    private boolean includePartialMessages = true;

    /** 是否跳过 session 持久化 */
    private boolean noSessionPersistence;

    /** Session 名称前缀 */
    private String sessionName;

    /** 额外允许的目录 */
    private String addDir;

    /** 最大预算（美元） */
    private Double maxBudgetUsd;

    /** JSON Schema 文件路径（用于结构化输出） */
    private String jsonSchema;

    /** 是否启用 worktree 隔离 */
    private boolean worktree;

    /** 是否启用 Bare 模式 */
    private boolean bare;

    /** 自定义 system prompt */
    private String systemPrompt;

    /** system prompt 文件路径 */
    private String systemPromptFile;

    /** 追加的 system prompt */
    private String appendSystemPrompt;

    /** 追加的 system prompt 文件路径 */
    private String appendSystemPromptFile;

    /** 单 agent 覆盖 */
    private String agent;

    /** 自定义 agents JSON */
    private String agents;

    /** 限制允许的工具（逗号分隔） */
    private String allowedTools;

    /** 禁止的工具（逗号分隔） */
    private String disallowedTools;

    /** 内置工具集（default / 空字符串 / 具体工具列表） */
    private String tools;

    /** MCP 配置文件路径 */
    private String mcpConfig;

    /** 仅使用 --mcp-config 指定的 MCP server */
    private boolean strictMcpConfig;

    /** 默认模型过载时的 fallback 模型 */
    private String fallbackModel;

    /** 是否启用 debug */
    private boolean debug;

    /** debug 分类过滤器（如 "api,hooks"） */
    private String debugFilter;

    /** debug 日志输出文件路径 */
    private String debugFile;

    /** 是否启用 verbose */
    private boolean verbose;

    /** Beta 头 */
    private String betas;

    /** 是否连接 IDE */
    private boolean ide;

    /** 是否在 Chrome 中集成 */
    private boolean chrome;

    /** 禁用 Chrome 集成 */
    private boolean noChrome;

    /** 启动时下载的文件资源（格式: file_id:relative_path） */
    private String fileResources;

    /** 插件目录或 .zip 路径（可多个） */
    private String[] pluginDir;

    /** 插件 URL（可多个） */
    private String[] pluginUrl;

    /** 额外 settings 文件或 JSON */
    private String settings;

    /** settings 来源（user, project, local） */
    private String settingSources;

    /** tmux 模式（true 或 classic） */
    private String tmux;

    /** Remote Control 名称 */
    private String remoteControl;

    /** Remote Control session 名称前缀 */
    private String remoteControlSessionNamePrefix;

    /** 允许跳过权限作为选项（不默认启用） */
    private boolean allowDangerouslySkipPermissions;

    /** 禁用 slash 命令（skills） */
    private boolean disableSlashCommands;

    /** 将动态 system prompt 部分移到首条 user message */
    private boolean excludeDynamicSystemPromptSections;

    /** 在 stream-json 中包含 hook 事件 */
    private boolean includeHookEvents;

    /** [DEPRECATED] 启用 MCP debug 模式 */
    private boolean mcpDebug;
}
