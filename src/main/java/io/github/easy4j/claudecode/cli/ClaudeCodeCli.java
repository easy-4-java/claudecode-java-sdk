package io.github.easy4j.claudecode.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 本地 {@code claude} CLI 命令封装 — 覆盖所有官方 CLI 选项和命令。
 *
 * <h3>Session 管理（核心特性）</h3>
 * <ul>
 *   <li>{@code -c / --continue} — 继续最近的会话</li>
 *   <li>{@code -r / --resume [ID]} — 恢复指定会话</li>
 *   <li>{@code --fork-session} — 恢复时创建新 session ID</li>
 *   <li>{@code --session-id <uuid>} — 使用指定 session ID</li>
 *   <li>{@code --from-pr [value]} — 恢复与 PR 关联的会话</li>
 *   <li>{@code --no-session-persistence} — 不持久化 session</li>
 *   <li>{@code -n / --name <name>} — 设置 session 显示名称</li>
 * </ul>
 *
 * @see <a href="https://docs.anthropic.com/en/docs/claude-code">Claude Code CLI</a>
 */
public class ClaudeCodeCli {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCodeCli.class);

    private final ClaudeCodeCliExecutor executor;

    public ClaudeCodeCli(ClaudeCodeCliExecutor executor) {
        this.executor = executor;
    }

    public ClaudeCodeCliExecutor executor() {
        return executor;
    }

    // ============================================================
    // 全局
    // ============================================================

    /** {@code claude --version} */
    public ClaudeCodeCliResult version() {
        return executor.execute("--version");
    }

    /** {@code claude --help} */
    public ClaudeCodeCliResult help() {
        return executor.execute("--help");
    }

    // ============================================================
    // print (-p) — 非交互输出
    // ============================================================

    /** {@code claude -p <prompt>} — 基本 print 模式 */
    public ClaudeCodeCliResult print(String prompt) {
        return executor.execute("-p", prompt);
    }

    /** {@code claude -p --model <model> <prompt>} */
    public ClaudeCodeCliResult print(String prompt, String model) {
        return executor.execute("-p", "--model", model, prompt);
    }

    /** {@code claude -p --output-format stream-json --include-partial-messages <prompt>} */
    public ClaudeCodeCliResult printStreamJson(String prompt) {
        return executor.execute("-p", "--output-format", "stream-json", "--include-partial-messages", prompt);
    }

    /** {@code claude -p --output-format json <prompt>} */
    public ClaudeCodeCliResult printJson(String prompt) {
        return executor.execute("-p", "--output-format", "json", prompt);
    }

    /** {@code claude -p --output-format stream-json --input-format stream-json --replay-user-messages --include-partial-messages} */
    public ClaudeCodeCliResult printStreamJsonBidirectional() {
        return executor.execute("-p", "--output-format", "stream-json",
                "--input-format", "stream-json",
                "--replay-user-messages", "--include-partial-messages");
    }

    /** {@code claude -p --json-schema <schema> <prompt>} */
    public ClaudeCodeCliResult printWithSchema(String prompt, String jsonSchema) {
        return executor.execute("-p", "--json-schema", jsonSchema, prompt);
    }

    /** 完整 print options */
    public ClaudeCodeCliResult print(PrintOptions opts) {
        return executor.execute(opts.toArgs());
    }

    // ============================================================
    // Session 生命周期 — continue / resume / fork
    // ============================================================

    /** {@code claude -c} — 继续最近的对话 */
    public ClaudeCodeCliResult continue_() {
        return executor.execute("-c");
    }

    /** {@code claude -c -p <prompt>} — 继续并发送 prompt */
    public ClaudeCodeCliResult continue_(String prompt) {
        return executor.execute("-c", "-p", prompt);
    }

    /** {@code claude -c --model <model> -p <prompt>} */
    public ClaudeCodeCliResult continue_(String prompt, String model) {
        return executor.execute("-c", "--model", model, "-p", prompt);
    }

    /** {@code claude -r [sessionId]} — 恢复对话（交互式 picker） */
    public ClaudeCodeCliResult resume() {
        return executor.execute("-r");
    }

    /** {@code claude -r <sessionId>} — 通过 ID 恢复 */
    public ClaudeCodeCliResult resume(String sessionId) {
        return executor.execute("-r", sessionId);
    }

    /** {@code claude -r <sessionId> -p <prompt>} — 恢复并发送 prompt */
    public ClaudeCodeCliResult resume(String sessionId, String prompt) {
        return executor.execute("-r", sessionId, "-p", prompt);
    }

    /** {@code claude -r <sessionId> --model <model> -p <prompt>} */
    public ClaudeCodeCliResult resume(String sessionId, String prompt, String model) {
        return executor.execute("-r", sessionId, "--model", model, "-p", prompt);
    }

    /** {@code claude -c --fork-session} — 继续但创建新 session ID */
    public ClaudeCodeCliResult continueForkSession() {
        return executor.execute("-c", "--fork-session");
    }

    /** {@code claude -r <id> --fork-session} — 恢复并 fork */
    public ClaudeCodeCliResult resumeForkSession(String sessionId) {
        return executor.execute("-r", sessionId, "--fork-session");
    }

    /** {@code claude -r <id> --fork-session -p <prompt>} */
    public ClaudeCodeCliResult resumeForkSession(String sessionId, String prompt) {
        return executor.execute("-r", sessionId, "--fork-session", "-p", prompt);
    }

    // ============================================================
    // Session ID / PR / Name
    // ============================================================

    /** {@code claude --session-id <uuid> -p <prompt>} */
    public ClaudeCodeCliResult withSessionId(String uuid, String prompt) {
        return executor.execute("--session-id", uuid, "-p", prompt);
    }

    /** {@code claude --from-pr [prNumber]} */
    public ClaudeCodeCliResult fromPr(String prNumber) {
        return executor.execute("--from-pr", prNumber);
    }

    /** {@code claude --from-pr} (interactive picker) */
    public ClaudeCodeCliResult fromPr() {
        return executor.execute("--from-pr");
    }

    /** {@code claude -n <name> -p <prompt>} — 命名 session */
    public ClaudeCodeCliResult namedSession(String name, String prompt) {
        return executor.execute("-n", name, "-p", prompt);
    }

    /** {@code claude --no-session-persistence -p <prompt>} */
    public ClaudeCodeCliResult printNoPersistence(String prompt) {
        return executor.execute("--no-session-persistence", "-p", prompt);
    }

    // ============================================================
    // permission mode
    // ============================================================

    /** {@code claude --permission-mode <mode> -p <prompt>} */
    public ClaudeCodeCliResult printWithPermission(String prompt, String permissionMode) {
        return executor.execute("--permission-mode", permissionMode, "-p", prompt);
    }

    /** {@code claude --dangerously-skip-permissions -p <prompt>} */
    public ClaudeCodeCliResult printBypassPermissions(String prompt) {
        return executor.execute("--dangerously-skip-permissions", "-p", prompt);
    }

    // ============================================================
    // worktree / add-dir
    // ============================================================

    /** {@code claude -w -p <prompt>} */
    public ClaudeCodeCliResult printInWorktree(String prompt) {
        return executor.execute("-w", "-p", prompt);
    }

    /** {@code claude -w <name> -p <prompt>} — 命名 worktree */
    public ClaudeCodeCliResult printInWorktree(String name, String prompt) {
        return executor.execute("-w", name, "-p", prompt);
    }

    /** {@code claude --add-dir <dir> -p <prompt>} */
    public ClaudeCodeCliResult printWithDir(String dir, String prompt) {
        return executor.execute("--add-dir", dir, "-p", prompt);
    }

    // ============================================================
    // effort / budget
    // ============================================================

    /** {@code claude --effort <level> -p <prompt>} */
    public ClaudeCodeCliResult printWithEffort(String prompt, String effort) {
        return executor.execute("--effort", effort, "-p", prompt);
    }

    /** {@code claude --max-budget-usd <amount> -p <prompt>} */
    public ClaudeCodeCliResult printWithBudget(String prompt, double maxBudgetUsd) {
        return executor.execute("--max-budget-usd", String.valueOf(maxBudgetUsd), "-p", prompt);
    }

    // ============================================================
    // system prompt
    // ============================================================

    /** {@code claude --system-prompt <prompt> -p <prompt>} */
    public ClaudeCodeCliResult printWithSystemPrompt(String userPrompt, String systemPrompt) {
        return executor.execute("--system-prompt", systemPrompt, "-p", userPrompt);
    }

    /** {@code claude --append-system-prompt <prompt> -p <prompt>} */
    public ClaudeCodeCliResult printWithAppendSystemPrompt(String userPrompt, String appendPrompt) {
        return executor.execute("--append-system-prompt", appendPrompt, "-p", userPrompt);
    }

    // ============================================================
    // agents / tools / mcp
    // ============================================================

    /** {@code claude --agents <json> -p <prompt>} */
    public ClaudeCodeCliResult printWithAgents(String prompt, String agentsJson) {
        return executor.execute("--agents", agentsJson, "-p", prompt);
    }

    /** {@code claude --allowedTools <tools> -p <prompt>} */
    public ClaudeCodeCliResult printWithAllowedTools(String prompt, String tools) {
        return executor.execute("--allowedTools", tools, "-p", prompt);
    }

    /** {@code claude --mcp-config <config> -p <prompt>} */
    public ClaudeCodeCliResult printWithMcpConfig(String prompt, String mcpConfig) {
        return executor.execute("--mcp-config", mcpConfig, "-p", prompt);
    }

    // ============================================================
    // 子命令
    // ============================================================

    /** {@code claude agents --json} */
    public ClaudeCodeCliResult agentsList() {
        return executor.execute("agents", "--json");
    }

    /** {@code claude agents [options]} */
    public ClaudeCodeCliResult agents(String... args) {
        String[] all = new String[args.length + 1];
        all[0] = "agents";
        System.arraycopy(args, 0, all, 1, args.length);
        return executor.execute(all);
    }

    /** {@code claude auth login} */
    public ClaudeCodeCliResult authLogin() {
        return executor.execute("auth", "login");
    }

    /** {@code claude auth logout} */
    public ClaudeCodeCliResult authLogout() {
        return executor.execute("auth", "logout");
    }

    /** {@code claude auth status} */
    public ClaudeCodeCliResult authStatus() {
        return executor.execute("auth", "status");
    }

    /** {@code claude doctor} */
    public ClaudeCodeCliResult doctor() {
        return executor.execute("doctor");
    }

    /** {@code claude install [target]} */
    public ClaudeCodeCliResult install(String target) {
        return executor.execute("install", target);
    }

    /** {@code claude install} */
    public ClaudeCodeCliResult install() {
        return executor.execute("install");
    }

    /** {@code claude mcp [subcommand...]} */
    public ClaudeCodeCliResult mcp(String... args) {
        String[] all = new String[args.length + 1];
        all[0] = "mcp";
        System.arraycopy(args, 0, all, 1, args.length);
        return executor.execute(all);
    }

    /** {@code claude mcp list} */
    public ClaudeCodeCliResult mcpList() {
        return executor.execute("mcp", "list");
    }

    /** {@code claude mcp add <name> <commandOrUrl> [args...]} */
    public ClaudeCodeCliResult mcpAdd(String name, String commandOrUrl, String... args) {
        List<String> all = new ArrayList<>();
        all.add("mcp"); all.add("add"); all.add(name); all.add(commandOrUrl);
        for (String a : args) all.add(a);
        return executor.execute(all.toArray(new String[0]));
    }

    /** {@code claude mcp get <name>} */
    public ClaudeCodeCliResult mcpGet(String name) {
        return executor.execute("mcp", "get", name);
    }

    /** {@code claude mcp remove <name>} */
    public ClaudeCodeCliResult mcpRemove(String name) {
        return executor.execute("mcp", "remove", name);
    }

    /** {@code claude mcp serve} */
    public ClaudeCodeCliResult mcpServe() {
        return executor.execute("mcp", "serve");
    }

    /** {@code claude plugin list} */
    public ClaudeCodeCliResult pluginList() {
        return executor.execute("plugin", "list");
    }

    /** {@code claude plugin install <plugin>} */
    public ClaudeCodeCliResult pluginInstall(String plugin) {
        return executor.execute("plugin", "install", plugin);
    }

    /** {@code claude plugin [subcommand...]} */
    public ClaudeCodeCliResult plugin(String... args) {
        String[] all = new String[args.length + 1];
        all[0] = "plugin";
        System.arraycopy(args, 0, all, 1, args.length);
        return executor.execute(all);
    }

    /** {@code claude project purge} */
    public ClaudeCodeCliResult projectPurge() {
        return executor.execute("project", "purge");
    }

    /** {@code claude setup-token} */
    public ClaudeCodeCliResult setupToken() {
        return executor.execute("setup-token");
    }

    /** {@code claude update} */
    public ClaudeCodeCliResult update() {
        return executor.execute("update");
    }

    /** {@code claude ultrareview} */
    public ClaudeCodeCliResult ultrareview() {
        return executor.execute("ultrareview");
    }

    /** {@code claude ultrareview <target> --timeout <min>} */
    public ClaudeCodeCliResult ultrareview(String target, int timeoutMinutes) {
        return executor.execute("ultrareview", target, "--timeout", String.valueOf(timeoutMinutes));
    }

    /** {@code claude --bare -p <prompt>} — 极简模式 */
    public ClaudeCodeCliResult barePrint(String prompt) {
        return executor.execute("--bare", "-p", prompt);
    }

    /** {@code claude --brief -p <prompt>} — 启用 agent-to-user 通信 */
    public ClaudeCodeCliResult briefPrint(String prompt) {
        return executor.execute("--brief", "-p", prompt);
    }

    /** {@code claude --debug -p <prompt>} */
    public ClaudeCodeCliResult debugPrint(String prompt) {
        return executor.execute("--debug", "-p", prompt);
    }

    /** {@code claude --verbose -p <prompt>} */
    public ClaudeCodeCliResult verbosePrint(String prompt) {
        return executor.execute("--verbose", "-p", prompt);
    }

    /** {@code claude --ide -p <prompt>} */
    public ClaudeCodeCliResult idePrint(String prompt) {
        return executor.execute("--ide", "-p", prompt);
    }

    // ============================================================
    // agent / fallback-model / tools
    // ============================================================

    /** {@code claude --agent <agent> -p <prompt>} */
    public ClaudeCodeCliResult printWithAgent(String prompt, String agent) {
        return executor.execute("--agent", agent, "-p", prompt);
    }

    /** {@code claude --fallback-model <model> --model <model> -p <prompt>} */
    public ClaudeCodeCliResult printWithFallbackModel(String prompt, String model, String fallbackModel) {
        return executor.execute("--model", model, "--fallback-model", fallbackModel, "-p", prompt);
    }

    /** {@code claude --disallowedTools <tools> -p <prompt>} */
    public ClaudeCodeCliResult printWithDisallowedTools(String prompt, String tools) {
        return executor.execute("--disallowedTools", tools, "-p", prompt);
    }

    /** {@code claude --tools <tools> -p <prompt>} — 指定可用工具集 */
    public ClaudeCodeCliResult printWithTools(String prompt, String tools) {
        return executor.execute("--tools", tools, "-p", prompt);
    }

    // ============================================================
    // system-prompt-file / append-system-prompt-file
    // ============================================================

    /** {@code claude --system-prompt-file <path> -p <prompt>} */
    public ClaudeCodeCliResult printWithSystemPromptFile(String userPrompt, String systemPromptFile) {
        return executor.execute("--system-prompt-file", systemPromptFile, "-p", userPrompt);
    }

    /** {@code claude --append-system-prompt-file <path> -p <prompt>} */
    public ClaudeCodeCliResult printWithAppendSystemPromptFile(String userPrompt, String appendFile) {
        return executor.execute("--append-system-prompt-file", appendFile, "-p", userPrompt);
    }

    // ============================================================
    // strict-mcp-config / settings / setting-sources
    // ============================================================

    /** {@code claude --mcp-config <config> --strict-mcp-config -p <prompt>} */
    public ClaudeCodeCliResult printWithStrictMcpConfig(String prompt, String mcpConfig) {
        return executor.execute("--mcp-config", mcpConfig, "--strict-mcp-config", "-p", prompt);
    }

    /** {@code claude --settings <file-or-json> -p <prompt>} */
    public ClaudeCodeCliResult printWithSettings(String prompt, String settings) {
        return executor.execute("--settings", settings, "-p", prompt);
    }

    /** {@code claude --setting-sources <sources> -p <prompt>} */
    public ClaudeCodeCliResult printWithSettingSources(String prompt, String sources) {
        return executor.execute("--setting-sources", sources, "-p", prompt);
    }

    // ============================================================
    // plugin-dir / plugin-url / file
    // ============================================================

    /** {@code claude --plugin-dir <path> -p <prompt>}（可多次指定） */
    public ClaudeCodeCliResult printWithPluginDir(String prompt, String... pluginDir) {
        List<String> args = new ArrayList<>();
        for (String p : pluginDir) { args.add("--plugin-dir"); args.add(p); }
        args.add("-p"); args.add(prompt);
        return executor.execute(args.toArray(new String[0]));
    }

    /** {@code claude --plugin-url <url> -p <prompt>}（可多次指定） */
    public ClaudeCodeCliResult printWithPluginUrl(String prompt, String... pluginUrl) {
        List<String> args = new ArrayList<>();
        for (String u : pluginUrl) { args.add("--plugin-url"); args.add(u); }
        args.add("-p"); args.add(prompt);
        return executor.execute(args.toArray(new String[0]));
    }

    /** {@code claude --file <specs> -p <prompt>} — 启动时下载文件资源 */
    public ClaudeCodeCliResult printWithFiles(String prompt, String fileSpecs) {
        return executor.execute("--file", fileSpecs, "-p", prompt);
    }

    // ============================================================
    // tmux / remote-control
    // ============================================================

    /** {@code claude --tmux -w -p <prompt>} */
    public ClaudeCodeCliResult printWithTmux(String prompt) {
        return executor.execute("--tmux", "-w", "-p", prompt);
    }

    /** {@code claude --tmux=classic -w -p <prompt>} */
    public ClaudeCodeCliResult printWithClassicTmux(String prompt) {
        return executor.execute("--tmux=classic", "-w", "-p", prompt);
    }

    /** {@code claude --remote-control [name]} */
    public ClaudeCodeCliResult remoteControl(String name) {
        return executor.execute("--remote-control", name);
    }

    /** {@code claude --remote-control} */
    public ClaudeCodeCliResult remoteControl() {
        return executor.execute("--remote-control");
    }

    /** {@code claude --remote-control-session-name-prefix <prefix>} */
    public ClaudeCodeCliResult remoteControlWithPrefix(String prefix) {
        return executor.execute("--remote-control", "--remote-control-session-name-prefix", prefix);
    }

    // ============================================================
    // 权限 / 安全 / 调试 杂项
    // ============================================================

    /** {@code claude --allow-dangerously-skip-permissions -p <prompt>} */
    public ClaudeCodeCliResult printAllowBypassPermissions(String prompt) {
        return executor.execute("--allow-dangerously-skip-permissions", "-p", prompt);
    }

    /** {@code claude --disable-slash-commands -p <prompt>} */
    public ClaudeCodeCliResult printDisableSlashCommands(String prompt) {
        return executor.execute("--disable-slash-commands", "-p", prompt);
    }

    /** {@code claude --exclude-dynamic-system-prompt-sections -p <prompt>} */
    public ClaudeCodeCliResult printExcludeDynamicSections(String prompt) {
        return executor.execute("--exclude-dynamic-system-prompt-sections", "-p", prompt);
    }

    /** {@code claude --include-hook-events --output-format stream-json -p <prompt>} */
    public ClaudeCodeCliResult printWithHookEvents(String prompt) {
        return executor.execute("--include-hook-events", "--output-format", "stream-json", "-p", prompt);
    }

    /** {@code claude --no-chrome -p <prompt>} */
    public ClaudeCodeCliResult printNoChrome(String prompt) {
        return executor.execute("--no-chrome", "-p", prompt);
    }

    /** {@code claude --debug <filter> -p <prompt>} — 带分类过滤器的 debug */
    public ClaudeCodeCliResult printWithDebugFilter(String prompt, String filter) {
        return executor.execute("--debug", filter, "-p", prompt);
    }

    /** {@code claude --debug-file <path> -p <prompt>} — debug 日志写入文件 */
    public ClaudeCodeCliResult printWithDebugFile(String prompt, String debugFile) {
        return executor.execute("--debug-file", debugFile, "-p", prompt);
    }

    /** {@code claude --mcp-debug -p <prompt>} — [DEPRECATED] MCP debug */
    public ClaudeCodeCliResult printWithMcpDebug(String prompt) {
        return executor.execute("--mcp-debug", "-p", prompt);
    }

    // ============================================================
    // auto-mode 子命令
    // ============================================================

    /** {@code claude auto-mode} — 检查 auto mode 分类器配置 */
    public ClaudeCodeCliResult autoMode() {
        return executor.execute("auto-mode");
    }

    // ============================================================
    // PrintOptions builder — 组合所有 print 选项
    // ============================================================

    /**
     * {@code claude -p} 的完整选项组装。
     * 这是 SDK 的核心：将所有 claude 命令行选项映射为 Java builder 方法。
     */
    public static class PrintOptions {
        private String prompt;
        private String model;
        private String outputFormat = "stream-json";
        private boolean includePartialMessages = true;
        private boolean replayUserMessages;
        private String inputFormat;
        private String permissionMode;
        private String effort;
        private String maxBudgetUsd;
        private String jsonSchema;
        private String systemPrompt;
        private String systemPromptFile;
        private String appendSystemPrompt;
        private String appendSystemPromptFile;
        private String agent;
        private String agents;
        private String allowedTools;
        private String disallowedTools;
        private String tools;
        private String mcpConfig;
        private boolean strictMcpConfig;
        private String fallbackModel;
        private String addDir;
        private String sessionId;
        private String resumeSessionId;
        private boolean continueSession;
        private boolean forkSession;
        private String fromPr;
        private String sessionName;
        private boolean noSessionPersistence;
        private boolean worktree;
        private String worktreeName;
        private String tmux;
        private boolean bare;
        private boolean brief;
        private boolean debug;
        private String debugFilter;
        private String debugFile;
        private boolean verbose;
        private boolean ide;
        private boolean chrome;
        private boolean noChrome;
        private boolean dangerouslySkipPermissions;
        private boolean allowDangerouslySkipPermissions;
        private boolean disableSlashCommands;
        private boolean excludeDynamicSystemPromptSections;
        private boolean includeHookEvents;
        private boolean mcpDebug;
        private String betas;
        private String settings;
        private String settingSources;
        private String[] pluginDir;
        private String[] pluginUrl;
        private String fileResources;
        private String remoteControl;
        private String remoteControlSessionNamePrefix;

        public PrintOptions(String prompt) { this.prompt = prompt; }

        public PrintOptions model(String v) { this.model = v; return this; }
        public PrintOptions outputFormat(String v) { this.outputFormat = v; return this; }
        public PrintOptions includePartialMessages(boolean v) { this.includePartialMessages = v; return this; }
        public PrintOptions replayUserMessages(boolean v) { this.replayUserMessages = v; return this; }
        public PrintOptions inputFormat(String v) { this.inputFormat = v; return this; }
        public PrintOptions permissionMode(String v) { this.permissionMode = v; return this; }
        public PrintOptions effort(String v) { this.effort = v; return this; }
        public PrintOptions maxBudgetUsd(double v) { this.maxBudgetUsd = String.valueOf(v); return this; }
        public PrintOptions jsonSchema(String v) { this.jsonSchema = v; return this; }
        public PrintOptions systemPrompt(String v) { this.systemPrompt = v; return this; }
        public PrintOptions systemPromptFile(String v) { this.systemPromptFile = v; return this; }
        public PrintOptions appendSystemPrompt(String v) { this.appendSystemPrompt = v; return this; }
        public PrintOptions appendSystemPromptFile(String v) { this.appendSystemPromptFile = v; return this; }
        public PrintOptions agent(String v) { this.agent = v; return this; }
        public PrintOptions agents(String v) { this.agents = v; return this; }
        public PrintOptions allowedTools(String v) { this.allowedTools = v; return this; }
        public PrintOptions disallowedTools(String v) { this.disallowedTools = v; return this; }
        public PrintOptions tools(String v) { this.tools = v; return this; }
        public PrintOptions mcpConfig(String v) { this.mcpConfig = v; return this; }
        public PrintOptions strictMcpConfig(boolean v) { this.strictMcpConfig = v; return this; }
        public PrintOptions fallbackModel(String v) { this.fallbackModel = v; return this; }
        public PrintOptions addDir(String v) { this.addDir = v; return this; }
        public PrintOptions sessionId(String v) { this.sessionId = v; return this; }
        public PrintOptions resumeSessionId(String v) { this.resumeSessionId = v; return this; }
        public PrintOptions continueSession(boolean v) { this.continueSession = v; return this; }
        public PrintOptions forkSession(boolean v) { this.forkSession = v; return this; }
        public PrintOptions fromPr(String v) { this.fromPr = v; return this; }
        public PrintOptions sessionName(String v) { this.sessionName = v; return this; }
        public PrintOptions noSessionPersistence(boolean v) { this.noSessionPersistence = v; return this; }
        public PrintOptions worktree(boolean v) { this.worktree = v; return this; }
        public PrintOptions worktreeName(String v) { this.worktreeName = v; return this; }
        public PrintOptions bare(boolean v) { this.bare = v; return this; }
        public PrintOptions brief(boolean v) { this.brief = v; return this; }
        public PrintOptions debug(boolean v) { this.debug = v; return this; }
        public PrintOptions debugFilter(String v) { this.debugFilter = v; return this; }
        public PrintOptions debugFile(String v) { this.debugFile = v; return this; }
        public PrintOptions verbose(boolean v) { this.verbose = v; return this; }
        public PrintOptions ide(boolean v) { this.ide = v; return this; }
        public PrintOptions chrome(boolean v) { this.chrome = v; return this; }
        public PrintOptions noChrome(boolean v) { this.noChrome = v; return this; }
        public PrintOptions dangerouslySkipPermissions(boolean v) { this.dangerouslySkipPermissions = v; return this; }
        public PrintOptions allowDangerouslySkipPermissions(boolean v) { this.allowDangerouslySkipPermissions = v; return this; }
        public PrintOptions disableSlashCommands(boolean v) { this.disableSlashCommands = v; return this; }
        public PrintOptions excludeDynamicSystemPromptSections(boolean v) { this.excludeDynamicSystemPromptSections = v; return this; }
        public PrintOptions includeHookEvents(boolean v) { this.includeHookEvents = v; return this; }
        public PrintOptions mcpDebug(boolean v) { this.mcpDebug = v; return this; }
        public PrintOptions betas(String v) { this.betas = v; return this; }
        public PrintOptions settings(String v) { this.settings = v; return this; }
        public PrintOptions settingSources(String v) { this.settingSources = v; return this; }
        public PrintOptions pluginDir(String... v) { this.pluginDir = v; return this; }
        public PrintOptions pluginUrl(String... v) { this.pluginUrl = v; return this; }
        public PrintOptions fileResources(String v) { this.fileResources = v; return this; }
        public PrintOptions remoteControl(String v) { this.remoteControl = v; return this; }
        public PrintOptions remoteControlSessionNamePrefix(String v) { this.remoteControlSessionNamePrefix = v; return this; }
        public PrintOptions tmux(String v) { this.tmux = v; return this; }

        public String[] toArgs() {
            List<String> args = new ArrayList<>();
            if (model != null) { args.add("--model"); args.add(model); }
            if (fallbackModel != null) { args.add("--fallback-model"); args.add(fallbackModel); }
            if (outputFormat != null) { args.add("--output-format"); args.add(outputFormat); }
            if (includePartialMessages) { args.add("--include-partial-messages"); }
            if (replayUserMessages) { args.add("--replay-user-messages"); }
            if (inputFormat != null) { args.add("--input-format"); args.add(inputFormat); }
            if (permissionMode != null) { args.add("--permission-mode"); args.add(permissionMode); }
            if (effort != null) { args.add("--effort"); args.add(effort); }
            if (maxBudgetUsd != null) { args.add("--max-budget-usd"); args.add(maxBudgetUsd); }
            if (jsonSchema != null) { args.add("--json-schema"); args.add(jsonSchema); }
            if (systemPrompt != null) { args.add("--system-prompt"); args.add(systemPrompt); }
            if (systemPromptFile != null) { args.add("--system-prompt-file"); args.add(systemPromptFile); }
            if (appendSystemPrompt != null) { args.add("--append-system-prompt"); args.add(appendSystemPrompt); }
            if (appendSystemPromptFile != null) { args.add("--append-system-prompt-file"); args.add(appendSystemPromptFile); }
            if (agent != null) { args.add("--agent"); args.add(agent); }
            if (agents != null) { args.add("--agents"); args.add(agents); }
            if (allowedTools != null) { args.add("--allowedTools"); args.add(allowedTools); }
            if (disallowedTools != null) { args.add("--disallowedTools"); args.add(disallowedTools); }
            if (tools != null) { args.add("--tools"); args.add(tools); }
            if (mcpConfig != null) { args.add("--mcp-config"); args.add(mcpConfig); }
            if (strictMcpConfig) { args.add("--strict-mcp-config"); }
            if (addDir != null) { args.add("--add-dir"); args.add(addDir); }
            if (fileResources != null) { args.add("--file"); args.add(fileResources); }
            if (sessionId != null) { args.add("--session-id"); args.add(sessionId); }
            if (resumeSessionId != null) { args.add("-r"); args.add(resumeSessionId); }
            if (continueSession) { args.add("-c"); }
            if (forkSession) { args.add("--fork-session"); }
            if (fromPr != null) { args.add("--from-pr"); args.add(fromPr); }
            if (sessionName != null) { args.add("-n"); args.add(sessionName); }
            if (noSessionPersistence) { args.add("--no-session-persistence"); }
            if (worktreeName != null) { args.add("-w"); args.add(worktreeName); }
            else if (worktree) { args.add("-w"); }
            if (tmux != null) { args.add("--tmux"); if (!"classic".equals(tmux) && !"true".equals(tmux)) { args.add(tmux); } }
            if (bare) { args.add("--bare"); }
            if (brief) { args.add("--brief"); }
            if (debugFilter != null) { args.add("--debug"); args.add(debugFilter); }
            else if (debug) { args.add("--debug"); }
            if (debugFile != null) { args.add("--debug-file"); args.add(debugFile); }
            if (verbose) { args.add("--verbose"); }
            if (ide) { args.add("--ide"); }
            if (chrome) { args.add("--chrome"); }
            if (noChrome) { args.add("--no-chrome"); }
            if (dangerouslySkipPermissions) { args.add("--dangerously-skip-permissions"); }
            if (allowDangerouslySkipPermissions) { args.add("--allow-dangerously-skip-permissions"); }
            if (disableSlashCommands) { args.add("--disable-slash-commands"); }
            if (excludeDynamicSystemPromptSections) { args.add("--exclude-dynamic-system-prompt-sections"); }
            if (includeHookEvents) { args.add("--include-hook-events"); }
            if (mcpDebug) { args.add("--mcp-debug"); }
            if (betas != null) { args.add("--betas"); args.add(betas); }
            if (settings != null) { args.add("--settings"); args.add(settings); }
            if (settingSources != null) { args.add("--setting-sources"); args.add(settingSources); }
            if (pluginDir != null) {
                for (String p : pluginDir) { args.add("--plugin-dir"); args.add(p); }
            }
            if (pluginUrl != null) {
                for (String u : pluginUrl) { args.add("--plugin-url"); args.add(u); }
            }
            if (remoteControl != null) { args.add("--remote-control"); args.add(remoteControl); }
            if (remoteControlSessionNamePrefix != null) { args.add("--remote-control-session-name-prefix"); args.add(remoteControlSessionNamePrefix); }
            args.add("-p");
            if (prompt != null) { args.add(prompt); }
            return args.toArray(new String[0]);
        }
    }
}
