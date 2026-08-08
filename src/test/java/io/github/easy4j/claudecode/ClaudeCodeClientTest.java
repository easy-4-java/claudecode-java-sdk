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

import io.github.easy4j.claudecode.cli.ClaudeCodeCli;
import io.github.easy4j.claudecode.model.ClaudeAgent;
import io.github.easy4j.claudecode.model.ClaudeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClaudeCodeClient}.
 *
 * @since 3.0.0
 */
class ClaudeCodeClientTest {

    private ClaudeCodeClientConfig config;
    private FakeClaudeCodeCliExecutor exec;
    private ClaudeCodeCli cli;
    private ClaudeCodeClient client;

    @BeforeEach
    void setUp() {
        config = new ClaudeCodeClientConfig();
        config.setLocalExecutable("claude");
        exec = new FakeClaudeCodeCliExecutor(config);
        cli = new ClaudeCodeCli(exec);
        client = new ClaudeCodeClient(config, cli);
    }

    private String[] lastCall() {
        return exec.calls.get(exec.calls.size() - 1);
    }

    // ------------------------------------------------------------
    // construction
    // ------------------------------------------------------------

    @Test
    void shouldRejectNullConfigInDefaultConstructor() {
        assertThrows(NullPointerException.class, () -> new ClaudeCodeClient(null));
    }

    @Test
    void shouldRejectNullConfigInTwoArgConstructor() {
        assertThrows(NullPointerException.class, () -> new ClaudeCodeClient(null, cli));
    }

    @Test
    void shouldRejectNullCliInTwoArgConstructor() {
        assertThrows(NullPointerException.class, () -> new ClaudeCodeClient(config, null));
    }

    @Test
    void shouldExposeConfigAndCli() {
        assertSame(config, client.getConfig());
        assertSame(cli, client.cli());
    }

    @Test
    void shouldCloseWithoutError() throws Exception {
        client.close();
    }

    @Test
    void shouldBeUsableInsideTryWithResources() throws Exception {
        ClaudeCodeClientConfig cfg = new ClaudeCodeClientConfig();
        cfg.setLocalExecutable("java");
        try (ClaudeCodeClient c = new ClaudeCodeClient(cfg)) {
            // No-op; just ensures close() is invoked.
            c.close();
        }
    }

    // ------------------------------------------------------------
    // delegation
    // ------------------------------------------------------------

    @Test
    void shouldDelegateVersionAndHelp() {
        client.version();
        assertEquals("--version", lastCall()[0]);
        client.help();
        assertEquals("--help", lastCall()[0]);
    }

    @Test
    void shouldForwardPrintPrompt() {
        client.print("hello");
        assertEquals("-p", lastCall()[0]);
        assertEquals("hello", lastCall()[1]);
    }

    @Test
    void shouldForwardPrintWithModel() {
        client.print("hello", "opus");
        assertEquals("opus", lastCall()[2]);
    }

    @Test
    void shouldForwardPrintWithOptions() {
        ClaudeCodeCli.PrintOptions opts = new ClaudeCodeCli.PrintOptions("hi").model("opus");
        client.print(opts);
        assertEquals("opus", lastCall()[0]);
        // Final two should be -p hi
        assertEquals("-p", lastCall()[lastCall().length - 2]);
        assertEquals("hi", lastCall()[lastCall().length - 1]);
    }

    @Test
    void shouldPrintStreamJsonParseMessages() {
        exec.result = new ClaudeCodeCliResult(0,
                "{\"type\":\"assistant\",\"message\":\"hi\"}\n" +
                        "{\"type\":\"result\",\"result\":\"done\",\"session_id\":\"s\"}\n",
                "");

        List<ClaudeMessage> messages = client.printStreamJson("hi");

        assertEquals(2, messages.size());
        assertEquals("assistant", messages.get(0).getType());
        assertEquals("result", messages.get(1).getType());
    }

    @Test
    void shouldIgnoreUnparseableLinesDuringStreamJsonParsing() {
        exec.result = new ClaudeCodeCliResult(0,
                "not-json\n" +
                        "{\"type\":\"assistant\",\"message\":\"hi\"}\n" +
                        "garbage\n",
                "");

        List<ClaudeMessage> messages = client.printStreamJson("hi");

        assertEquals(1, messages.size());
        assertEquals("assistant", messages.get(0).getType());
    }

