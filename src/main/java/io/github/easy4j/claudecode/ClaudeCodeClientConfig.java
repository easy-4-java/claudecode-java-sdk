/*
 * Copyright (c) 2018-present, easy-4-java (https://github.com/easy-4-java).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.easy4j.claudecode;

import lombok.Data;

/**
 * Pure POJO holding every tunable of the {@link ClaudeCodeClient}.
 *
 * <p>Every field on this class maps to a single Claude Code CLI option.
 * The {@link ClaudeCodeClient} reads the non-null values and turns them
 * into a fully-populated {@code PrintOptions} on every invocation.</p>
 *
 * <p>The class is annotated with Lombok's {@code @Data} so getters,
 * setters, {@code toString}, {@code equals} and {@code hashCode} are
 * generated at compile time. It is intentionally framework-agnostic so it
 * can be populated from Spring's {@code @ConfigurationProperties}, plain
 * Java, JSON or a builder.</p>
 *
 * @author easy-4-java contributors
 * @since 3.0.0
 * @see ClaudeCodeClient
 */
@Data
public class ClaudeCodeClientConfig {

    /** Local CLI executable name or absolute path. */
    private String localExecutable = "claude";

    /** Command execution timeout in seconds. */
    private int localTimeoutSeconds = 600;

    /** Probe timeout in seconds used to verify that the CLI is available. */
    private int localProbeTimeoutSeconds = 5;

    /** Default model identifier (e.g. {@code sonnet}, {@code opus}, {@code claude-sonnet-4-6}). */
    private String defaultModel;

    /** Default reasoning effort level ({@code low, medium, high, xhigh, max}). */
    private String defaultEffort;

    /** Default permission mode
     *  ({@code default, acceptEdits, bypassPermissions, plan, auto}). */
    private String defaultPermissionMode;

    /** Default output format ({@code text}, {@code json} or {@code stream-json}). */
    private String defaultOutputFormat = "stream-json";

    /** Whether to include partial message chunks in the {@code stream-json} envelope. */
    private boolean includePartialMessages = true;

    /** Skip session persistence (one-shot invocation). */
    private boolean noSessionPersistence;

    /** Display name to give the session. */
    private String sessionName;

    /** Additional working directory allowed for tool execution. */
    private String addDir;

    /** Maximum spend in USD. */
    private Double maxBudgetUsd;

    /** Path to a JSON-Schema file used to constrain the response shape. */
    private String jsonSchema;

    /** Run inside an isolated git worktree. */
    private boolean worktree;

    /** Run in bare mode (minimal scaffolding). */
    private boolean bare;

    /** Custom system prompt. */
    private String systemPrompt;

    /** Path to a system prompt file. */
    private String systemPromptFile;

    /** Text appended to the default system prompt. */
    private String appendSystemPrompt;

    /** Path to a file appended to the default system prompt. */
    private String appendSystemPromptFile;

    /** Single-agent override. */
    private String agent;

    /** Custom agents JSON payload. */
    private String agents;

    /** Comma-separated list of tools explicitly allowed. */
    private String allowedTools;

    /** Comma-separated list of tools explicitly disallowed. */
    private String disallowedTools;

    /** Built-in tool set identifier ({@code default} / empty / explicit list). */
    private String tools;

    /** MCP configuration file or JSON. */
    private String mcpConfig;

    /** Whether the CLI should use only the MCP servers declared in {@link #mcpConfig}. */
    private boolean strictMcpConfig;

    /** Fallback model used when {@link #defaultModel} is overloaded. */
    private String fallbackModel;

    /** Enable debug logging. */
    private boolean debug;

    /** Comma-separated debug category filter (e.g. {@code api,hooks}). */
    private String debugFilter;

    /** Path to a debug log file. */
    private String debugFile;

    /** Enable verbose CLI output. */
    private boolean verbose;

    /** Custom {@code betas} header values. */
    private String betas;

    /** Connect the CLI to an IDE. */
    private boolean ide;

    /** Integrate the CLI with Chrome. */
    private boolean chrome;

    /** Disable Chrome integration. */
    private boolean noChrome;

    /** File resources to download on startup ({@code file_id:relative_path}). */
    private String fileResources;

    /** Plugin directory or {@code .zip} path (multiple). */
    private String[] pluginDir;

    /** Plugin URL (multiple). */
    private String[] pluginUrl;

    /** Additional settings file or JSON. */
    private String settings;

    /** Settings sources to load ({@code user, project, local}). */
    private String settingSources;

    /** tmux mode ({@code true} or {@code classic}). */
    private String tmux;

    /** Remote-control session name. */
    private String remoteControl;

    /** Remote-control session name prefix. */
    private String remoteControlSessionNamePrefix;

    /** Allow bypassing permissions as a selectable option (off by default). */
    private boolean allowDangerouslySkipPermissions;

    /** Disable slash commands (skills). */
    private boolean disableSlashCommands;

    /** Move dynamic system-prompt sections into the first user message. */
    private boolean excludeDynamicSystemPromptSections;

    /** Include hook events in the {@code stream-json} envelope. */
    private boolean includeHookEvents;

    /** [DEPRECATED] enable the MCP debug mode. */
    private boolean mcpDebug;
}
