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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClaudeCodeClientConfig}.
 *
 * @since 3.0.0
 */
class ClaudeCodeClientConfigTest {

    @Test
    void shouldExposeSensibleDefaults() {
        ClaudeCodeClientConfig cfg = new ClaudeCodeClientConfig();

        assertEquals("claude", cfg.getLocalExecutable());
        assertEquals(600, cfg.getLocalTimeoutSeconds());
        assertEquals(5, cfg.getLocalProbeTimeoutSeconds());
        assertEquals("stream-json", cfg.getDefaultOutputFormat());
        assertTrue(cfg.isIncludePartialMessages());
        assertFalse(cfg.isNoSessionPersistence());
        assertFalse(cfg.isWorktree());
        assertFalse(cfg.isBare());
        assertFalse(cfg.isStrictMcpConfig());
        assertFalse(cfg.isDebug());
        assertFalse(cfg.isVerbose());
        assertFalse(cfg.isIde());
        assertFalse(cfg.isChrome());
        assertFalse(cfg.isNoChrome());
        assertFalse(cfg.isAllowDangerouslySkipPermissions());
        assertFalse(cfg.isDisableSlashCommands());
        assertFalse(cfg.isExcludeDynamicSystemPromptSections());
        assertFalse(cfg.isIncludeHookEvents());
        assertFalse(cfg.isMcpDebug());
    }

    @Test
    void shouldRoundTripScalarValuesThroughSetters() {
        ClaudeCodeClientConfig cfg = new ClaudeCodeClientConfig();
        cfg.setDefaultModel("sonnet");
        cfg.setDefaultEffort("high");
        cfg.setDefaultPermissionMode("auto");
        cfg.setMaxBudgetUsd(0.42);
        cfg.setSessionName("s");
        cfg.setAddDir("/tmp");
        cfg.setJsonSchema("schema");
        cfg.setSystemPrompt("sp");
        cfg.setSystemPromptFile("spf");
        cfg.setAppendSystemPrompt("ap");
        cfg.setAppendSystemPromptFile("apf");
        cfg.setAgent("a");
        cfg.setAgents("{}");
        cfg.setAllowedTools("t1");
        cfg.setDisallowedTools("t2");
        cfg.setTools("default");
        cfg.setMcpConfig("mcp");
        cfg.setFallbackModel("opus");
        cfg.setDebugFilter("api");
        cfg.setDebugFile("/tmp/dbg");
        cfg.setBetas("beta");
        cfg.setSettings("settings");
        cfg.setSettingSources("user");
        cfg.setTmux("classic");
        cfg.setRemoteControl("rc");
        cfg.setRemoteControlSessionNamePrefix("prefix");
        cfg.setFileResources("file_id:path");
        cfg.setDefaultOutputFormat("json");
        cfg.setNoSessionPersistence(true);
        cfg.setWorktree(true);
        cfg.setBare(true);
        cfg.setDebug(true);
        cfg.setVerbose(true);
        cfg.setIde(true);
        cfg.setChrome(true);
        cfg.setNoChrome(true);
        cfg.setAllowDangerouslySkipPermissions(true);
        cfg.setDisableSlashCommands(true);
        cfg.setExcludeDynamicSystemPromptSections(true);
        cfg.setIncludeHookEvents(true);
        cfg.setMcpDebug(true);
        cfg.setStrictMcpConfig(true);
        cfg.setIncludePartialMessages(false);

        assertEquals("sonnet", cfg.getDefaultModel());
        assertEquals("high", cfg.getDefaultEffort());
        assertEquals("auto", cfg.getDefaultPermissionMode());
        assertEquals(0.42, cfg.getMaxBudgetUsd());
        assertEquals("s", cfg.getSessionName());
        assertEquals("/tmp", cfg.getAddDir());
        assertEquals("schema", cfg.getJsonSchema());
        assertEquals("sp", cfg.getSystemPrompt());
        assertEquals("spf", cfg.getSystemPromptFile());
        assertEquals("ap", cfg.getAppendSystemPrompt());
        assertEquals("apf", cfg.getAppendSystemPromptFile());
        assertEquals("a", cfg.getAgent());
        assertEquals("{}", cfg.getAgents());
        assertEquals("t1", cfg.getAllowedTools());
        assertEquals("t2", cfg.getDisallowedTools());
        assertEquals("default", cfg.getTools());
        assertEquals("mcp", cfg.getMcpConfig());
        assertEquals("opus", cfg.getFallbackModel());
        assertEquals("api", cfg.getDebugFilter());
        assertEquals("/tmp/dbg", cfg.getDebugFile());
        assertEquals("beta", cfg.getBetas());
        assertEquals("settings", cfg.getSettings());
        assertEquals("user", cfg.getSettingSources());
        assertEquals("classic", cfg.getTmux());
        assertEquals("rc", cfg.getRemoteControl());
        assertEquals("prefix", cfg.getRemoteControlSessionNamePrefix());
        assertEquals("file_id:path", cfg.getFileResources());
        assertEquals("json", cfg.getDefaultOutputFormat());
        assertTrue(cfg.isNoSessionPersistence());
        assertTrue(cfg.isWorktree());
        assertTrue(cfg.isBare());
        assertTrue(cfg.isDebug());
        assertTrue(cfg.isVerbose());
        assertTrue(cfg.isIde());
        assertTrue(cfg.isChrome());
        assertTrue(cfg.isNoChrome());
        assertTrue(cfg.isAllowDangerouslySkipPermissions());
        assertTrue(cfg.isDisableSlashCommands());
        assertTrue(cfg.isExcludeDynamicSystemPromptSections());
        assertTrue(cfg.isIncludeHookEvents());
        assertTrue(cfg.isMcpDebug());
        assertTrue(cfg.isStrictMcpConfig());
        assertFalse(cfg.isIncludePartialMessages());
    }

    @Test
    void shouldRoundTripArrayValuesThroughSetters() {
        ClaudeCodeClientConfig cfg = new ClaudeCodeClientConfig();
        cfg.setPluginDir(new String[]{"a", "b"});
        cfg.setPluginUrl(new String[]{"https://x"});

        assertArrayEquals(new String[]{"a", "b"}, cfg.getPluginDir());
        assertArrayEquals(new String[]{"https://x"}, cfg.getPluginUrl());
    }

    @Test
    void shouldExposeLombokEqualsHashCodeAndToString() {
        ClaudeCodeClientConfig a = new ClaudeCodeClientConfig();
        ClaudeCodeClientConfig b = new ClaudeCodeClientConfig();
        a.setDefaultModel("sonnet");
        b.setDefaultModel("sonnet");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        b.setDefaultModel("opus");
        assertNotEquals(a, b);
        assertTrue(a.toString().contains("ClaudeCodeClientConfig"));
    }
}
