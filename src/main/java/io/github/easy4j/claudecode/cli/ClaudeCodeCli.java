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
package io.github.easy4j.claudecode.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper that maps every official {@code claude} CLI option onto a
 * strongly-typed Java method.
 *
 * <p>Each method delegates to {@link ClaudeCodeCliExecutor#execute(String...)}
 * with the appropriate argument vector. Higher-level composition lives in
 * {@link ClaudeCodeCli.PrintOptions} which produces the exact CLI argument
 * vector via {@link PrintOptions#toArgs()}.</p>
 *
 * <h3>Session management (core feature)</h3>
 * <ul>
 *   <li>{@code -c / --continue} — continue the most recent session
 *       ({@link #continue_()}, {@link #continue_(String)},
 *       {@link #continue_(String, String)}).</li>
 *   <li>{@code -r / --resume [ID]} — resume a specific session or open
 *       an interactive picker ({@link #resume()}, {@link #resume(String)}).</li>
 *   <li>{@code --fork-session} — fork a session into a new identifier
 *       ({@link #continueForkSession()}, {@link #resumeForkSession(String)}).</li>
 *   <li>{@code --session-id <uuid>} — start a brand-new session
 *       ({@link #withSessionId(String, String)}).</li>
 *   <li>{@code --from-pr [value]} — resume a PR-bound session
 *       ({@link #fromPr(String)}, {@link #fromPr()}).</li>
 *   <li>{@code --no-session-persistence} — non-persistent session
 *       ({@link #printNoPersistence(String)}).</li>
 *   <li>{@code -n / --name <name>} — give the session a display name
 *       ({@link #namedSession(String, String)}).</li>
 * </ul>
 *
 * @author easy-4-java contributors
 * @since 3.0.0
 * @see ClaudeCodeCliExecutor
 * @see <a href="https://docs.anthropic.com/en/docs/claude-code">Claude Code CLI</a>
 */
public class ClaudeCodeCli {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCodeCli.class);

    private final ClaudeCodeCliExecutor executor;

    /**
     * Construct a new CLI facade wrapping the supplied executor.
     *
     * @param executor the executor used to run the underlying subprocess
     */
    public ClaudeCodeCli(ClaudeCodeCliExecutor executor) {
        this.executor = executor;
    }

    /**
     * @return the wrapped {@link ClaudeCodeCliExecutor}
     */
    public ClaudeCodeCliExecutor executor() {
        return executor;
    }

    // ============================================================
    // Global
    // ============================================================

    /**
     * Run {@code claude --version}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult version() {
        return executor.execute("--version");
    }

    /**
     * Run {@code claude --help}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult help() {
        return executor.execute("--help");
    }

    // ============================================================
    // print (-p) — non-interactive output
    // ============================================================

    /**
     * Run {@code claude -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult print(String prompt) {
        return executor.execute("-p", prompt);
    }

    /**
     * Run {@code claude -p --model <model> <prompt>}.
     *
     * @param prompt the user prompt
     * @param model  the model identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult print(String prompt, String model) {
        return executor.execute("-p", "--model", model, prompt);
    }

    /**
     * Run {@code claude -p --output-format stream-json --include-partial-messages <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printStreamJson(String prompt) {
        return executor.execute("-p", "--output-format", "stream-json", "--include-partial-messages", prompt);
    }

    /**
     * Run {@code claude -p --output-format json <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printJson(String prompt) {
        return executor.execute("-p", "--output-format", "json", prompt);
    }

    /**
     * Run {@code claude -p --output-format stream-json --input-format stream-json
     * --replay-user-messages --include-partial-messages} (bidirectional mode).
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printStreamJsonBidirectional() {
        return executor.execute("-p", "--output-format", "stream-json",
                "--input-format", "stream-json",
                "--replay-user-messages", "--include-partial-messages");
    }

    /**
     * Run {@code claude -p --json-schema <schema> <prompt>}.
     *
     * @param prompt     the user prompt
     * @param jsonSchema the JSON-Schema spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSchema(String prompt, String jsonSchema) {
        return executor.execute("-p", "--json-schema", jsonSchema, prompt);
    }

    /**
     * Execute using the fully configured {@link PrintOptions}.
     *
     * @param opts pre-built print options
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult print(PrintOptions opts) {
        return executor.execute(opts.toArgs());
    }

    // ============================================================
    // Session lifecycle — continue / resume / fork
    // ============================================================

    /**
     * Run {@code claude -c} — continue the most recent conversation.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continue_() {
        return executor.execute("-c");
    }

    /**
     * Run {@code claude -c -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continue_(String prompt) {
        return executor.execute("-c", "-p", prompt);
    }

    /**
     * Run {@code claude -c --model <model> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param model  the model identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continue_(String prompt, String model) {
        return executor.execute("-c", "--model", model, "-p", prompt);
    }

    /**
     * Run {@code claude -r [sessionId]} — interactive picker when no
     * session id is given.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resume() {
        return executor.execute("-r");
    }

    /**
     * Run {@code claude -r <sessionId>} — resume by id.
     *
     * @param sessionId the session identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resume(String sessionId) {
        return executor.execute("-r", sessionId);
    }

    /**
     * Run {@code claude -r <sessionId> -p <prompt>}.
     *
     * @param sessionId the session identifier
     * @param prompt    the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resume(String sessionId, String prompt) {
        return executor.execute("-r", sessionId, "-p", prompt);
    }

    /**
     * Run {@code claude -r <sessionId> --model <model> -p <prompt>}.
     *
     * @param sessionId the session identifier
     * @param prompt    the user prompt
     * @param model     the model identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resume(String sessionId, String prompt, String model) {
        return executor.execute("-r", sessionId, "--model", model, "-p", prompt);
    }

    /**
     * Run {@code claude -c --fork-session} — continue as a new fork.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continueForkSession() {
        return executor.execute("-c", "--fork-session");
    }

    /**
     * Run {@code claude -r <id> --fork-session} — resume as a new fork.
     *
     * @param sessionId the session identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeForkSession(String sessionId) {
        return executor.execute("-r", sessionId, "--fork-session");
    }

    /**
     * Run {@code claude -r <id> --fork-session -p <prompt>}.
     *
     * @param sessionId the session identifier
     * @param prompt    the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeForkSession(String sessionId, String prompt) {
        return executor.execute("-r", sessionId, "--fork-session", "-p", prompt);
    }

    // ============================================================
    // Session ID / PR / Name
    // ============================================================

    /**
     * Run {@code claude --session-id <uuid> -p <prompt>}.
     *
     * @param uuid   the new session identifier
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult withSessionId(String uuid, String prompt) {
        return executor.execute("--session-id", uuid, "-p", prompt);
    }

    /**
     * Run {@code claude --from-pr <prNumber>}.
     *
     * @param prNumber the PR number
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult fromPr(String prNumber) {
        return executor.execute("--from-pr", prNumber);
    }

    /**
     * Run {@code claude --from-pr} (interactive picker).
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult fromPr() {
        return executor.execute("--from-pr");
    }

    /**
     * Run {@code claude -n <name> -p <prompt>} — name the session.
     *
     * @param name   the session display name
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult namedSession(String name, String prompt) {
        return executor.execute("-n", name, "-p", prompt);
    }

    /**
     * Run {@code claude --no-session-persistence -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printNoPersistence(String prompt) {
        return executor.execute("--no-session-persistence", "-p", prompt);
    }

    // ============================================================
    // permission mode
    // ============================================================

    /**
     * Run {@code claude --permission-mode <mode> -p <prompt>}.
     *
     * @param prompt         the user prompt
     * @param permissionMode the permission mode identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithPermission(String prompt, String permissionMode) {
        return executor.execute("--permission-mode", permissionMode, "-p", prompt);
    }

    /**
     * Run {@code claude --dangerously-skip-permissions -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printBypassPermissions(String prompt) {
        return executor.execute("--dangerously-skip-permissions", "-p", prompt);
    }

    // ============================================================
    // worktree / add-dir
    // ============================================================

    /**
     * Run {@code claude -w -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printInWorktree(String prompt) {
        return executor.execute("-w", "-p", prompt);
    }

    /**
     * Run {@code claude -w <name> -p <prompt>} — named worktree.
     *
     * @param name   the worktree name
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printInWorktree(String name, String prompt) {
        return executor.execute("-w", name, "-p", prompt);
    }

    /**
     * Run {@code claude --add-dir <dir> -p <prompt>}.
     *
     * @param dir    the extra directory path
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDir(String dir, String prompt) {
        return executor.execute("--add-dir", dir, "-p", prompt);
    }

    // ============================================================
    // effort / budget
    // ============================================================

    /**
     * Run {@code claude --effort <level> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param effort the effort level identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithEffort(String prompt, String effort) {
        return executor.execute("--effort", effort, "-p", prompt);
    }

    /**
     * Run {@code claude --max-budget-usd <amount> -p <prompt>}.
     *
     * @param prompt       the user prompt
     * @param maxBudgetUsd the budget ceiling in USD
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithBudget(String prompt, double maxBudgetUsd) {
        return executor.execute("--max-budget-usd", String.valueOf(maxBudgetUsd), "-p", prompt);
    }

    // ============================================================
    // system prompt
    // ============================================================

    /**
     * Run {@code claude --system-prompt <prompt> -p <prompt>}.
     *
     * @param userPrompt   the user prompt
     * @param systemPrompt the system prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSystemPrompt(String userPrompt, String systemPrompt) {
        return executor.execute("--system-prompt", systemPrompt, "-p", userPrompt);
    }

    /**
     * Run {@code claude --append-system-prompt <prompt> -p <prompt>}.
     *
     * @param userPrompt    the user prompt
     * @param appendPrompt  text appended to the system prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAppendSystemPrompt(String userPrompt, String appendPrompt) {
        return executor.execute("--append-system-prompt", appendPrompt, "-p", userPrompt);
    }

    // ============================================================
    // agents / tools / mcp
    // ============================================================

    /**
     * Run {@code claude --agents <json> -p <prompt>}.
     *
     * @param prompt     the user prompt
     * @param agentsJson raw JSON describing the agents
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAgents(String prompt, String agentsJson) {
        return executor.execute("--agents", agentsJson, "-p", prompt);
    }

    /**
     * Run {@code claude --allowedTools <tools> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param tools  comma-separated allowed tools
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAllowedTools(String prompt, String tools) {
        return executor.execute("--allowedTools", tools, "-p", prompt);
    }

    /**
     * Run {@code claude --mcp-config <config> -p <prompt>}.
     *
     * @param prompt    the user prompt
     * @param mcpConfig the MCP configuration spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithMcpConfig(String prompt, String mcpConfig) {
        return executor.execute("--mcp-config", mcpConfig, "-p", prompt);
    }

    // ============================================================
    // Subcommands
    // ============================================================

    /**
     * Run {@code claude agents --json}.
     *
     * @return the underlying CLI result whose stdout is JSON
     */
    public ClaudeCodeCliResult agentsList() {
        return executor.execute("agents", "--json");
    }

    /**
     * Pass through to {@code claude agents [options]}.
     *
     * @param args CLI arguments following {@code agents}
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult agents(String... args) {
        String[] all = new String[args.length + 1];
        all[0] = "agents";
        System.arraycopy(args, 0, all, 1, args.length);
        return executor.execute(all);
    }

    /**
     * Run {@code claude auth login}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult authLogin() {
        return executor.execute("auth", "login");
    }

    /**
     * Run {@code claude auth logout}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult authLogout() {
        return executor.execute("auth", "logout");
    }

    /**
     * Run {@code claude auth status}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult authStatus() {
        return executor.execute("auth", "status");
    }

    /**
     * Run {@code claude doctor}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult doctor() {
        return executor.execute("doctor");
    }

    /**
     * Run {@code claude install [target]}.
     *
     * @param target install destination
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult install(String target) {
        return executor.execute("install", target);
    }

    /**
     * Run {@code claude install}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult install() {
        return executor.execute("install");
    }

    /**
     * Pass through to {@code claude mcp [subcommand...]}.
     *
     * @param args CLI arguments following {@code mcp}
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcp(String... args) {
        String[] all = new String[args.length + 1];
        all[0] = "mcp";
        System.arraycopy(args, 0, all, 1, args.length);
        return executor.execute(all);
    }

    /**
     * Run {@code claude mcp list}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpList() {
        return executor.execute("mcp", "list");
    }

    /**
     * Run {@code claude mcp add <name> <commandOrUrl> [args...]}.
     *
     * @param name          MCP server name
     * @param commandOrUrl  the launch command or URL
     * @param args          additional server arguments
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpAdd(String name, String commandOrUrl, String... args) {
        List<String> all = new ArrayList<>();
        all.add("mcp"); all.add("add"); all.add(name); all.add(commandOrUrl);
        for (String a : args) all.add(a);
        return executor.execute(all.toArray(new String[0]));
    }

    /**
     * Run {@code claude mcp get <name>}.
     *
     * @param name MCP server name
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpGet(String name) {
        return executor.execute("mcp", "get", name);
    }

    /**
     * Run {@code claude mcp remove <name>}.
     *
     * @param name MCP server name
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpRemove(String name) {
        return executor.execute("mcp", "remove", name);
    }

    /**
     * Run {@code claude mcp serve}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpServe() {
        return executor.execute("mcp", "serve");
    }

    /**
     * Run {@code claude plugin list}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult pluginList() {
        return executor.execute("plugin", "list");
    }

    /**
     * Run {@code claude plugin install <plugin>}.
     *
     * @param plugin plugin identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult pluginInstall(String plugin) {
        return executor.execute("plugin", "install", plugin);
    }

    /**
     * Pass through to {@code claude plugin [subcommand...]}.
     *
     * @param args CLI arguments following {@code plugin}
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult plugin(String... args) {
        String[] all = new String[args.length + 1];
        all[0] = "plugin";
        System.arraycopy(args, 0, all, 1, args.length);
        return executor.execute(all);
    }

    /**
     * Run {@code claude project purge}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult projectPurge() {
        return executor.execute("project", "purge");
    }

    /**
     * Run {@code claude setup-token}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult setupToken() {
        return executor.execute("setup-token");
    }

    /**
     * Run {@code claude update}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult update() {
        return executor.execute("update");
    }

    /**
     * Run {@code claude ultrareview}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult ultrareview() {
        return executor.execute("ultrareview");
    }

    /**
     * Run {@code claude ultrareview <target> --timeout <min>}.
     *
     * @param target         review target
     * @param timeoutMinutes timeout in minutes
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult ultrareview(String target, int timeoutMinutes) {
        return executor.execute("ultrareview", target, "--timeout", String.valueOf(timeoutMinutes));
    }

    /**
     * Run {@code claude --bare -p <prompt>} — minimal mode.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult barePrint(String prompt) {
        return executor.execute("--bare", "-p", prompt);
    }

    /**
     * Run {@code claude --brief -p <prompt>} — enable agent-to-user communication.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult briefPrint(String prompt) {
        return executor.execute("--brief", "-p", prompt);
    }

    /**
     * Run {@code claude --debug -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult debugPrint(String prompt) {
        return executor.execute("--debug", "-p", prompt);
    }

    /**
     * Run {@code claude --verbose -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult verbosePrint(String prompt) {
        return executor.execute("--verbose", "-p", prompt);
    }

    /**
     * Run {@code claude --ide -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult idePrint(String prompt) {
        return executor.execute("--ide", "-p", prompt);
    }

    // ============================================================
    // agent / fallback-model / tools
    // ============================================================

    /**
     * Run {@code claude --agent <agent> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param agent  the agent identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAgent(String prompt, String agent) {
        return executor.execute("--agent", agent, "-p", prompt);
    }

    /**
     * Run {@code claude --fallback-model <model> --model <model> -p <prompt>}.
     *
     * @param prompt        the user prompt
     * @param model         the primary model
     * @param fallbackModel the fallback model
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithFallbackModel(String prompt, String model, String fallbackModel) {
        return executor.execute("--model", model, "--fallback-model", fallbackModel, "-p", prompt);
    }

    /**
     * Run {@code claude --disallowedTools <tools> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param tools  comma-separated disallowed tools
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDisallowedTools(String prompt, String tools) {
        return executor.execute("--disallowedTools", tools, "-p", prompt);
    }

    /**
     * Run {@code claude --tools <tools> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param tools  the tool-set spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithTools(String prompt, String tools) {
        return executor.execute("--tools", tools, "-p", prompt);
    }

    // ============================================================
    // system-prompt-file / append-system-prompt-file
    // ============================================================

    /**
     * Run {@code claude --system-prompt-file <path> -p <prompt>}.
     *
     * @param userPrompt       the user prompt
     * @param systemPromptFile path to the system-prompt file
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSystemPromptFile(String userPrompt, String systemPromptFile) {
        return executor.execute("--system-prompt-file", systemPromptFile, "-p", userPrompt);
    }

    /**
     * Run {@code claude --append-system-prompt-file <path> -p <prompt>}.
     *
     * @param userPrompt the user prompt
     * @param appendFile path to the file appended to the system prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAppendSystemPromptFile(String userPrompt, String appendFile) {
        return executor.execute("--append-system-prompt-file", appendFile, "-p", userPrompt);
    }

    // ============================================================
    // strict-mcp-config / settings / setting-sources
    // ============================================================

    /**
     * Run {@code claude --mcp-config <config> --strict-mcp-config -p <prompt>}.
     *
     * @param prompt    the user prompt
     * @param mcpConfig the MCP configuration spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithStrictMcpConfig(String prompt, String mcpConfig) {
        return executor.execute("--mcp-config", mcpConfig, "--strict-mcp-config", "-p", prompt);
    }

    /**
     * Run {@code claude --settings <file-or-json> -p <prompt>}.
     *
     * @param prompt   the user prompt
     * @param settings the settings spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSettings(String prompt, String settings) {
        return executor.execute("--settings", settings, "-p", prompt);
    }

    /**
     * Run {@code claude --setting-sources <sources> -p <prompt>}.
     *
     * @param prompt  the user prompt
     * @param sources comma-separated settings sources
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSettingSources(String prompt, String sources) {
        return executor.execute("--setting-sources", sources, "-p", prompt);
    }

    // ============================================================
    // plugin-dir / plugin-url / file
    // ============================================================

    /**
     * Run {@code claude --plugin-dir <path>... -p <prompt>} (repeatable).
     *
     * @param prompt   the user prompt
     * @param pluginDir one or more plugin directories or {@code .zip} archives
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithPluginDir(String prompt, String... pluginDir) {
        List<String> args = new ArrayList<>();
        for (String p : pluginDir) { args.add("--plugin-dir"); args.add(p); }
        args.add("-p"); args.add(prompt);
        return executor.execute(args.toArray(new String[0]));
    }

    /**
     * Run {@code claude --plugin-url <url>... -p <prompt>} (repeatable).
     *
     * @param prompt    the user prompt
     * @param pluginUrl one or more plugin URLs
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithPluginUrl(String prompt, String... pluginUrl) {
        List<String> args = new ArrayList<>();
        for (String u : pluginUrl) { args.add("--plugin-url"); args.add(u); }
        args.add("-p"); args.add(prompt);
        return executor.execute(args.toArray(new String[0]));
    }

    /**
     * Run {@code claude --file <specs> -p <prompt>}.
     *
     * @param prompt    the user prompt
     * @param fileSpecs comma-separated {@code file_id:relative_path} specs
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithFiles(String prompt, String fileSpecs) {
        return executor.execute("--file", fileSpecs, "-p", prompt);
    }

    // ============================================================
    // tmux / remote-control
    // ============================================================

    /**
     * Run {@code claude --tmux -w -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithTmux(String prompt) {
        return executor.execute("--tmux", "-w", "-p", prompt);
    }

    /**
     * Run {@code claude --tmux=classic -w -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithClassicTmux(String prompt) {
        return executor.execute("--tmux=classic", "-w", "-p", prompt);
    }

    /**
     * Run {@code claude --remote-control <name>}.
     *
     * @param name remote-control session name
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult remoteControl(String name) {
        return executor.execute("--remote-control", name);
    }

    /**
     * Run {@code claude --remote-control}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult remoteControl() {
        return executor.execute("--remote-control");
    }

    /**
     * Run {@code claude --remote-control --remote-control-session-name-prefix <prefix>}.
     *
     * @param prefix name prefix used by remote-control sessions
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult remoteControlWithPrefix(String prefix) {
        return executor.execute("--remote-control", "--remote-control-session-name-prefix", prefix);
    }

    // ============================================================
    // Permissions / safety / debug  miscellaneous
    // ============================================================

    /**
     * Run {@code claude --allow-dangerously-skip-permissions -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printAllowBypassPermissions(String prompt) {
        return executor.execute("--allow-dangerously-skip-permissions", "-p", prompt);
    }

    /**
     * Run {@code claude --disable-slash-commands -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printDisableSlashCommands(String prompt) {
        return executor.execute("--disable-slash-commands", "-p", prompt);
    }

    /**
     * Run {@code claude --exclude-dynamic-system-prompt-sections -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printExcludeDynamicSections(String prompt) {
        return executor.execute("--exclude-dynamic-system-prompt-sections", "-p", prompt);
    }

    /**
     * Run {@code claude --include-hook-events --output-format stream-json -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithHookEvents(String prompt) {
        return executor.execute("--include-hook-events", "--output-format", "stream-json", "-p", prompt);
    }

    /**
     * Run {@code claude --no-chrome -p <prompt>}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printNoChrome(String prompt) {
        return executor.execute("--no-chrome", "-p", prompt);
    }

    /**
     * Run {@code claude --debug <filter> -p <prompt>}.
     *
     * @param prompt the user prompt
     * @param filter debug category filter
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDebugFilter(String prompt, String filter) {
        return executor.execute("--debug", filter, "-p", prompt);
    }

    /**
     * Run {@code claude --debug-file <path> -p <prompt>}.
     *
     * @param prompt    the user prompt
     * @param debugFile debug log file path
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDebugFile(String prompt, String debugFile) {
        return executor.execute("--debug-file", debugFile, "-p", prompt);
    }

    /**
     * Run {@code claude --mcp-debug -p <prompt>} (deprecated MCP debug).
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithMcpDebug(String prompt) {
        return executor.execute("--mcp-debug", "-p", prompt);
    }

    // ============================================================
    // auto-mode subcommand
    // ============================================================

    /**
     * Run {@code claude auto-mode} — inspect auto-mode classifier config.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult autoMode() {
        return executor.execute("auto-mode");
    }

    // ============================================================
    // PrintOptions builder
    // ============================================================

    /**
     * Builder that assembles the complete {@code claude -p} argument vector.
     *
     * <p>Each setter corresponds to one CLI option. Calling {@link #toArgs()}
     * produces the final argument array passed to
     * {@link ClaudeCodeCliExecutor#execute(String...)}.</p>
     *
     * @author easy-4-java contributors
     * @since 3.0.0
     */
    public static class PrintOptions {

        /** User prompt forwarded to the CLI. */
        private String prompt;

        /** Primary model identifier. */
        private String model;

        /** Output format ({@code text}, {@code json}, {@code stream-json}). */
        private String outputFormat = "stream-json";

        /** Whether to include partial message chunks. */
        private boolean includePartialMessages = true;

        /** Whether to replay user messages in {@code stream-json}. */
        private boolean replayUserMessages;

        /** Input format ({@code stream-json}). */
        private String inputFormat;

        /** Permission mode identifier. */
        private String permissionMode;

        /** Effort level identifier. */
        private String effort;

        /** Cached string form of the {@link #maxBudgetUsd} budget. */
        private String maxBudgetUsd;

        /** JSON-Schema spec. */
        private String jsonSchema;

        /** System prompt override. */
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

        /** Comma-separated allowed tools. */
        private String allowedTools;

        /** Comma-separated disallowed tools. */
        private String disallowedTools;

        /** Tool set spec. */
        private String tools;

        /** MCP configuration. */
        private String mcpConfig;

        /** Whether only {@link #mcpConfig} servers may be used. */
        private boolean strictMcpConfig;

        /** Fallback model identifier. */
        private String fallbackModel;

        /** Additional working directory. */
        private String addDir;

        /** New session identifier. */
        private String sessionId;

        /** Resume session identifier. */
        private String resumeSessionId;

        /** Whether to continue the most recent session. */
        private boolean continueSession;

        /** Whether to fork the resumed session into a new ID. */
        private boolean forkSession;

        /** PR number to resume a PR-bound session. */
        private String fromPr;

        /** Display name for the session. */
        private String sessionName;

        /** Disable session persistence. */
        private boolean noSessionPersistence;

        /** Run inside a git worktree. */
        private boolean worktree;

        /** Named worktree identifier. */
        private String worktreeName;

        /** tmux mode ({@code true} / {@code classic} / arbitrary value). */
        private String tmux;

        /** Bare mode flag. */
        private boolean bare;

        /** Brief mode flag. */
        private boolean brief;

        /** Debug mode flag. */
        private boolean debug;

        /** Debug category filter. */
        private String debugFilter;

        /** Debug log file path. */
        private String debugFile;

        /** Verbose mode flag. */
        private boolean verbose;

        /** IDE flag. */
        private boolean ide;

        /** Chrome flag. */
        private boolean chrome;

        /** No-Chrome flag. */
        private boolean noChrome;

        /** Skip permissions flag. */
        private boolean dangerouslySkipPermissions;

        /** Allow-skip-permissions flag. */
        private boolean allowDangerouslySkipPermissions;

        /** Disable slash commands flag. */
        private boolean disableSlashCommands;

        /** Exclude dynamic system prompt sections flag. */
        private boolean excludeDynamicSystemPromptSections;

        /** Include hook events flag. */
        private boolean includeHookEvents;

        /** [DEPRECATED] MCP debug flag. */
        private boolean mcpDebug;

        /** Custom {@code betas} header values. */
        private String betas;

        /** Settings file or JSON. */
        private String settings;

        /** Settings sources. */
        private String settingSources;

        /** Plugin directories (repeatable). */
        private String[] pluginDir;

        /** Plugin URLs (repeatable). */
        private String[] pluginUrl;

        /** File resource specs. */
        private String fileResources;

        /** Remote-control session name. */
        private String remoteControl;

        /** Remote-control session name prefix. */
        private String remoteControlSessionNamePrefix;

        /**
         * Create a new {@code PrintOptions} with the supplied prompt.
         *
         * @param prompt the user prompt
         */
        public PrintOptions(String prompt) { this.prompt = prompt; }

        /** @param v model identifier; returns {@code this} for chaining */
        public PrintOptions model(String v) { this.model = v; return this; }

        /** @param v output format; returns {@code this} for chaining */
        public PrintOptions outputFormat(String v) { this.outputFormat = v; return this; }

        /** @param v include partial messages; returns {@code this} for chaining */
        public PrintOptions includePartialMessages(boolean v) { this.includePartialMessages = v; return this; }

        /** @param v replay user messages; returns {@code this} for chaining */
        public PrintOptions replayUserMessages(boolean v) { this.replayUserMessages = v; return this; }

        /** @param v input format; returns {@code this} for chaining */
        public PrintOptions inputFormat(String v) { this.inputFormat = v; return this; }

        /** @param v permission mode; returns {@code this} for chaining */
        public PrintOptions permissionMode(String v) { this.permissionMode = v; return this; }

        /** @param v effort level; returns {@code this} for chaining */
        public PrintOptions effort(String v) { this.effort = v; return this; }

        /** @param v budget ceiling in USD; returns {@code this} for chaining */
        public PrintOptions maxBudgetUsd(double v) { this.maxBudgetUsd = String.valueOf(v); return this; }

        /** @param v JSON-Schema; returns {@code this} for chaining */
        public PrintOptions jsonSchema(String v) { this.jsonSchema = v; return this; }

        /** @param v system prompt; returns {@code this} for chaining */
        public PrintOptions systemPrompt(String v) { this.systemPrompt = v; return this; }

        /** @param v system prompt file; returns {@code this} for chaining */
        public PrintOptions systemPromptFile(String v) { this.systemPromptFile = v; return this; }

        /** @param v appended system prompt; returns {@code this} for chaining */
        public PrintOptions appendSystemPrompt(String v) { this.appendSystemPrompt = v; return this; }

        /** @param v appended system prompt file; returns {@code this} for chaining */
        public PrintOptions appendSystemPromptFile(String v) { this.appendSystemPromptFile = v; return this; }

        /** @param v agent identifier; returns {@code this} for chaining */
        public PrintOptions agent(String v) { this.agent = v; return this; }

        /** @param v agents JSON; returns {@code this} for chaining */
        public PrintOptions agents(String v) { this.agents = v; return this; }

        /** @param v allowed tools; returns {@code this} for chaining */
        public PrintOptions allowedTools(String v) { this.allowedTools = v; return this; }

        /** @param v disallowed tools; returns {@code this} for chaining */
        public PrintOptions disallowedTools(String v) { this.disallowedTools = v; return this; }

        /** @param v tools spec; returns {@code this} for chaining */
        public PrintOptions tools(String v) { this.tools = v; return this; }

        /** @param v MCP config; returns {@code this} for chaining */
        public PrintOptions mcpConfig(String v) { this.mcpConfig = v; return this; }

        /** @param v strict MCP flag; returns {@code this} for chaining */
        public PrintOptions strictMcpConfig(boolean v) { this.strictMcpConfig = v; return this; }

        /** @param v fallback model; returns {@code this} for chaining */
        public PrintOptions fallbackModel(String v) { this.fallbackModel = v; return this; }

        /** @param v add-dir path; returns {@code this} for chaining */
        public PrintOptions addDir(String v) { this.addDir = v; return this; }

        /** @param v session id; returns {@code this} for chaining */
        public PrintOptions sessionId(String v) { this.sessionId = v; return this; }

        /** @param v resume session id; returns {@code this} for chaining */
        public PrintOptions resumeSessionId(String v) { this.resumeSessionId = v; return this; }

        /** @param v continue flag; returns {@code this} for chaining */
        public PrintOptions continueSession(boolean v) { this.continueSession = v; return this; }

        /** @param v fork flag; returns {@code this} for chaining */
        public PrintOptions forkSession(boolean v) { this.forkSession = v; return this; }

        /** @param v PR number; returns {@code this} for chaining */
        public PrintOptions fromPr(String v) { this.fromPr = v; return this; }

        /** @param v session name; returns {@code this} for chaining */
        public PrintOptions sessionName(String v) { this.sessionName = v; return this; }

        /** @param v no-session-persistence flag; returns {@code this} for chaining */
        public PrintOptions noSessionPersistence(boolean v) { this.noSessionPersistence = v; return this; }

        /** @param v worktree flag; returns {@code this} for chaining */
        public PrintOptions worktree(boolean v) { this.worktree = v; return this; }

        /** @param v worktree name; returns {@code this} for chaining */
        public PrintOptions worktreeName(String v) { this.worktreeName = v; return this; }

        /** @param v bare flag; returns {@code this} for chaining */
        public PrintOptions bare(boolean v) { this.bare = v; return this; }

        /** @param v brief flag; returns {@code this} for chaining */
        public PrintOptions brief(boolean v) { this.brief = v; return this; }

        /** @param v debug flag; returns {@code this} for chaining */
        public PrintOptions debug(boolean v) { this.debug = v; return this; }

        /** @param v debug filter; returns {@code this} for chaining */
        public PrintOptions debugFilter(String v) { this.debugFilter = v; return this; }

        /** @param v debug file path; returns {@code this} for chaining */
        public PrintOptions debugFile(String v) { this.debugFile = v; return this; }

        /** @param v verbose flag; returns {@code this} for chaining */
        public PrintOptions verbose(boolean v) { this.verbose = v; return this; }

        /** @param v IDE flag; returns {@code this} for chaining */
        public PrintOptions ide(boolean v) { this.ide = v; return this; }

        /** @param v Chrome flag; returns {@code this} for chaining */
        public PrintOptions chrome(boolean v) { this.chrome = v; return this; }

        /** @param v no-Chrome flag; returns {@code this} for chaining */
        public PrintOptions noChrome(boolean v) { this.noChrome = v; return this; }

        /** @param v skip-permissions flag; returns {@code this} for chaining */
        public PrintOptions dangerouslySkipPermissions(boolean v) { this.dangerouslySkipPermissions = v; return this; }

        /** @param v allow-skip-permissions flag; returns {@code this} for chaining */
        public PrintOptions allowDangerouslySkipPermissions(boolean v) { this.allowDangerouslySkipPermissions = v; return this; }

        /** @param v disable-slash-commands flag; returns {@code this} for chaining */
        public PrintOptions disableSlashCommands(boolean v) { this.disableSlashCommands = v; return this; }

        /** @param v exclude-dynamic-sections flag; returns {@code this} for chaining */
        public PrintOptions excludeDynamicSystemPromptSections(boolean v) { this.excludeDynamicSystemPromptSections = v; return this; }

        /** @param v include-hook-events flag; returns {@code this} for chaining */
        public PrintOptions includeHookEvents(boolean v) { this.includeHookEvents = v; return this; }

        /** @param v MCP debug flag; returns {@code this} for chaining */
        public PrintOptions mcpDebug(boolean v) { this.mcpDebug = v; return this; }

        /** @param v betas header value; returns {@code this} for chaining */
        public PrintOptions betas(String v) { this.betas = v; return this; }

        /** @param v settings spec; returns {@code this} for chaining */
        public PrintOptions settings(String v) { this.settings = v; return this; }

        /** @param v setting sources; returns {@code this} for chaining */
        public PrintOptions settingSources(String v) { this.settingSources = v; return this; }

        /** @param v plugin directories; returns {@code this} for chaining */
        public PrintOptions pluginDir(String... v) { this.pluginDir = v; return this; }

        /** @param v plugin URLs; returns {@code this} for chaining */
        public PrintOptions pluginUrl(String... v) { this.pluginUrl = v; return this; }

        /** @param v file resource specs; returns {@code this} for chaining */
        public PrintOptions fileResources(String v) { this.fileResources = v; return this; }

        /** @param v remote-control name; returns {@code this} for chaining */
        public PrintOptions remoteControl(String v) { this.remoteControl = v; return this; }

        /** @param v remote-control name prefix; returns {@code this} for chaining */
        public PrintOptions remoteControlSessionNamePrefix(String v) { this.remoteControlSessionNamePrefix = v; return this; }

        /** @param v tmux mode; returns {@code this} for chaining */
        public PrintOptions tmux(String v) { this.tmux = v; return this; }

        /**
         * Materialise the configured options as a CLI argument array.
         *
         * <p>The prompt is always appended last so it can be a free-form
         * string with no risk of being misinterpreted as a flag.</p>
         *
         * @return the CLI argument array (never {@code null})
         */
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
