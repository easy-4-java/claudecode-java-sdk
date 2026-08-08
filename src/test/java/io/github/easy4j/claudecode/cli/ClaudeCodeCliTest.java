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

import io.github.easy4j.claudecode.ClaudeCodeClientConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClaudeCodeCli} and its nested {@link ClaudeCodeCli.PrintOptions}.
 *
 * @since 3.0.0
 */
class ClaudeCodeCliTest {

    private FakeClaudeCodeCliExecutor exec;
    private ClaudeCodeCli cli;

    @BeforeEach
    void setUp() {
        ClaudeCodeClientConfig config = new ClaudeCodeClientConfig();
        exec = new FakeClaudeCodeCliExecutor(config);
        cli = new ClaudeCodeCli(exec);
    }

    private void assertArgs(String... expected) {
        assertEquals(1, exec.calls.size(), "expected exactly one execute() call");
        assertArrayEquals(expected, exec.calls.get(0));
    }

    // ------------------------------------------------------------
    // accessors
    // ------------------------------------------------------------

    @Test
    void shouldExposeExecutor() {
        assertSame(exec, cli.executor());
    }

    // ------------------------------------------------------------
    // global
    // ------------------------------------------------------------

    @Test
    void shouldIssueVersion() {
        cli.version();
        assertArgs("--version");
    }

    @Test
    void shouldIssueHelp() {
        cli.help();
        assertArgs("--help");
    }

    // ------------------------------------------------------------
    // print / stream-json / json / schema
    // ------------------------------------------------------------

    @Test
    void shouldIssuePrintPrompt() {
        cli.print("hello");
        assertArgs("-p", "hello");
    }

    @Test
    void shouldIssuePrintWithModel() {
        cli.print("hello", "sonnet");
        assertArgs("-p", "--model", "sonnet", "hello");
    }

    @Test
    void shouldIssueStreamJsonPrint() {
        cli.printStreamJson("hi");
        assertArgs("-p", "--output-format", "stream-json", "--include-partial-messages", "hi");
    }

    @Test
    void shouldIssueJsonPrint() {
        cli.printJson("hi");
        assertArgs("-p", "--output-format", "json", "hi");
    }

    @Test
    void shouldIssueBidirectionalStreamJson() {
        cli.printStreamJsonBidirectional();
        assertArgs("-p", "--output-format", "stream-json",
                "--input-format", "stream-json",
                "--replay-user-messages", "--include-partial-messages");
    }

    @Test
    void shouldIssuePrintWithSchema() {
        cli.printWithSchema("hi", "schema");
        assertArgs("-p", "--json-schema", "schema", "hi");
    }

    @Test
    void shouldDelegatePrintOptionsToArgs() {
        cli.print(new ClaudeCodeCli.PrintOptions("hi").model("opus"));
        assertArgs("--model", "opus", "--output-format", "stream-json",
                "--include-partial-messages", "-p", "hi");
    }

    // ------------------------------------------------------------
    // session lifecycle
    // ------------------------------------------------------------

    @Test
    void shouldIssueContinue() {
        cli.continue_();
        assertArgs("-c");
    }

    @Test
    void shouldIssueContinueWithPrompt() {
        cli.continue_("hi");
        assertArgs("-c", "-p", "hi");
    }

    @Test
    void shouldIssueContinueWithPromptAndModel() {
        cli.continue_("hi", "opus");
        assertArgs("-c", "--model", "opus", "-p", "hi");
    }

    @Test
    void shouldIssueResumePicker() {
        cli.resume();
        assertArgs("-r");
    }

    @Test
    void shouldIssueResumeById() {
        cli.resume("sess-1");
        assertArgs("-r", "sess-1");
    }

    @Test
    void shouldIssueResumeWithPrompt() {
        cli.resume("sess-1", "hi");
        assertArgs("-r", "sess-1", "-p", "hi");
    }

    @Test
    void shouldIssueResumeWithModelAndPrompt() {
        cli.resume("sess-1", "hi", "opus");
        assertArgs("-r", "sess-1", "--model", "opus", "-p", "hi");
    }