    @Test
    void shouldHandleEmptyStreamJson() {
        exec.result = new ClaudeCodeCliResult(0, "", "");

        List<ClaudeMessage> messages = client.printStreamJson("hi");

        assertTrue(messages.isEmpty());
    }

    @Test
    void shouldHandleNullStreamJson() {
        exec.result = new ClaudeCodeCliResult(0, null, "");

        List<ClaudeMessage> messages = client.printStreamJson("hi");

        assertTrue(messages.isEmpty());
    }

    @Test
    void shouldPrintStreamJsonAndParseReturnFinalResult() {
        exec.result = new ClaudeCodeCliResult(0,
                "{\"type\":\"assistant\",\"message\":\"hi\"}\n" +
                        "{\"type\":\"result\",\"result\":\"final\",\"session_id\":\"s\"," +
                        "\"total_cost_usd\":0.05,\"usage\":{\"input_tokens\":1,\"output_tokens\":2," +
                        "\"cache_creation_tokens\":3,\"cache_read_tokens\":4}}\n",
                "");

        ClaudeCodeClient.StreamResult sr = client.printStreamJsonAndParse("hi");

        assertEquals(2, sr.getMessages().size());
        assertNotNull(sr.getResult());
        assertEquals("final", sr.getResult().getResult());
        assertEquals("final", sr.getTextContent());
        assertEquals(0.05, sr.getTotalCostUsd(), 0.0001);
    }

    @Test
    void shouldPrintStreamJsonAndParseHandleMissingResultEnvelope() {
        exec.result = new ClaudeCodeCliResult(0,
                "{\"type\":\"assistant\",\"message\":\"hi\"}\n",
                "");

        ClaudeCodeClient.StreamResult sr = client.printStreamJsonAndParse("hi");

        assertEquals(1, sr.getMessages().size());
        assertNull(sr.getResult());
        assertEquals("", sr.getTextContent());
        assertNull(sr.getTotalCostUsd());
    }

    @Test
    void shouldPrintStreamJsonAndParseFindLastResultEnvelope() {
        exec.result = new ClaudeCodeCliResult(0,
                "{\"type\":\"result\",\"result\":\"first\"}\n" +
                        "{\"type\":\"assistant\",\"message\":\"mid\"}\n" +
                        "{\"type\":\"result\",\"result\":\"last\"}\n",
                "");

        ClaudeCodeClient.StreamResult sr = client.printStreamJsonAndParse("hi");

        assertEquals("last", sr.getResult().getResult());
    }

    @Test
    void shouldPrintBidirectionalConfigureOptions() {
        client.printBidirectional("hi");
        String[] args = lastCall();
        assertEquals("-p", args[args.length - 2]);
        assertEquals("hi", args[args.length - 1]);
        // Check key flags are emitted
        assertTrue(java.util.Arrays.asList(args).contains("--output-format"));
        assertTrue(java.util.Arrays.asList(args).contains("stream-json"));
        assertTrue(java.util.Arrays.asList(args).contains("--input-format"));
        assertTrue(java.util.Arrays.asList(args).contains("--replay-user-messages"));
        assertTrue(java.util.Arrays.asList(args).contains("--include-partial-messages"));
    }

    @Test
    void shouldPrintJson() {
        client.printJson("hi");
        assertEquals("json", lastCall()[2]);
    }

    @Test
    void shouldPrintWithSchema() {
        client.printWithSchema("hi", "schema");
        assertEquals("--json-schema", lastCall()[1]);
        assertEquals("schema", lastCall()[2]);
    }

    // ------------------------------------------------------------
    // session lifecycle
    // ------------------------------------------------------------

    @Test
    void shouldForwardContinueAndResumeMethods() {
        client.continueSession();
        assertEquals("-c", lastCall()[0]);

        client.continueSession("hi");
        assertEquals("hi", lastCall()[2]);

        client.continueSession("hi", "opus");
        assertEquals("opus", lastCall()[2]);

        client.resumeSession();
        assertEquals("-r", lastCall()[0]);

        client.resumeSession("s");
        assertEquals("s", lastCall()[1]);

        client.resumeSession("s", "hi");
        assertEquals("hi", lastCall()[3]);

        client.resumeSession("s", "hi", "opus");
        assertEquals("opus", lastCall()[2]);

        client.continueForkSession();
        assertEquals("--fork-session", lastCall()[1]);

        client.resumeForkSession("s");
        assertEquals("--fork-session", lastCall()[2]);

        client.resumeForkSession("s", "hi");
        assertEquals("hi", lastCall()[4]);

        client.withSessionId("u", "hi");
        assertEquals("u", lastCall()[1]);

        client.fromPr("42");
        assertEquals("42", lastCall()[1]);

        client.fromPr();
        assertEquals("--from-pr", lastCall()[0]);

        client.namedSession("n", "hi");
        assertEquals("n", lastCall()[1]);

        client.printNoPersistence("hi");
        assertEquals("--no-session-persistence", lastCall()[0]);
    }

