package io.github.hiwepy.claudecode;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hiwepy.claudecode.cli.ClaudeCodeCli;
import io.github.hiwepy.claudecode.cli.ClaudeCodeCliExecutor;
import io.github.hiwepy.claudecode.cli.ClaudeCodeCliResult;
import io.github.hiwepy.claudecode.model.ClaudeAgent;
import io.github.hiwepy.claudecode.model.ClaudeMessage;
import io.github.hiwepy.claudecode.model.ClaudeResult;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Claude Code 客户端门面，封装本地 CLI 子进程调用。
 *
 * <h3>Session 管理</h3>
 * Claude Code 将 session 持久化到本地文件系统，通过以下机制管理：
 * <ul>
 *   <li>{@code -c / --continue} — 继续最近会话</li>
 *   <li>{@code -r / --resume [ID]} — 恢复指定会话或打开 picker</li>
 *   <li>{@code --fork-session} — 恢复时创建新 session ID</li>
 *   <li>{@code --session-id <uuid>} — 显式指定 session ID</li>
 *   <li>{@code --from-pr [number]} — 恢复关联 PR 的会话</li>
 *   <li>{@code -n <name>} — 命名 session 便于后期 resume</li>
 *   <li>{@code --no-session-persistence} — 不持久化（一次性）</li>
 * </ul>
 */