    @Test
    void shouldIssueContinueFork() {
        cli.continueForkSession();
        assertArgs("-c", "--fork-session");
    }

    @Test
    void shouldIssueResumeFork() {
        cli.resumeForkSession("sess-1");
        assertArgs("-r", "sess-1", "--fork-session");
    }

    @Test
    void shouldIssueResumeForkWithPrompt() {
        cli.resumeForkSession("sess-1", "hi");
        assertArgs("-r", "sess-1", "--fork-session", "-p", "hi");
    }

    // ------------------------------------------------------------
    // session id / PR / name
    // ------------------------------------------------------------

    @Test
    void shouldIssueWithSessionId() {
        cli.withSessionId("u-1", "hi");
        assertArgs("--session-id", "u-1", "-p", "hi");
    }

    @Test
    void shouldIssueFromPrByNumber() {
        cli.fromPr("42");
        assertArgs("--from-pr", "42");
    }

    @Test
    void shouldIssueFromPrPicker() {
        cli.fromPr();
        assertArgs("--from-pr");
    }

    @Test
    void shouldIssueNamedSession() {
        cli.namedSession("name", "hi");
        assertArgs("-n", "name", "-p", "hi");
    }

    @Test
    void shouldIssueNoPersistence() {
        cli.printNoPersistence("hi");
        assertArgs("--no-session-persistence", "-p", "hi");
    }

    // ------------------------------------------------------------
    // permissions
    // ------------------------------------------------------------

    @Test
    void shouldIssuePrintWithPermission() {
        cli.printWithPermission("hi", "auto");
        assertArgs("--permission-mode", "auto", "-p", "hi");
    }

    @Test
    void shouldIssueBypassPermissions() {
        cli.printBypassPermissions("hi");
        assertArgs("--dangerously-skip-permissions", "-p", "hi");
    }

    // ------------------------------------------------------------
    // worktree / add-dir
    // ------------------------------------------------------------

    @Test
    void shouldIssueWorktreePrint() {
        cli.printInWorktree("hi");
        assertArgs("-w", "-p", "hi");
    }

    @Test
    void shouldIssueNamedWorktreePrint() {
        cli.printInWorktree("wt", "hi");
        assertArgs("-w", "wt", "-p", "hi");
    }

    @Test
    void shouldIssueAddDir() {
        cli.printWithDir("/tmp", "hi");
        assertArgs("--add-dir", "/tmp", "-p", "hi");
    }

    // ------------------------------------------------------------
    // effort / budget
    // ------------------------------------------------------------

    @Test
    void shouldIssueEffort() {
        cli.printWithEffort("hi", "high");
        assertArgs("--effort", "high", "-p", "hi");
    }

    @Test
    void shouldIssueBudget() {
        cli.printWithBudget("hi", 1.5);
        assertArgs("--max-budget-usd", "1.5", "-p", "hi");
    }

    // ------------------------------------------------------------
    // system prompt
    // ------------------------------------------------------------

    @Test
    void shouldIssueSystemPrompt() {
        cli.printWithSystemPrompt("u", "s");
        assertArgs("--system-prompt", "s", "-p", "u");
    }

    @Test
    void shouldIssueAppendSystemPrompt() {
        cli.printWithAppendSystemPrompt("u", "a");
        assertArgs("--append-system-prompt", "a", "-p", "u");
    }

    // ------------------------------------------------------------
    // agents / tools / mcp
    // ------------------------------------------------------------

    @Test
    void shouldIssueAgents() {
        cli.printWithAgents("hi", "{}");
        assertArgs("--agents", "{}", "-p", "hi");
    }

    @Test
    void shouldIssueAllowedTools() {
        cli.printWithAllowedTools("hi", "t1");
        assertArgs("--allowedTools", "t1", "-p", "hi");
    }

    @Test
    void shouldIssueMcpConfig() {
        cli.printWithMcpConfig("hi", "m");
        assertArgs("--mcp-config", "m", "-p", "hi");
    }

    // ------------------------------------------------------------
    // subcommands
    // ------------------------------------------------------------