    // ------------------------------------------------------------
    // permissions / worktree / effort / budget
    // ------------------------------------------------------------

    @Test
    void shouldForwardPermissionMethods() {
        client.printWithPermission("hi", "auto");
        assertEquals("auto", lastCall()[1]);

        client.printBypassPermissions("hi");
        assertEquals("--dangerously-skip-permissions", lastCall()[0]);
    }

    @Test
    void shouldForwardWorktreeMethods() {
        client.printInWorktree("hi");
        assertEquals("-w", lastCall()[0]);

        client.printInWorktree("wt", "hi");
        assertEquals("wt", lastCall()[1]);

        client.printWithDir("/tmp", "hi");
        assertEquals("/tmp", lastCall()[1]);
    }

    @Test
    void shouldForwardEffortAndBudget() {
        client.printWithEffort("hi", "high");
        assertEquals("high", lastCall()[1]);

        client.printWithBudget("hi", 1.25);
        assertEquals("1.25", lastCall()[1]);
    }

    // ------------------------------------------------------------
    // system prompt
    // ------------------------------------------------------------

    @Test
    void shouldForwardSystemPromptMethods() {
        client.printWithSystemPrompt("u", "s");
        assertEquals("s", lastCall()[1]);

        client.printWithAppendSystemPrompt("u", "a");
        assertEquals("a", lastCall()[1]);

        client.printWithSystemPromptFile("u", "f");
        assertEquals("f", lastCall()[1]);

        client.printWithAppendSystemPromptFile("u", "f");
        assertEquals("f", lastCall()[1]);
    }

    // ------------------------------------------------------------
    // agents / tools / mcp / plugins
    // ------------------------------------------------------------

    @Test
    void shouldForwardAgentsToolsMcpPlugins() {
        client.printWithAgent("hi", "a");
        assertEquals("a", lastCall()[1]);

        client.printWithAgents("hi", "{}");
        assertEquals("{}", lastCall()[1]);

        client.printWithTools("hi", "t");
        assertEquals("t", lastCall()[1]);

        client.printWithAllowedTools("hi", "t");
        assertEquals("t", lastCall()[1]);

        client.printWithDisallowedTools("hi", "t");
        assertEquals("t", lastCall()[1]);

        client.printWithFallbackModel("hi", "opus", "sonnet");
        assertEquals("opus", lastCall()[1]);
        assertEquals("sonnet", lastCall()[3]);

        client.printWithMcpConfig("hi", "m");
        assertEquals("m", lastCall()[1]);

        client.printWithStrictMcpConfig("hi", "m");
        assertEquals("--strict-mcp-config", lastCall()[3]);

        client.printWithSettings("hi", "s");
        assertEquals("s", lastCall()[1]);

        client.printWithSettingSources("hi", "user");
        assertEquals("user", lastCall()[1]);

        client.printWithPluginDir("hi", "/p1");
        assertEquals("--plugin-dir", lastCall()[0]);
        assertEquals("/p1", lastCall()[1]);

        client.printWithPluginUrl("hi", "https://x");
        assertEquals("--plugin-url", lastCall()[0]);

        client.printWithFiles("hi", "spec");
        assertEquals("spec", lastCall()[1]);
    }

    // ------------------------------------------------------------
    // tmux / remote / debug
    // ------------------------------------------------------------

