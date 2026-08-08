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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.easy4j.claudecode.cli.ClaudeCodeCli;
import io.github.easy4j.claudecode.cli.ClaudeCodeCliExecutor;
import io.github.easy4j.claudecode.cli.ClaudeCodeCliResult;
import io.github.easy4j.claudecode.model.ClaudeAgent;
import io.github.easy4j.claudecode.model.ClaudeMessage;
import io.github.easy4j.claudecode.model.ClaudeResult;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Facade for the Claude Code CLI: a thin Java wrapper that turns the
 * Anthropic {@code claude} command-line agent into a programmatic API.
 *
 * <p>The client delegates every command to a local {@code claude}
 * subprocess through {@link ClaudeCodeCliExecutor}. All blocking calls
 * return a {@link ClaudeCodeCliResult} carrying the captured stdout,
 * stderr and exit code; the higher-level convenience methods
 * ({@link #printStreamJson(String)}, {@link #printStreamJsonAndParse(String)},
 * {@link #agentsListAsObjects()}) additionally parse the JSON payloads that
 * Claude Code emits.</p>
 *
 * <h3>Session management</h3>
 * <p>Claude Code persists sessions to the local file system. The following
 * command-line mechanisms are exposed as first-class Java methods:</p>
 * <ul>
 *   <li>{@code -c / --continue} — continue the most recent session
 *       ({@link #continueSession()}).</li>
 *   <li>{@code -r / --resume [ID]} — resume a specific session or open an
 *       interactive picker ({@link #resumeSession()}).</li>
 *   <li>{@code --fork-session} — fork a session into a new ID
 *       ({@link #continueForkSession()}, {@link #resumeForkSession(String)}).</li>
 *   <li>{@code --session-id <uuid>} — start a brand-new session with the
 *       supplied identifier ({@link #withSessionId(String, String)}).</li>
 *   <li>{@code --from-pr [number]} — resume a session associated with a PR
 *       ({@link #fromPr(String)}).</li>
 *   <li>{@code -n <name>} — name a session so it can be located later
 *       ({@link #namedSession(String, String)}).</li>
 *   <li>{@code --no-session-persistence} — run a one-shot, non-persistent
 *       session ({@link #printNoPersistence(String)}).</li>
 * </ul>
 *
 * <p>The class implements {@link AutoCloseable} so it can be used with
 * try-with-resources. {@link #close()} is currently a no-op because
 * the underlying executor does not hold any persistent resources
 * beyond a per-invocation subprocess.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ClaudeCodeCli
 * @see ClaudeCodeCliExecutor
 * @see ClaudeCodeCliResult
 */
public class ClaudeCodeClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCodeClient.class);

    /**
     * Shared Jackson mapper configured to ignore unknown properties.
     * This is the same mapper used for parsing both {@code stream-json}
     * payloads and the {@code claude agents --json} response.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ClaudeCodeClientConfig config;
    private final ClaudeCodeCli cli;

    /**
     * Construct a new client backed by a freshly built CLI instance that
     * owns its own {@link ClaudeCodeCliExecutor}.
     *
     * @param config non-null client configuration
     * @throws NullPointerException if {@code config} is {@code null}
     */
    public ClaudeCodeClient(ClaudeCodeClientConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.cli = new ClaudeCodeCli(new ClaudeCodeCliExecutor(config));
    }

    /**
     * Construct a new client wrapping a caller-supplied {@link ClaudeCodeCli}
     * (typically used in tests with a mock executor).
     *
     * @param config non-null client configuration
     * @param cli    non-null CLI facade to delegate to
     * @throws NullPointerException if {@code config} or {@code cli} is {@code null}
     */
    public ClaudeCodeClient(ClaudeCodeClientConfig config, ClaudeCodeCli cli) {
        this.config = Objects.requireNonNull(config, "config");
        this.cli = Objects.requireNonNull(cli, "cli");
    }

    // ============================================================
    // Basic information
    // ============================================================

    /**
     * Invoke {@code claude --version}.
     *
     * @return the underlying CLI result with stdout containing the version string
     */
    public ClaudeCodeCliResult version() { return cli.version(); }

    /**
     * Invoke {@code claude --help}.
     *
     * @return the underlying CLI result with the help text on stdout
     */
    public ClaudeCodeCliResult help() { return cli.help(); }

    // ============================================================
    // print — non-interactive execution (core)
    // ============================================================

    /**
     * Send {@code prompt} to {@code claude -p} using default options
     * sourced from {@link ClaudeCodeClientConfig}.
     *
     * @param prompt the user prompt (may be {@code null} when streaming)
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult print(String prompt) {
        return cli.print(defaultPrintOptions(prompt));
    }

    /**
     * Send {@code prompt} using an explicit model identifier, ignoring
     * {@link ClaudeCodeClientConfig#getDefaultModel()}.
     *
     * @param prompt the user prompt
     * @param model  the model identifier (e.g. {@code claude-sonnet-4-6})
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult print(String prompt, String model) {
        return cli.print(new ClaudeCodeCli.PrintOptions(prompt).model(model));
    }

    /**
     * Send a fully configured {@link ClaudeCodeCli.PrintOptions} payload.
     *
     * @param opts pre-built print options
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult print(ClaudeCodeCli.PrintOptions opts) {
        return cli.print(opts);
    }

    /**
     * Run the {@code stream-json} print mode and parse the stdout into
     * a list of {@link ClaudeMessage} instances.
     *
     * <p>Unparseable lines are logged at DEBUG level and silently dropped so
     * that one bad record cannot poison the whole stream.</p>
     *
     * @param prompt the user prompt
     * @return ordered list of parsed messages (never {@code null}; empty
     *         if no output was produced)
     */
    public List<ClaudeMessage> printStreamJson(String prompt) {
        ClaudeCodeCliResult result = cli.printStreamJson(prompt);
        return parseStreamJsonOutput(result.getStdout());
    }

    /**
     * Run {@code stream-json} and bundle the parsed messages together
     * with the terminating {@code result} envelope.
     *
     * @param prompt the user prompt
     * @return a {@link StreamResult} containing the messages, the final
     *         result envelope and the raw CLI result
     */
    public StreamResult printStreamJsonAndParse(String prompt) {
        ClaudeCodeCliResult result = cli.printStreamJson(prompt);
        List<ClaudeMessage> messages = parseStreamJsonOutput(result.getStdout());
        ClaudeResult finalResult = findResult(messages);
        return new StreamResult(messages, finalResult, result);
    }

    /**
     * Run Claude Code in {@code stream-json} bidirectional mode,
     * suitable for piping additional messages back into the agent.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printBidirectional(String prompt) {
        return cli.print(new ClaudeCodeCli.PrintOptions(prompt)
                .outputFormat("stream-json")
                .inputFormat("stream-json")
                .includePartialMessages(true)
                .replayUserMessages(true));
    }

    /**
     * Run Claude Code with the single-result {@code json} output format.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printJson(String prompt) {
        return cli.printJson(prompt);
    }

    /**
     * Run Claude Code with a JSON-Schema to constrain the response shape.
     *
     * @param prompt     the user prompt
     * @param jsonSchema JSON-Schema string
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSchema(String prompt, String jsonSchema) {
        return cli.printWithSchema(prompt, jsonSchema);
    }

    // ============================================================
    // Session lifecycle
    // ============================================================

    /**
     * Continue the most recent conversation.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continueSession() {
        return cli.continue_();
    }

    /**
     * Continue the most recent conversation and send a prompt.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continueSession(String prompt) {
        return cli.continue_(prompt);
    }

    /**
     * Continue the most recent conversation with an explicit model.
     *
     * @param prompt the user prompt
     * @param model  the model identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continueSession(String prompt, String model) {
        return cli.continue_(prompt, model);
    }

    /**
     * Open the interactive resume picker.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeSession() {
        return cli.resume();
    }

    /**
     * Resume the session identified by {@code sessionId}.
     *
     * @param sessionId the session identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeSession(String sessionId) {
        return cli.resume(sessionId);
    }

    /**
     * Resume the session identified by {@code sessionId} and send a prompt.
     *
     * @param sessionId the session identifier
     * @param prompt    the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeSession(String sessionId, String prompt) {
        return cli.resume(sessionId, prompt);
    }

    /**
     * Resume a session with an explicit model.
     *
     * @param sessionId the session identifier
     * @param prompt    the user prompt
     * @param model     the model identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeSession(String sessionId, String prompt, String model) {
        return cli.resume(sessionId, prompt, model);
    }

    /**
     * Continue the most recent conversation but allocate a new session ID
     * (a "fork").
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult continueForkSession() {
        return cli.continueForkSession();
    }

    /**
     * Resume the named session as a new fork.
     *
     * @param sessionId the session identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeForkSession(String sessionId) {
        return cli.resumeForkSession(sessionId);
    }

    /**
     * Resume the named session as a new fork and immediately send a prompt.
     *
     * @param sessionId the session identifier
     * @param prompt    the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult resumeForkSession(String sessionId, String prompt) {
        return cli.resumeForkSession(sessionId, prompt);
    }

    /**
     * Begin a brand-new session with the supplied UUID.
     *
     * @param uuid   the new session identifier
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult withSessionId(String uuid, String prompt) {
        return cli.withSessionId(uuid, prompt);
    }

    /**
     * Resume the conversation associated with PR {@code prNumber}.
     *
     * @param prNumber the pull-request number
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult fromPr(String prNumber) {
        return cli.fromPr(prNumber);
    }

    /**
     * Open the interactive PR picker.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult fromPr() {
        return cli.fromPr();
    }

    /**
     * Run with a named session so that {@code -r} can locate it later.
     *
     * @param name   the display name of the session
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult namedSession(String name, String prompt) {
        return cli.namedSession(name, prompt);
    }

    /**
     * Run a one-shot, non-persistent session.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printNoPersistence(String prompt) {
        return cli.printNoPersistence(prompt);
    }

    // ============================================================
    // Permission control
    // ============================================================

    /**
     * Run {@code -p} with an explicit permission mode
     * (e.g. {@code default}, {@code acceptEdits}, {@code bypassPermissions},
     * {@code plan}, {@code auto}).
     *
     * @param prompt         the user prompt
     * @param permissionMode the permission mode identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithPermission(String prompt, String permissionMode) {
        return cli.printWithPermission(prompt, permissionMode);
    }

    /**
     * Skip all permission checks via {@code --dangerously-skip-permissions}.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printBypassPermissions(String prompt) {
        return cli.printBypassPermissions(prompt);
    }

    // ============================================================
    // worktree / directory
    // ============================================================

    /**
     * Run {@code -p} inside an anonymous worktree.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printInWorktree(String prompt) {
        return cli.printInWorktree(prompt);
    }

    /**
     * Run {@code -p} inside a named worktree.
     *
     * @param name   the worktree name
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printInWorktree(String name, String prompt) {
        return cli.printInWorktree(name, prompt);
    }

    /**
     * Run {@code -p} with an additional working directory added.
     *
     * @param dir    the extra directory path
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDir(String dir, String prompt) {
        return cli.printWithDir(dir, prompt);
    }

    // ============================================================
    // effort / budget
    // ============================================================

    /**
     * Set the reasoning effort level
     * (one of {@code low, medium, high, xhigh, max}).
     *
     * @param prompt the user prompt
     * @param effort the effort identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithEffort(String prompt, String effort) {
        return cli.printWithEffort(prompt, effort);
    }

    /**
     * Cap the spend for this invocation in US dollars.
     *
     * @param prompt       the user prompt
     * @param maxBudgetUsd the budget ceiling in USD
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithBudget(String prompt, double maxBudgetUsd) {
        return cli.printWithBudget(prompt, maxBudgetUsd);
    }

    // ============================================================
    // system prompt
    // ============================================================

    /**
     * Override the system prompt entirely for this invocation.
     *
     * @param userPrompt    the user prompt
     * @param systemPrompt  the full replacement system prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSystemPrompt(String userPrompt, String systemPrompt) {
        return cli.printWithSystemPrompt(userPrompt, systemPrompt);
    }

    /**
     * Append a fragment to the default system prompt.
     *
     * @param userPrompt the user prompt
     * @param append     text to append to the system prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAppendSystemPrompt(String userPrompt, String append) {
        return cli.printWithAppendSystemPrompt(userPrompt, append);
    }

    // ============================================================
    // agents / tools / mcp / plugins
    // ============================================================

    /**
     * Override the default agent.
     *
     * @param prompt the user prompt
     * @param agent  the agent identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAgent(String prompt, String agent) {
        return cli.printWithAgent(prompt, agent);
    }

    /**
     * Pass a JSON payload describing a custom set of agents.
     *
     * @param prompt     the user prompt
     * @param agentsJson raw JSON describing the agents
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAgents(String prompt, String agentsJson) {
        return cli.printWithAgents(prompt, agentsJson);
    }

    /**
     * Limit the available tool set.
     *
     * @param prompt the user prompt
     * @param tools  a tool-set spec accepted by Claude Code
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithTools(String prompt, String tools) {
        return cli.printWithTools(prompt, tools);
    }

    /**
     * Explicitly allow additional tools (comma-separated list).
     *
     * @param prompt the user prompt
     * @param tools  comma-separated allowed tools
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAllowedTools(String prompt, String tools) {
        return cli.printWithAllowedTools(prompt, tools);
    }

    /**
     * Explicitly deny tools (comma-separated list).
     *
     * @param prompt the user prompt
     * @param tools  comma-separated disallowed tools
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDisallowedTools(String prompt, String tools) {
        return cli.printWithDisallowedTools(prompt, tools);
    }

    /**
     * Provide a primary and fallback model so the CLI can degrade
     * gracefully on capacity errors.
     *
     * @param prompt        the user prompt
     * @param model         the primary model
     * @param fallbackModel the fallback model
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithFallbackModel(String prompt, String model, String fallbackModel) {
        return cli.printWithFallbackModel(prompt, model, fallbackModel);
    }

    /**
     * Attach an MCP configuration file.
     *
     * @param prompt    the user prompt
     * @param mcpConfig the MCP configuration spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithMcpConfig(String prompt, String mcpConfig) {
        return cli.printWithMcpConfig(prompt, mcpConfig);
    }

    /**
     * Attach an MCP configuration file and forbid any other MCP servers.
     *
     * @param prompt    the user prompt
     * @param mcpConfig the MCP configuration spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithStrictMcpConfig(String prompt, String mcpConfig) {
        return cli.printWithStrictMcpConfig(prompt, mcpConfig);
    }

    // ============================================================
    // system prompt — file variants
    // ============================================================

    /**
     * Load the system prompt from a file.
     *
     * @param userPrompt        the user prompt
     * @param systemPromptFile  path to the system-prompt file
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSystemPromptFile(String userPrompt, String systemPromptFile) {
        return cli.printWithSystemPromptFile(userPrompt, systemPromptFile);
    }

    /**
     * Append a file-based fragment to the default system prompt.
     *
     * @param userPrompt  the user prompt
     * @param appendFile  path to the file containing the fragment
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithAppendSystemPromptFile(String userPrompt, String appendFile) {
        return cli.printWithAppendSystemPromptFile(userPrompt, appendFile);
    }

    // ============================================================
    // settings / plugin / files
    // ============================================================

    /**
     * Provide an explicit settings file or inline settings JSON.
     *
     * @param prompt   the user prompt
     * @param settings the settings spec
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSettings(String prompt, String settings) {
        return cli.printWithSettings(prompt, settings);
    }

    /**
     * Restrict which settings sources the CLI loads
     * (one of {@code user}, {@code project}, {@code local}).
     *
     * @param prompt  the user prompt
     * @param sources comma-separated settings sources
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithSettingSources(String prompt, String sources) {
        return cli.printWithSettingSources(prompt, sources);
    }

    /**
     * Load one or more local plugin directories (or {@code .zip} archives).
     *
     * @param prompt   the user prompt
     * @param pluginDir single directory or zip path
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithPluginDir(String prompt, String pluginDir) {
        return cli.printWithPluginDir(prompt, pluginDir);
    }

    /**
     * Load one or more remote plugins by URL.
     *
     * @param prompt    the user prompt
     * @param pluginUrl single plugin URL
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithPluginUrl(String prompt, String pluginUrl) {
        return cli.printWithPluginUrl(prompt, pluginUrl);
    }

    /**
     * Pre-stage file resources
     * (format: {@code file_id:relative_path}).
     *
     * @param prompt    the user prompt
     * @param fileSpecs comma-separated file specs
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithFiles(String prompt, String fileSpecs) {
        return cli.printWithFiles(prompt, fileSpecs);
    }

    // ============================================================
    // tmux / remote-control
    // ============================================================

    /**
     * Run Claude Code inside an integrated tmux pane.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithTmux(String prompt) {
        return cli.printWithTmux(prompt);
    }

    /**
     * Run Claude Code inside the classic tmux implementation.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithClassicTmux(String prompt) {
        return cli.printWithClassicTmux(prompt);
    }

    /**
     * Start a remote-control session with the given name.
     *
     * @param name the remote-control session name
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult remoteControl(String name) {
        return cli.remoteControl(name);
    }

    /**
     * Start an anonymous remote-control session.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult remoteControl() {
        return cli.remoteControl();
    }

    // ============================================================
    // permissions / debug  miscellaneous
    // ============================================================

    /**
     * Disable the Chrome integration.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printNoChrome(String prompt) {
        return cli.printNoChrome(prompt);
    }

    /**
     * Allow skipping permissions to be selected as an option
     * (does not enable it by default).
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printAllowBypassPermissions(String prompt) {
        return cli.printAllowBypassPermissions(prompt);
    }

    /**
     * Disable slash commands (skills).
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printDisableSlashCommands(String prompt) {
        return cli.printDisableSlashCommands(prompt);
    }

    /**
     * Move dynamic sections out of the system prompt into the first
     * user message.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printExcludeDynamicSections(String prompt) {
        return cli.printExcludeDynamicSections(prompt);
    }

    /**
     * Include hook events inside the {@code stream-json} envelope.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithHookEvents(String prompt) {
        return cli.printWithHookEvents(prompt);
    }

    /**
     * Run with debug logging enabled and a category filter
     * (e.g. {@code api,hooks}).
     *
     * @param prompt the user prompt
     * @param filter comma-separated debug categories
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDebugFilter(String prompt, String filter) {
        return cli.printWithDebugFilter(prompt, filter);
    }

    /**
     * Run with debug logging written to a file.
     *
     * @param prompt   the user prompt
     * @param debugFile path of the debug log file
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithDebugFile(String prompt, String debugFile) {
        return cli.printWithDebugFile(prompt, debugFile);
    }

    /**
     * Enable the deprecated {@code --mcp-debug} flag.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult printWithMcpDebug(String prompt) {
        return cli.printWithMcpDebug(prompt);
    }

    // ============================================================
    // subcommands
    // ============================================================

    /**
     * List background agents in JSON form.
     *
     * @return the underlying CLI result whose stdout is JSON
     */
    public ClaudeCodeCliResult agentsList() {
        return cli.agentsList();
    }

    /**
     * List background agents and parse the JSON into a list of
     * {@link ClaudeAgent} objects.
     *
     * <p>An empty list is returned when the CLI call fails or returns no
     * payload; JSON parse errors are swallowed and logged at DEBUG.</p>
     *
     * @return parsed agent list (never {@code null})
     */
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

    /**
     * Pass through to the {@code claude agents} subcommand with custom args.
     *
     * @param args the CLI arguments that follow {@code agents}
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult agents(String... args) { return cli.agents(args); }

    /**
     * Run {@code claude auth login}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult authLogin() { return cli.authLogin(); }

    /**
     * Run {@code claude auth logout}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult authLogout() { return cli.authLogout(); }

    /**
     * Run {@code claude auth status}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult authStatus() { return cli.authStatus(); }

    /**
     * Run {@code claude doctor}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult doctor() { return cli.doctor(); }

    /**
     * Install Claude Code into a specific target.
     *
     * @param target install destination
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult install(String target) { return cli.install(target); }

    /**
     * Install Claude Code using the default target.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult install() { return cli.install(); }

    /**
     * List MCP servers.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpList() { return cli.mcpList(); }

    /**
     * Register a new MCP server.
     *
     * @param name          MCP server name
     * @param command       the command or URL used to launch the server
     * @param args          additional server arguments
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpAdd(String name, String command, String... args) { return cli.mcpAdd(name, command, args); }

    /**
     * Display a registered MCP server.
     *
     * @param name MCP server name
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpGet(String name) { return cli.mcpGet(name); }

    /**
     * Remove a registered MCP server.
     *
     * @param name MCP server name
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpRemove(String name) { return cli.mcpRemove(name); }

    /**
     * Run {@code claude mcp serve}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcpServe() { return cli.mcpServe(); }

    /**
     * Pass through to the {@code claude mcp} subcommand.
     *
     * @param args CLI arguments following {@code mcp}
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult mcp(String... args) { return cli.mcp(args); }

    /**
     * List installed plugins.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult pluginList() { return cli.pluginList(); }

    /**
     * Install a plugin by name.
     *
     * @param plugin plugin identifier
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult pluginInstall(String plugin) { return cli.pluginInstall(plugin); }

    /**
     * Pass through to the {@code claude plugin} subcommand.
     *
     * @param args CLI arguments following {@code plugin}
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult plugin(String... args) { return cli.plugin(args); }

    /**
     * Run {@code claude project purge}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult projectPurge() { return cli.projectPurge(); }

    /**
     * Run {@code claude setup-token}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult setupToken() { return cli.setupToken(); }

    /**
     * Run {@code claude update}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult update() { return cli.update(); }

    /**
     * Run {@code claude ultrareview}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult ultrareview() { return cli.ultrareview(); }

    /**
     * Run {@code claude ultrareview <target> --timeout <min>}.
     *
     * @param target        review target
     * @param timeoutMinutes timeout in minutes
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult ultrareview(String target, int timeoutMinutes) { return cli.ultrareview(target, timeoutMinutes); }

    /**
     * Run {@code claude auto-mode}.
     *
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult autoMode() { return cli.autoMode(); }

    // ============================================================
    // Special modes
    // ============================================================

    /**
     * Bare-mode print: minimal prompt-only invocation.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult barePrint(String prompt) { return cli.barePrint(prompt); }

    /**
     * Brief print: enables agent-to-user communication.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult briefPrint(String prompt) { return cli.briefPrint(prompt); }

    /**
     * Debug print: enables verbose debug logging.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult debugPrint(String prompt) { return cli.debugPrint(prompt); }

    /**
     * Verbose print: enables verbose output.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult verbosePrint(String prompt) { return cli.verbosePrint(prompt); }

    /**
     * IDE print: connects Claude Code to an IDE.
     *
     * @param prompt the user prompt
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult idePrint(String prompt) { return cli.idePrint(prompt); }

    /**
     * Execute an arbitrary CLI argument vector. The supplied args are
     * forwarded to {@link ClaudeCodeCliExecutor} exactly as given, after
     * the configured executable name.
     *
     * @param args CLI argument vector
     * @return the underlying CLI result
     */
    public ClaudeCodeCliResult execute(String... args) {
        return cli.executor().execute(args);
    }

    // ============================================================
    // CLI / Config accessors
    // ============================================================

    /**
     * @return the wrapped {@link ClaudeCodeCli} (never {@code null})
     */
    public ClaudeCodeCli cli() { return cli; }

    /**
     * @return the client configuration (never {@code null})
     */
    public ClaudeCodeClientConfig getConfig() { return config; }

    // ============================================================
    // Internal helpers
    // ============================================================

    /**
     * Build a default {@link ClaudeCodeCli.PrintOptions} from
     * {@link ClaudeCodeClientConfig}, applying every non-null option
     * onto a fresh {@code PrintOptions} instance.
     *
     * @param prompt the user prompt
     * @return a new {@code PrintOptions} populated from the config
     */
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

    /**
     * Parse the {@code stream-json} stdout payload line by line into
     * {@link ClaudeMessage} instances.
     *
     * @param stdout raw stdout captured from the CLI (may be {@code null})
     * @return parsed messages in order (never {@code null}; empty on
     *         {@code null}/empty input)
     */
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

    /**
     * Walk the parsed message list backwards to find the trailing
     * {@code result}-typed envelope and convert it into a
     * {@link ClaudeResult}.
     *
     * @param messages parsed stream-json messages
     * @return the last {@code ClaudeResult}, or {@code null} if no
     *         {@code result} envelope is present
     */
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
     * Bundle returned by {@link ClaudeCodeClient#printStreamJsonAndParse(String)}
     * that contains the parsed message stream, the final result envelope
     * and the raw CLI result for diagnostics.
     *
     * @author [@Loong Wan](https://github.com/loong10k)
     * @since 3.0.0
     */
    public static class StreamResult {

        /** Parsed stream-json messages in order. */
        private final List<ClaudeMessage> messages;

        /** Final {@code result} envelope, or {@code null} when missing. */
        private final ClaudeResult result;

        /** Raw CLI result for diagnostics and access to stderr/exit-code. */
        private final ClaudeCodeCliResult rawResult;

        /**
         * Create a new bundle.
         *
         * @param messages  parsed messages
         * @param result    final result envelope (may be {@code null})
         * @param rawResult raw CLI result
         */
        public StreamResult(List<ClaudeMessage> messages, ClaudeResult result, ClaudeCodeCliResult rawResult) {
            this.messages = messages;
            this.result = result;
            this.rawResult = rawResult;
        }

        /** @return parsed stream-json messages */
        public List<ClaudeMessage> getMessages() { return messages; }

        /** @return final result envelope, or {@code null} */
        public ClaudeResult getResult() { return result; }

        /** @return raw CLI result */
        public ClaudeCodeCliResult getRawResult() { return rawResult; }

        /**
         * @return the textual {@code result} payload, or an empty string
         *         when no result envelope is present
         */
        public String getTextContent() {
            return result != null ? result.getResult() : "";
        }

        /** @return the USD cost reported in the result envelope, or {@code null} */
        public Double getTotalCostUsd() {
            return result != null ? result.getTotalCostUsd() : null;
        }
    }

    /**
     * Release any resources held by this client. Currently a no-op
     * because the underlying executor is stateless.
     */
    @Override
    public void close() {
    }
}