    @Test
    void shouldIssueAgentsList() {
        cli.agentsList();
        assertArgs("agents", "--json");
    }

    @Test
    void shouldIssueAgentsWithArgs() {
        cli.agents("a", "b");
        assertArgs("agents", "a", "b");
    }

    @Test
    void shouldIssueAuthLogin() {
        cli.authLogin();
        assertArgs("auth", "login");
    }

    @Test
    void shouldIssueAuthLogout() {
        cli.authLogout();
        assertArgs("auth", "logout");
    }

    @Test
    void shouldIssueAuthStatus() {
        cli.authStatus();
        assertArgs("auth", "status");
    }

    @Test
    void shouldIssueDoctor() {
        cli.doctor();
        assertArgs("doctor");
    }

    @Test
    void shouldIssueInstall() {
        cli.install();
        assertArgs("install");
    }

    @Test
    void shouldIssueInstallWithTarget() {
        cli.install("/opt");
        assertArgs("install", "/opt");
    }

    @Test
    void shouldIssueMcp() {
        cli.mcp("a", "b");
        assertArgs("mcp", "a", "b");
    }

    @Test
    void shouldIssueMcpList() {
        cli.mcpList();
        assertArgs("mcp", "list");
    }

    @Test
    void shouldIssueMcpAddWithoutArgs() {
        cli.mcpAdd("name", "url");
        assertArgs("mcp", "add", "name", "url");
    }

    @Test
    void shouldIssueMcpAddWithArgs() {
        cli.mcpAdd("name", "url", "x", "y");
        assertArgs("mcp", "add", "name", "url", "x", "y");
    }

    @Test
    void shouldIssueMcpGet() {
        cli.mcpGet("name");
        assertArgs("mcp", "get", "name");
    }

    @Test
    void shouldIssueMcpRemove() {
        cli.mcpRemove("name");
        assertArgs("mcp", "remove", "name");
    }

    @Test
    void shouldIssueMcpServe() {
        cli.mcpServe();
        assertArgs("mcp", "serve");
    }

    @Test
    void shouldIssuePluginList() {
        cli.pluginList();
        assertArgs("plugin", "list");
    }

    @Test
    void shouldIssuePluginInstall() {
        cli.pluginInstall("name");
        assertArgs("plugin", "install", "name");
    }

    @Test
    void shouldIssuePluginWithArgs() {
        cli.plugin("a", "b");
        assertArgs("plugin", "a", "b");
    }

    @Test
    void shouldIssueProjectPurge() {
        cli.projectPurge();
        assertArgs("project", "purge");
    }

    @Test
    void shouldIssueSetupToken() {
        cli.setupToken();
        assertArgs("setup-token");
    }

    @Test
    void shouldIssueUpdate() {
        cli.update();
        assertArgs("update");
    }

    @Test
    void shouldIssueUltrareview() {
        cli.ultrareview();
        assertArgs("ultrareview");
    }

    @Test
    void shouldIssueUltrareviewWithTarget() {
        cli.ultrareview("src", 10);
        assertArgs("ultrareview", "src", "--timeout", "10");
    }

    @Test
    void shouldIssueBarePrint() {
        cli.barePrint("hi");
        assertArgs("--bare", "-p", "hi");
    }

    @Test
    void shouldIssueBriefPrint() {
        cli.briefPrint("hi");
        assertArgs("--brief", "-p", "hi");
    }

    @Test
    void shouldIssueDebugPrint() {
        cli.debugPrint("hi");
        assertArgs("--debug", "-p", "hi");
    }

    @Test
    void shouldIssueVerbosePrint() {
        cli.verbosePrint("hi");
        assertArgs("--verbose", "-p", "hi");
    }

    @Test
    void shouldIssueIdePrint() {
        cli.idePrint("hi");
        assertArgs("--ide", "-p", "hi");
    }

    @Test
    void shouldIssueAgent() {
        cli.printWithAgent("hi", "a");
        assertArgs("--agent", "a", "-p", "hi");
    }