    @Test
    void shouldForwardTmuxAndRemoteAndDebugMethods() {
        client.printWithTmux("hi");
        assertEquals("--tmux", lastCall()[0]);

        client.printWithClassicTmux("hi");
        assertEquals("--tmux=classic", lastCall()[0]);

        client.remoteControl("name");
        assertEquals("name", lastCall()[1]);

        client.remoteControl();
        assertEquals("--remote-control", lastCall()[0]);

        client.printNoChrome("hi");
        assertEquals("--no-chrome", lastCall()[0]);

        client.printAllowBypassPermissions("hi");
        assertEquals("--allow-dangerously-skip-permissions", lastCall()[0]);

        client.printDisableSlashCommands("hi");
        assertEquals("--disable-slash-commands", lastCall()[0]);

        client.printExcludeDynamicSections("hi");
        assertEquals("--exclude-dynamic-system-prompt-sections", lastCall()[0]);

        client.printWithHookEvents("hi");
        assertEquals("--include-hook-events", lastCall()[0]);

        client.printWithDebugFilter("hi", "api");
        assertEquals("api", lastCall()[1]);

        client.printWithDebugFile("hi", "/tmp/dbg");
        assertEquals("/tmp/dbg", lastCall()[1]);

        client.printWithMcpDebug("hi");
        assertEquals("--mcp-debug", lastCall()[0]);
    }

    // ------------------------------------------------------------
    // subcommands
    // ------------------------------------------------------------

    @Test
    void shouldForwardSubcommands() {
        client.agentsList();
        assertEquals("agents", lastCall()[0]);

        client.agents("a", "b");
        assertEquals("agents", lastCall()[0]);

        client.authLogin();
        assertEquals("login", lastCall()[1]);

        client.authLogout();
        assertEquals("logout", lastCall()[1]);

        client.authStatus();
        assertEquals("status", lastCall()[1]);

        client.doctor();
        assertEquals("doctor", lastCall()[0]);

        client.install();
        assertEquals("install", lastCall()[0]);

        client.install("/opt");
        assertEquals("/opt", lastCall()[1]);

        client.mcpList();
        assertEquals("list", lastCall()[1]);

        client.mcpAdd("n", "c", "x");
        assertEquals("x", lastCall()[4]);

        client.mcpGet("n");
        assertEquals("n", lastCall()[2]);

        client.mcpRemove("n");
        assertEquals("n", lastCall()[2]);

        client.mcpServe();
        assertEquals("serve", lastCall()[1]);

        client.mcp("a");
        assertEquals("a", lastCall()[1]);

        client.pluginList();
        assertEquals("list", lastCall()[1]);

        client.pluginInstall("p");
        assertEquals("p", lastCall()[2]);

        client.plugin("a");
        assertEquals("a", lastCall()[1]);

        client.projectPurge();
        assertEquals("purge", lastCall()[1]);

        client.setupToken();
        assertEquals("setup-token", lastCall()[0]);

        client.update();
        assertEquals("update", lastCall()[0]);

        client.ultrareview();
        assertEquals("ultrareview", lastCall()[0]);

        client.ultrareview("src", 10);
        assertEquals("10", lastCall()[3]);

        client.autoMode();
        assertEquals("auto-mode", lastCall()[0]);

        client.barePrint("hi");
        assertEquals("--bare", lastCall()[0]);

        client.briefPrint("hi");
        assertEquals("--brief", lastCall()[0]);

        client.debugPrint("hi");
        assertEquals("--debug", lastCall()[0]);

        client.verbosePrint("hi");
        assertEquals("--verbose", lastCall()[0]);

        client.idePrint("hi");
        assertEquals("--ide", lastCall()[0]);
    }

    // ------------------------------------------------------------
    // agents list parsing
    // ------------------------------------------------------------

    @Test
    void shouldParseAgentsListJson() {
        exec.result = new ClaudeCodeCliResult(0,
                "[{\"id\":\"a1\",\"name\":\"helper\",\"session_id\":\"s\"}]",
                "");

        List<ClaudeAgent> agents = client.agentsListAsObjects();

        assertEquals(1, agents.size());
        assertEquals("a1", agents.get(0).getId());
        assertEquals("s", agents.get(0).getSessionId());
    }