public class ClaudeCodeClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCodeClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ClaudeCodeClientConfig config;
    private final ClaudeCodeCli cli;

    public ClaudeCodeClient(ClaudeCodeClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.cli = new ClaudeCodeCli(new ClaudeCodeCliExecutor(config));
    }

    public ClaudeCodeClient(ClaudeCodeClientConfig config, ClaudeCodeCli cli) {
        this.config = Objects.requireNonNull(config, "config");
        this.cli = Objects.requireNonNull(cli, "cli");
    }

    // ============================================================
    // 基本信息
    // ============================================================

    public ClaudeCodeCliResult version() { return cli.version(); }
    public ClaudeCodeCliResult help() { return cli.help(); }

    // ============================================================
    // print — 非交互执行（核心）
    // ============================================================

    /** 发送 prompt 并阻塞等待文本输出 */
    public ClaudeCodeCliResult print(String prompt) {
        return cli.print(defaultPrintOptions(prompt));
    }

    /** 发送 prompt（指定模型） */
    public ClaudeCodeCliResult print(String prompt, String model) {
        return cli.print(new ClaudeCodeCli.PrintOptions(prompt).model(model));
    }

    /** 完整参数 print */
    public ClaudeCodeCliResult print(ClaudeCodeCli.PrintOptions opts) {
        return cli.print(opts);
    }

    /** stream-json 格式输出并解析为消息列表 */
    public List<ClaudeMessage> printStreamJson(String prompt) {
        ClaudeCodeCliResult result = cli.printStreamJson(prompt);
        return parseStreamJsonOutput(result.getStdout());
    }

    /** stream-json + 解析，返回消息列表和最后一个 result */
    public StreamResult printStreamJsonAndParse(String prompt) {
        ClaudeCodeCliResult result = cli.printStreamJson(prompt);
        List<ClaudeMessage> messages = parseStreamJsonOutput(result.getStdout());
        ClaudeResult finalResult = findResult(messages);
        return new StreamResult(messages, finalResult, result);
    }

    /** stream-json 双向模式（适合管道集成） */
    public ClaudeCodeCliResult printBidirectional(String prompt) {
        return cli.print(new ClaudeCodeCli.PrintOptions(prompt)
                .outputFormat("stream-json")
                .inputFormat("stream-json")
                .includePartialMessages(true)
                .replayUserMessages(true));
    }

    /** JSON 模式输出（单次完整 result） */
    public ClaudeCodeCliResult printJson(String prompt) {
        return cli.printJson(prompt);
    }

    /** 带 JSON Schema 的结构化输出 */
    public ClaudeCodeCliResult printWithSchema(String prompt, String jsonSchema) {
        return cli.printWithSchema(prompt, jsonSchema);
    }

    // ============================================================
    // Session 生命周期
    // ============================================================

    /** 继续最近的对话 */
    public ClaudeCodeCliResult continueSession() {
        return cli.continue_();
    }

    /** 继续最近的对话并发送 prompt */
    public ClaudeCodeCliResult continueSession(String prompt) {
        return cli.continue_(prompt);
    }

    /** 继续最近的对话（指定模型） */
    public ClaudeCodeCliResult continueSession(String prompt, String model) {
        return cli.continue_(prompt, model);
    }

    /** 打开交互式 resume picker */
    public ClaudeCodeCliResult resumeSession() {
        return cli.resume();
    }

    /** 通过 ID 恢复会话 */
    public ClaudeCodeCliResult resumeSession(String sessionId) {
        return cli.resume(sessionId);
    }

    /** 通过 ID 恢复会话并发送 prompt */
    public ClaudeCodeCliResult resumeSession(String sessionId, String prompt) {
        return cli.resume(sessionId, prompt);
    }

    /** 恢复并指定模型 */
    public ClaudeCodeCliResult resumeSession(String sessionId, String prompt, String model) {
        return cli.resume(sessionId, prompt, model);
    }

    /** 继续最近会话但创建新 session ID（fork） */
    public ClaudeCodeCliResult continueForkSession() {
        return cli.continueForkSession();
    }

    /** 恢复会话但创建新 session ID（fork） */
    public ClaudeCodeCliResult resumeForkSession(String sessionId) {
        return cli.resumeForkSession(sessionId);
    }

    /** 恢复并 fork（保留历史，新分支） */
    public ClaudeCodeCliResult resumeForkSession(String sessionId, String prompt) {
        return cli.resumeForkSession(sessionId, prompt);
    }

    /** 使用指定的 session ID 开始新会话 */
    public ClaudeCodeCliResult withSessionId(String uuid, String prompt) {
        return cli.withSessionId(uuid, prompt);
    }

    /** 恢复与 PR 关联的会话 */
    public ClaudeCodeCliResult fromPr(String prNumber) {
        return cli.fromPr(prNumber);
    }

    /** 交互式选择 PR 会话 */
    public ClaudeCodeCliResult fromPr() {
        return cli.fromPr();
    }

    /** 命名会话（便于后期 resume 时查找） */
    public ClaudeCodeCliResult namedSession(String name, String prompt) {
        return cli.namedSession(name, prompt);
    }

    /** 一次性会话（不持久化） */
    public ClaudeCodeCliResult printNoPersistence(String prompt) {
        return cli.printNoPersistence(prompt);
    }

    // ============================================================
    // 权限控制
    // ============================================================

    /** 指定权限模式 */
    public ClaudeCodeCliResult printWithPermission(String prompt, String permissionMode) {
        return cli.printWithPermission(prompt, permissionMode);
    }

    /** 跳过所有权限检查 */
    public ClaudeCodeCliResult printBypassPermissions(String prompt) {
        return cli.printBypassPermissions(prompt);
    }

    // ============================================================
    // worktree / directory
    // ============================================================

    public ClaudeCodeCliResult printInWorktree(String prompt) {
        return cli.printInWorktree(prompt);
    }

    public ClaudeCodeCliResult printInWorktree(String name, String prompt) {
        return cli.printInWorktree(name, prompt);
    }

    public ClaudeCodeCliResult printWithDir(String dir, String prompt) {
        return cli.printWithDir(dir, prompt);
    }

    // ============================================================
    // effort / budget
    // ============================================================

    public ClaudeCodeCliResult printWithEffort(String prompt, String effort) {
        return cli.printWithEffort(prompt, effort);
    }

    public ClaudeCodeCliResult printWithBudget(String prompt, double maxBudgetUsd) {
        return cli.printWithBudget(prompt, maxBudgetUsd);
    }

    // ============================================================
    // system prompt
    // ============================================================

    public ClaudeCodeCliResult printWithSystemPrompt(String userPrompt, String systemPrompt) {
        return cli.printWithSystemPrompt(userPrompt, systemPrompt);
    }

    public ClaudeCodeCliResult printWithAppendSystemPrompt(String userPrompt, String append) {
        return cli.printWithAppendSystemPrompt(userPrompt, append);
    }

    // ============================================================
    // agents / tools / mcp / plugins
    // ============================================================

    public ClaudeCodeCliResult printWithAgent(String prompt, String agent) {
        return cli.printWithAgent(prompt, agent);
    }

    public ClaudeCodeCliResult printWithAgents(String prompt, String agentsJson) {
        return cli.printWithAgents(prompt, agentsJson);
    }

    public ClaudeCodeCliResult printWithTools(String prompt, String tools) {
        return cli.printWithTools(prompt, tools);
    }

    public ClaudeCodeCliResult printWithAllowedTools(String prompt, String tools) {
        return cli.printWithAllowedTools(prompt, tools);
    }

    public ClaudeCodeCliResult printWithDisallowedTools(String prompt, String tools) {
        return cli.printWithDisallowedTools(prompt, tools);
    }

    public ClaudeCodeCliResult printWithFallbackModel(String prompt, String model, String fallbackModel) {
        return cli.printWithFallbackModel(prompt, model, fallbackModel);
    }

    public ClaudeCodeCliResult printWithMcpConfig(String prompt, String mcpConfig) {
        return cli.printWithMcpConfig(prompt, mcpConfig);
    }

    public ClaudeCodeCliResult printWithStrictMcpConfig(String prompt, String mcpConfig) {
        return cli.printWithStrictMcpConfig(prompt, mcpConfig);
    }

    // ============================================================
    // system prompt — file variants
    // ============================================================

    public ClaudeCodeCliResult printWithSystemPromptFile(String userPrompt, String systemPromptFile) {
        return cli.printWithSystemPromptFile(userPrompt, systemPromptFile);
    }

    public ClaudeCodeCliResult printWithAppendSystemPromptFile(String userPrompt, String appendFile) {
        return cli.printWithAppendSystemPromptFile(userPrompt, appendFile);
    }

    // ============================================================
    // settings / plugin / files
    // ============================================================

    public ClaudeCodeCliResult printWithSettings(String prompt, String settings) {
        return cli.printWithSettings(prompt, settings);
    }

    public ClaudeCodeCliResult printWithSettingSources(String prompt, String sources) {
        return cli.printWithSettingSources(prompt, sources);
    }

    public ClaudeCodeCliResult printWithPluginDir(String prompt, String pluginDir) {
        return cli.printWithPluginDir(prompt, pluginDir);
    }

    public ClaudeCodeCliResult printWithPluginUrl(String prompt, String pluginUrl) {
        return cli.printWithPluginUrl(prompt, pluginUrl);
    }

    public ClaudeCodeCliResult printWithFiles(String prompt, String fileSpecs) {
        return cli.printWithFiles(prompt, fileSpecs);
    }

    // ============================================================
    // tmux / remote-control
    // ============================================================

    public ClaudeCodeCliResult printWithTmux(String prompt) {
        return cli.printWithTmux(prompt);
    }

    public ClaudeCodeCliResult printWithClassicTmux(String prompt) {
        return cli.printWithClassicTmux(prompt);
    }

    public ClaudeCodeCliResult remoteControl(String name) {
        return cli.remoteControl(name);
    }

    public ClaudeCodeCliResult remoteControl() {
        return cli.remoteControl();
    }

    // ============================================================
    // 权限 / 调试 杂项
    // ============================================================

    public ClaudeCodeCliResult printNoChrome(String prompt) {
        return cli.printNoChrome(prompt);
    }

    public ClaudeCodeCliResult printAllowBypassPermissions(String prompt) {
        return cli.printAllowBypassPermissions(prompt);
    }

    public ClaudeCodeCliResult printDisableSlashCommands(String prompt) {
        return cli.printDisableSlashCommands(prompt);
    }

    public ClaudeCodeCliResult printExcludeDynamicSections(String prompt) {
        return cli.printExcludeDynamicSections(prompt);
    }

    public ClaudeCodeCliResult printWithHookEvents(String prompt) {
        return cli.printWithHookEvents(prompt);
    }

    public ClaudeCodeCliResult printWithDebugFilter(String prompt, String filter) {
        return cli.printWithDebugFilter(prompt, filter);
    }

    public ClaudeCodeCliResult printWithDebugFile(String prompt, String debugFile) {
        return cli.printWithDebugFile(prompt, debugFile);
    }

    public ClaudeCodeCliResult printWithMcpDebug(String prompt) {
        return cli.printWithMcpDebug(prompt);
    }

    // ============================================================
    // 子命令
    // ============================================================

    /** 列出后台 agents（JSON 格式） */
    public ClaudeCodeCliResult agentsList() {
        return cli.agentsList();
    }

    /** 列出后台 agents 并解析为对象列表 */
    public List<ClaudeAgent> agentsListAsObjects() {
        ClaudeCodeCliResult result = cli.agentsList();
        if (!result.isSuccess() || result.getStdout().isEmpty()) return Collections.emptyList();
        try {
            return MAPPER.readValue(result.getStdout(), new TypeReference<List<ClaudeAgent>>() {});
        } catch (Exception e) {
            log.debug("Failed to parse agents list JSON", e);
            return Collections.emptyList();
        }
    }

    public ClaudeCodeCliResult agents(String... args) { return cli.agents(args); }

    public ClaudeCodeCliResult authLogin() { return cli.authLogin(); }
    public ClaudeCodeCliResult authLogout() { return cli.authLogout(); }
    public ClaudeCodeCliResult authStatus() { return cli.authStatus(); }

    public ClaudeCodeCliResult doctor() { return cli.doctor(); }

    public ClaudeCodeCliResult install() { return cli.install(); }
    public ClaudeCodeCliResult install(String target) { return cli.install(target); }

    public ClaudeCodeCliResult mcpList() { return cli.mcpList(); }
    public ClaudeCodeCliResult mcpAdd(String name, String command, String... args) { return cli.mcpAdd(name, command, args); }
    public ClaudeCodeCliResult mcpGet(String name) { return cli.mcpGet(name); }
    public ClaudeCodeCliResult mcpRemove(String name) { return cli.mcpRemove(name); }
    public ClaudeCodeCliResult mcpServe() { return cli.mcpServe(); }
    public ClaudeCodeCliResult mcp(String... args) { return cli.mcp(args); }

    public ClaudeCodeCliResult pluginList() { return cli.pluginList(); }
    public ClaudeCodeCliResult pluginInstall(String plugin) { return cli.pluginInstall(plugin); }
    public ClaudeCodeCliResult plugin(String... args) { return cli.plugin(args); }

    public ClaudeCodeCliResult projectPurge() { return cli.projectPurge(); }
    public ClaudeCodeCliResult setupToken() { return cli.setupToken(); }
    public ClaudeCodeCliResult update() { return cli.update(); }

    public ClaudeCodeCliResult ultrareview() { return cli.ultrareview(); }
    public ClaudeCodeCliResult ultrareview(String target, int timeoutMinutes) { return cli.ultrareview(target, timeoutMinutes); }

    public ClaudeCodeCliResult autoMode() { return cli.autoMode(); }

    // ============================================================
    // 特殊模式
    // ============================================================

    public ClaudeCodeCliResult barePrint(String prompt) { return cli.barePrint(prompt); }
    public ClaudeCodeCliResult briefPrint(String prompt) { return cli.briefPrint(prompt); }
    public ClaudeCodeCliResult debugPrint(String prompt) { return cli.debugPrint(prompt); }
    public ClaudeCodeCliResult verbosePrint(String prompt) { return cli.verbosePrint(prompt); }
    public ClaudeCodeCliResult idePrint(String prompt) { return cli.idePrint(prompt); }

    /** 执行自定义 CLI 参数 */
    public ClaudeCodeCliResult execute(String... args) {
        return cli.executor().execute(args);
    }

    // ============================================================
    // CLI / Config
    // ============================================================

    public ClaudeCodeCli cli() { return cli; }
    public ClaudeCodeClientConfig getConfig() { return config; }

    // ============================================================
    // 内部方法
    // ============================================================

    private ClaudeCodeCli.PrintOptions defaultPrintOptions(String prompt) {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions(prompt);
        if (config.getDefaultModel() != null) opts.model(config.getDefaultModel());
        if (config.getFallbackModel() != null) opts.fallbackModel(config.getFallbackModel());
        if (config.getDefaultOutputFormat() != null) opts.outputFormat(config.getDefaultOutputFormat());
        if (config.isIncludePartialMessages()) opts.includePartialMessages(true);
        if (config.getDefaultPermissionMode() != null) opts.permissionMode(config.getDefaultPermissionMode());
        if (config.getDefaultEffort() != null) opts.effort(config.getDefaultEffort());
        if (config.getMaxBudgetUsd() != null) opts.maxBudgetUsd(config.getMaxBudgetUsd());
        if (config.getJsonSchema() != null) opts.jsonSchema(config.getJsonSchema());
        if (config.getSystemPrompt() != null) opts.systemPrompt(config.getSystemPrompt());
        if (config.getSystemPromptFile() != null) opts.systemPromptFile(config.getSystemPromptFile());
        if (config.getAppendSystemPrompt() != null) opts.appendSystemPrompt(config.getAppendSystemPrompt());
        if (config.getAppendSystemPromptFile() != null) opts.appendSystemPromptFile(config.getAppendSystemPromptFile());
        if (config.getAgent() != null) opts.agent(config.getAgent());
        if (config.getAgents() != null) opts.agents(config.getAgents());
        if (config.getAllowedTools() != null) opts.allowedTools(config.getAllowedTools());
        if (config.getDisallowedTools() != null) opts.disallowedTools(config.getDisallowedTools());
        if (config.getTools() != null) opts.tools(config.getTools());
        if (config.getMcpConfig() != null) opts.mcpConfig(config.getMcpConfig());
        if (config.isStrictMcpConfig()) opts.strictMcpConfig(true);
        if (config.getAddDir() != null) opts.addDir(config.getAddDir());
        if (config.getFileResources() != null) opts.fileResources(config.getFileResources());
        if (config.isNoSessionPersistence()) opts.noSessionPersistence(true);
        if (config.isWorktree()) opts.worktree(true);
        if (config.getTmux() != null) opts.tmux(config.getTmux());
        if (config.isBare()) opts.bare(true);
        if (config.isDebug()) opts.debug(true);
        if (config.getDebugFilter() != null) opts.debugFilter(config.getDebugFilter());
        if (config.getDebugFile() != null) opts.debugFile(config.getDebugFile());
        if (config.isVerbose()) opts.verbose(true);
        if (config.isIde()) opts.ide(true);
        if (config.isChrome()) opts.chrome(true);
        if (config.isNoChrome()) opts.noChrome(true);
        if (config.isAllowDangerouslySkipPermissions()) opts.allowDangerouslySkipPermissions(true);
        if (config.isDisableSlashCommands()) opts.disableSlashCommands(true);
        if (config.isExcludeDynamicSystemPromptSections()) opts.excludeDynamicSystemPromptSections(true);
        if (config.isIncludeHookEvents()) opts.includeHookEvents(true);
        if (config.isMcpDebug()) opts.mcpDebug(true);
        if (config.getBetas() != null) opts.betas(config.getBetas());
        if (config.getSettings() != null) opts.settings(config.getSettings());
        if (config.getSettingSources() != null) opts.settingSources(config.getSettingSources());
        if (config.getPluginDir() != null) opts.pluginDir(config.getPluginDir());
        if (config.getPluginUrl() != null) opts.pluginUrl(config.getPluginUrl());
        if (config.getRemoteControl() != null) opts.remoteControl(config.getRemoteControl());
        if (config.getRemoteControlSessionNamePrefix() != null) opts.remoteControlSessionNamePrefix(config.getRemoteControlSessionNamePrefix());
        if (config.getSessionName() != null) opts.sessionName(config.getSessionName());
        return opts;
    }

    private List<ClaudeMessage> parseStreamJsonOutput(String stdout) {
        List<ClaudeMessage> messages = new ArrayList<>();
        if (stdout == null || stdout.isEmpty()) return messages;
        for (String line : stdout.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            try {
                ClaudeMessage msg = MAPPER.readValue(line, ClaudeMessage.class);
                messages.add(msg);
            } catch (Exception e) {
                log.debug("Failed to parse stream-json line: {}", line, e);
            }
        }
        return messages;
    }

    private ClaudeResult findResult(List<ClaudeMessage> messages) {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ClaudeMessage msg = messages.get(i);
            if ("result".equals(msg.getType())) {
                try {
                    return MAPPER.convertValue(msg, ClaudeResult.class);
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /**
     * stream-json 解析结果包装。
     */
    public static class StreamResult {
        private final List<ClaudeMessage> messages;
        private final ClaudeResult result;
        private final ClaudeCodeCliResult rawResult;

        public StreamResult(List<ClaudeMessage> messages, ClaudeResult result, ClaudeCodeCliResult rawResult) {
            this.messages = messages;
            this.result = result;
            this.rawResult = rawResult;
        }

        public List<ClaudeMessage> getMessages() { return messages; }
        public ClaudeResult getResult() { return result; }
        public ClaudeCodeCliResult getRawResult() { return rawResult; }

        /** 提取所有文本内容 */
        public String getTextContent() {
            return result != null ? result.getResult() : "";
        }

        /** 获取总费用 */
        public Double getTotalCostUsd() {
            return result != null ? result.getTotalCostUsd() : null;
        }
    }

    @Override
    public void close() {
    }
}