    @Test
    void shouldIssueFallbackModel() {
        cli.printWithFallbackModel("hi", "opus", "sonnet");
        assertArgs("--model", "opus", "--fallback-model", "sonnet", "-p", "hi");
    }

    @Test
    void shouldIssueDisallowedTools() {
        cli.printWithDisallowedTools("hi", "t");
        assertArgs("--disallowedTools", "t", "-p", "hi");
    }

    @Test
    void shouldIssueTools() {
        cli.printWithTools("hi", "t");
        assertArgs("--tools", "t", "-p", "hi");
    }

    @Test
    void shouldIssueSystemPromptFile() {
        cli.printWithSystemPromptFile("u", "f");
        assertArgs("--system-prompt-file", "f", "-p", "u");
    }

    @Test
    void shouldIssueAppendSystemPromptFile() {
        cli.printWithAppendSystemPromptFile("u", "f");
        assertArgs("--append-system-prompt-file", "f", "-p", "u");
    }

    @Test
    void shouldIssueStrictMcpConfig() {
        cli.printWithStrictMcpConfig("hi", "m");
        assertArgs("--mcp-config", "m", "--strict-mcp-config", "-p", "hi");
    }

    @Test
    void shouldIssueSettings() {
        cli.printWithSettings("hi", "s");
        assertArgs("--settings", "s", "-p", "hi");
    }

    @Test
    void shouldIssueSettingSources() {
        cli.printWithSettingSources("hi", "user");
        assertArgs("--setting-sources", "user", "-p", "hi");
    }

    @Test
    void shouldIssuePluginDir() {
        cli.printWithPluginDir("hi", "a", "b");
        assertArgs("--plugin-dir", "a", "--plugin-dir", "b", "-p", "hi");
    }

    @Test
    void shouldIssuePluginUrl() {
        cli.printWithPluginUrl("hi", "u1", "u2");
        assertArgs("--plugin-url", "u1", "--plugin-url", "u2", "-p", "hi");
    }

    @Test
    void shouldIssueFiles() {
        cli.printWithFiles("hi", "spec");
        assertArgs("--file", "spec", "-p", "hi");
    }

    @Test
    void shouldIssueTmux() {
        cli.printWithTmux("hi");
        assertArgs("--tmux", "-w", "-p", "hi");
    }

    @Test
    void shouldIssueClassicTmux() {
        cli.printWithClassicTmux("hi");
        assertArgs("--tmux=classic", "-w", "-p", "hi");
    }

    @Test
    void shouldIssueRemoteControlByName() {
        cli.remoteControl("name");
        assertArgs("--remote-control", "name");
    }

    @Test
    void shouldIssueRemoteControl() {
        cli.remoteControl();
        assertArgs("--remote-control");
    }

    @Test
    void shouldIssueRemoteControlWithPrefix() {
        cli.remoteControlWithPrefix("pre");
        assertArgs("--remote-control", "--remote-control-session-name-prefix", "pre");
    }

    @Test
    void shouldIssueAllowBypassPermissions() {
        cli.printAllowBypassPermissions("hi");
        assertArgs("--allow-dangerously-skip-permissions", "-p", "hi");
    }

    @Test
    void shouldIssueDisableSlashCommands() {
        cli.printDisableSlashCommands("hi");
        assertArgs("--disable-slash-commands", "-p", "hi");
    }

    @Test
    void shouldIssueExcludeDynamicSections() {
        cli.printExcludeDynamicSections("hi");
        assertArgs("--exclude-dynamic-system-prompt-sections", "-p", "hi");
    }

    @Test
    void shouldIssueHookEvents() {
        cli.printWithHookEvents("hi");
        assertArgs("--include-hook-events", "--output-format", "stream-json", "-p", "hi");
    }

    @Test
    void shouldIssueNoChrome() {
        cli.printNoChrome("hi");
        assertArgs("--no-chrome", "-p", "hi");
    }

    @Test
    void shouldIssueDebugFilter() {
        cli.printWithDebugFilter("hi", "api");
        assertArgs("--debug", "api", "-p", "hi");
    }

    @Test
    void shouldIssueDebugFile() {
        cli.printWithDebugFile("hi", "/tmp/dbg");
        assertArgs("--debug-file", "/tmp/dbg", "-p", "hi");
    }