    @Test
    void shouldReturnEmptyListWhenAgentsListFailed() {
        exec.result = new ClaudeCodeCliResult(1, "", "boom");

        List<ClaudeAgent> agents = client.agentsListAsObjects();

        assertTrue(agents.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenAgentsListStdoutEmpty() {
        exec.result = new ClaudeCodeCliResult(0, "", "");

        List<ClaudeAgent> agents = client.agentsListAsObjects();

        assertTrue(agents.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenAgentsListJsonMalformed() {
        exec.result = new ClaudeCodeCliResult(0, "not json", "");

        List<ClaudeAgent> agents = client.agentsListAsObjects();

        assertTrue(agents.isEmpty());
    }

    // ------------------------------------------------------------
    // execute(...)
    // ------------------------------------------------------------

    @Test
    void shouldForwardExecuteToUnderlyingExecutor() {
        client.execute("a", "b", "c");
        assertEquals(3, lastCall().length);
        assertEquals("a", lastCall()[0]);
        assertEquals("c", lastCall()[2]);
    }

    // ------------------------------------------------------------
    // config-driven default print options
    // ------------------------------------------------------------

    @Test
    void shouldMergeConfigDefaultsIntoPrintCall() {
        config.setDefaultModel("sonnet");
        config.setFallbackModel("opus");
        config.setDefaultOutputFormat("json");
        config.setIncludePartialMessages(true);
        config.setDefaultPermissionMode("auto");
        config.setDefaultEffort("high");
        config.setMaxBudgetUsd(2.0);
        config.setJsonSchema("schema");
        config.setSystemPrompt("sp");
        config.setSystemPromptFile("spf");
        config.setAppendSystemPrompt("ap");
        config.setAppendSystemPromptFile("apf");
        config.setAgent("ag");
        config.setAgents("{}");
        config.setAllowedTools("a,b");
        config.setDisallowedTools("c");
        config.setTools("default");
        config.setMcpConfig("mcp");
        config.setStrictMcpConfig(true);
        config.setAddDir("/tmp");
        config.setFileResources("fr");
        config.setNoSessionPersistence(true);
        config.setWorktree(true);
        config.setTmux("classic");
        config.setBare(true);
        config.setDebug(true);
        config.setDebugFilter("api");
        config.setDebugFile("/tmp/dbg");
        config.setVerbose(true);
        config.setIde(true);
        config.setChrome(true);
        config.setNoChrome(true);
        config.setAllowDangerouslySkipPermissions(true);
        config.setDisableSlashCommands(true);
        config.setExcludeDynamicSystemPromptSections(true);
        config.setIncludeHookEvents(true);
        config.setMcpDebug(true);
        config.setBetas("beta");
        config.setSettings("set");
        config.setSettingSources("user");
        config.setPluginDir(new String[]{"/p1"});
        config.setPluginUrl(new String[]{"https://x"});
        config.setRemoteControl("rc");
        config.setRemoteControlSessionNamePrefix("pre");
        config.setSessionName("sn");

        client.print("hi");
        String[] args = lastCall();
        java.util.List<String> list = java.util.Arrays.asList(args);

        // spot-check the most important fields are forwarded.
        assertTrue(list.contains("--model"));
        assertTrue(list.contains("sonnet"));
        assertTrue(list.contains("--fallback-model"));
        assertTrue(list.contains("opus"));
        assertTrue(list.contains("--output-format"));
        assertTrue(list.contains("json"));
        assertTrue(list.contains("--permission-mode"));
        assertTrue(list.contains("auto"));
        assertTrue(list.contains("--effort"));
        assertTrue(list.contains("high"));
        assertTrue(list.contains("--max-budget-usd"));
        assertTrue(list.contains("2.0"));
        assertTrue(list.contains("--json-schema"));
        assertTrue(list.contains("schema"));
        assertTrue(list.contains("--system-prompt"));
        assertTrue(list.contains("sp"));
        assertTrue(list.contains("--system-prompt-file"));
        assertTrue(list.contains("spf"));
        assertTrue(list.contains("--append-system-prompt"));
        assertTrue(list.contains("ap"));
        assertTrue(list.contains("--append-system-prompt-file"));
        assertTrue(list.contains("apf"));
        assertTrue(list.contains("--agent"));
        assertTrue(list.contains("ag"));
        assertTrue(list.contains("--agents"));
        assertTrue(list.contains("{}"));
        assertTrue(list.contains("--allowedTools"));
        assertTrue(list.contains("a,b"));
        assertTrue(list.contains("--disallowedTools"));
        assertTrue(list.contains("c"));
        assertTrue(list.contains("--tools"));
        assertTrue(list.contains("default"));
        assertTrue(list.contains("--mcp-config"));
        assertTrue(list.contains("mcp"));
        assertTrue(list.contains("--strict-mcp-config"));
        assertTrue(list.contains("--add-dir"));
        assertTrue(list.contains("/tmp"));
        assertTrue(list.contains("--file"));
        assertTrue(list.contains("fr"));
        assertTrue(list.contains("--no-session-persistence"));
        assertTrue(list.contains("-w"));
        assertTrue(list.contains("--tmux"));
        assertTrue(list.contains("--bare"));
        assertTrue(list.contains("--debug"));
        assertTrue(list.contains("api"));
        assertTrue(list.contains("--debug-file"));
        assertTrue(list.contains("/tmp/dbg"));
        assertTrue(list.contains("--verbose"));
        assertTrue(list.contains("--ide"));
        assertTrue(list.contains("--chrome"));
        assertTrue(list.contains("--no-chrome"));
        assertTrue(list.contains("--allow-dangerously-skip-permissions"));
        assertTrue(list.contains("--disable-slash-commands"));
        assertTrue(list.contains("--exclude-dynamic-system-prompt-sections"));
        assertTrue(list.contains("--include-hook-events"));
        assertTrue(list.contains("--mcp-debug"));
        assertTrue(list.contains("--betas"));
        assertTrue(list.contains("beta"));
        assertTrue(list.contains("--settings"));
        assertTrue(list.contains("set"));
        assertTrue(list.contains("--setting-sources"));
        assertTrue(list.contains("user"));
        assertTrue(list.contains("--plugin-dir"));
        assertTrue(list.contains("/p1"));
        assertTrue(list.contains("--plugin-url"));
        assertTrue(list.contains("https://x"));
        assertTrue(list.contains("--remote-control"));
        assertTrue(list.contains("rc"));
        assertTrue(list.contains("--remote-control-session-name-prefix"));
        assertTrue(list.contains("pre"));
        assertTrue(list.contains("-n"));
        assertTrue(list.contains("sn"));
    }

    @Test
    void shouldNotEmitAnyDefaultOptionsWhenConfigIsEmpty() {
        // Default output format = stream-json and includePartialMessages = true are defaults
        client.print("hi");
        String[] args = lastCall();
        java.util.List<String> list = java.util.Arrays.asList(args);

        // The only items should be --output-format, stream-json, --include-partial-messages, -p, hi
        assertEquals(5, args.length);
        assertTrue(list.contains("--output-format"));
        assertTrue(list.contains("stream-json"));
        assertTrue(list.contains("--include-partial-messages"));
        assertFalse(list.contains("--model"));
        assertFalse(list.contains("--bare"));
    }

    @Test
    void shouldNotEmitIncludePartialMessagesWhenConfigFalse() {
        config.setIncludePartialMessages(false);
        client.print("hi");
        java.util.List<String> list = java.util.Arrays.asList(lastCall());
        assertFalse(list.contains("--include-partial-messages"));
    }

    @Test
    void shouldNotEmitWorktreeFlagWhenConfigFalse() {
        config.setWorktree(false);
        client.print("hi");
        java.util.List<String> list = java.util.Arrays.asList(lastCall());
        assertFalse(list.contains("-w"));
    }

    // ------------------------------------------------------------
    // StreamResult
    // ------------------------------------------------------------

    @Test
    void streamResultShouldExposeAllFields() {
        ClaudeCodeClient.StreamResult sr = new ClaudeCodeClient.StreamResult(
                java.util.Collections.emptyList(),
                null,
                new io.github.easy4j.claudecode.cli.ClaudeCodeCliResult(0, "", ""));

        assertNotNull(sr.getMessages());
        assertNull(sr.getResult());
        assertNotNull(sr.getRawResult());
        assertEquals("", sr.getTextContent());
        assertNull(sr.getTotalCostUsd());
    }

    @Test
    void streamResultShouldExposeTextAndCostWhenResultPresent() {
        io.github.easy4j.claudecode.model.ClaudeResult cr =
                new io.github.easy4j.claudecode.model.ClaudeResult();
        cr.setResult("hi");
        cr.setTotalCostUsd(0.42);

        ClaudeCodeClient.StreamResult sr = new ClaudeCodeClient.StreamResult(
                java.util.Collections.emptyList(),
                cr,
                new io.github.easy4j.claudecode.cli.ClaudeCodeCliResult(0, "", ""));

        assertEquals("hi", sr.getTextContent());
        assertEquals(0.42, sr.getTotalCostUsd(), 0.0001);
    }
}