    @Test
    void shouldIssueMcpDebug() {
        cli.printWithMcpDebug("hi");
        assertArgs("--mcp-debug", "-p", "hi");
    }

    @Test
    void shouldIssueAutoMode() {
        cli.autoMode();
        assertArgs("auto-mode");
    }

    // ============================================================
    // PrintOptions.toArgs() — exhaustive coverage
    // ============================================================

    @Test
    void printOptionsShouldBeEmptyWithPromptOnly() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi");
        String[] args = opts.toArgs();
        // PrintOptions defaults outputFormat="stream-json" and includePartialMessages=true
        assertContains(args, "--output-format", "stream-json", "--include-partial-messages", "-p", "hi");
    }

    @Test
    void printOptionsShouldIncludePromptEvenWhenNull() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions(null);
        String[] args = opts.toArgs();
        // PrintOptions defaults outputFormat="stream-json" and includePartialMessages=true;
        // prompt is null so only "-p" is appended without a value.
        List<String> list = Arrays.asList(args);
        assertTrue(list.contains("-p"));
        assertTrue(list.contains("--output-format"));
        assertTrue(list.contains("stream-json"));
        assertTrue(list.contains("--include-partial-messages"));
    }

    @Test
    void printOptionsShouldEmitModelAndFallback() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .model("opus")
                .fallbackModel("sonnet");
        assertContains(opts.toArgs(), "--model", "opus", "--fallback-model", "sonnet", "-p", "hi");
    }

    @Test
    void printOptionsShouldRespectOutputFormatAndPartialMessages() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .outputFormat("json")
                .includePartialMessages(true)
                .replayUserMessages(true);
        assertContains(opts.toArgs(),
                "--output-format", "json",
                "--include-partial-messages",
                "--replay-user-messages",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldOmitIncludePartialMessagesWhenFalse() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .includePartialMessages(false);
        List<String> args = Arrays.asList(opts.toArgs());
        assertFalse(args.contains("--include-partial-messages"));
    }

    @Test
    void printOptionsShouldEmitInputFormat() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi").inputFormat("stream-json");
        assertContains(opts.toArgs(), "--input-format", "stream-json", "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitPermissionEffortAndBudget() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .permissionMode("auto")
                .effort("high")
                .maxBudgetUsd(1.25);
        assertContains(opts.toArgs(),
                "--permission-mode", "auto",
                "--effort", "high",
                "--max-budget-usd", "1.25",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitSystemPrompts() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .systemPrompt("s")
                .systemPromptFile("sf")
                .appendSystemPrompt("a")
                .appendSystemPromptFile("af");
        assertContains(opts.toArgs(),
                "--system-prompt", "s",
                "--system-prompt-file", "sf",
                "--append-system-prompt", "a",
                "--append-system-prompt-file", "af",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitAgentsAndTools() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .agent("a")
                .agents("{}")
                .allowedTools("a,b")
                .disallowedTools("x")
                .tools("default");
        assertContains(opts.toArgs(),
                "--agent", "a",
                "--agents", "{}",
                "--allowedTools", "a,b",
                "--disallowedTools", "x",
                "--tools", "default",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitMcpFlags() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .mcpConfig("m")
                .strictMcpConfig(true);
        assertContains(opts.toArgs(), "--mcp-config", "m", "--strict-mcp-config", "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitAddDirAndFiles() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .addDir("/tmp")
                .fileResources("spec");
        assertContains(opts.toArgs(), "--add-dir", "/tmp", "--file", "spec", "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitSessionFlags() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .sessionId("u-1")
                .resumeSessionId("s-1")
                .continueSession(true)
                .forkSession(true)
                .fromPr("42")
                .sessionName("name")
                .noSessionPersistence(true);
        assertContains(opts.toArgs(),
                "--session-id", "u-1",
                "-r", "s-1",
                "-c",
                "--fork-session",
                "--from-pr", "42",
                "-n", "name",
                "--no-session-persistence",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitWorktree() {
        ClaudeCodeCli.PrintOptions only = new ClaudeCodeCli.PrintOptions("hi").worktree(true);
        assertContains(only.toArgs(), "-w", "-p", "hi");

        ClaudeCodeCli.PrintOptions named = new ClaudeCodeCli.PrintOptions("hi").worktreeName("wt");
        assertContains(named.toArgs(), "-w", "wt", "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitTmux() {
        assertContains(new ClaudeCodeCli.PrintOptions("hi").tmux("classic").toArgs(),
                "--tmux", "-p", "hi");
        assertContains(new ClaudeCodeCli.PrintOptions("hi").tmux("true").toArgs(),
                "--tmux", "-p", "hi");
        assertContains(new ClaudeCodeCli.PrintOptions("hi").tmux("custom").toArgs(),
                "--tmux", "custom", "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitModeFlags() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .bare(true)
                .brief(true)
                .debug(true)
                .verbose(true)
                .ide(true)
                .chrome(true)
                .noChrome(true);
        assertContains(opts.toArgs(),
                "--bare", "--brief", "--debug", "--verbose", "--ide", "--chrome", "--no-chrome",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldPreferDebugFilterOverDebugFlag() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .debug(true)
                .debugFilter("api");
        assertContains(opts.toArgs(), "--debug", "api", "-p", "hi");
        List<String> args = Arrays.asList(opts.toArgs());
        // When debugFilter is set, --debug appears once (followed by the filter value);
        // the else-if branch for standalone --debug is skipped.
        long debugCount = args.stream()
                .filter("--debug"::equals)
                .count();
        assertEquals(1L, debugCount);
        // Verify the filter value immediately follows --debug
        int debugIdx = args.indexOf("--debug");
        assertEquals("api", args.get(debugIdx + 1));
    }

    @Test
    void printOptionsShouldEmitDebugFile() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi").debugFile("/tmp/dbg");
        assertContains(opts.toArgs(), "--debug-file", "/tmp/dbg", "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitSafetyFlags() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .dangerouslySkipPermissions(true)
                .allowDangerouslySkipPermissions(true)
                .disableSlashCommands(true)
                .excludeDynamicSystemPromptSections(true)
                .includeHookEvents(true)
                .mcpDebug(true);
        assertContains(opts.toArgs(),
                "--dangerously-skip-permissions",
                "--allow-dangerously-skip-permissions",
                "--disable-slash-commands",
                "--exclude-dynamic-system-prompt-sections",
                "--include-hook-events",
                "--mcp-debug",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitBetasSettingsAndSources() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .betas("beta")
                .settings("s")
                .settingSources("user");
        assertContains(opts.toArgs(),
                "--betas", "beta",
                "--settings", "s",
                "--setting-sources", "user",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitPluginArrays() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .pluginDir("a", "b")
                .pluginUrl("u1");
        assertContains(opts.toArgs(),
                "--plugin-dir", "a",
                "--plugin-dir", "b",
                "--plugin-url", "u1",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitRemoteControl() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi")
                .remoteControl("rc")
                .remoteControlSessionNamePrefix("pre");
        assertContains(opts.toArgs(),
                "--remote-control", "rc",
                "--remote-control-session-name-prefix", "pre",
                "-p", "hi");
    }

    @Test
    void printOptionsShouldEmitJsonSchema() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi").jsonSchema("schema");
        assertContains(opts.toArgs(), "--json-schema", "schema", "-p", "hi");
    }

    @Test
    void printOptionsShouldAlwaysAppendPromptAtEnd() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hello")
                .model("opus");
        String[] args = opts.toArgs();
        assertTrue(args[args.length - 2].equals("-p"));
        assertEquals("hello", args[args.length - 1]);
    }

    private static void assertContains(String[] haystack, String... needle) {
        List<String> list = Arrays.asList(haystack);
        int idx = 0;
        for (String n : needle) {
            int found = list.subList(idx, list.size()).indexOf(n);
            assertTrue(found >= 0, "expected " + n + " at or after index " + idx + " in " + list);
            idx = idx + found + 1;
        }
    }
}
