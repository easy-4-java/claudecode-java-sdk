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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClaudeCodeCliExecutor}.
 *
 * <p>Tests invoke a real subprocess so they are coupled to the host
 * shell — a JDK is always present, so {@code java -version} is a safe
 * stand-in for the {@code claude} executable.</p>
 *
 * @since 3.0.0
 */
class ClaudeCodeCliExecutorTest {

    private ClaudeCodeCliExecutor newExecutorFor(String exe) {
        ClaudeCodeClientConfig config = new ClaudeCodeClientConfig();
        config.setLocalExecutable(exe);
        config.setLocalTimeoutSeconds(5);
        return new ClaudeCodeCliExecutor(config);
    }

    @Test
    void shouldCaptureStdoutFromSuccessfulCommand() {
        ClaudeCodeCliExecutor exec = newExecutorFor("java");

        ClaudeCodeCliResult result = exec.execute("-version");

        assertEquals(0, result.getExitCode());
        assertTrue(result.isSuccess());
        assertFalse(result.isTimeout());
        // java -version outputs to stderr on most JVMs
        String combined = (result.getStdout() + " " + result.getStderr()).toLowerCase();
        assertTrue(combined.contains("version"),
                "expected version output, got stdout=" + result.getStdout() + " stderr=" + result.getStderr());
    }

    @Test
    void shouldReturnFailureForUnknownCommand() {
        ClaudeCodeCliExecutor exec = newExecutorFor("definitely-not-a-real-command-xyz");

        ClaudeCodeCliResult result = exec.execute("arg");

        assertEquals(-1, result.getExitCode());
        assertFalse(result.isSuccess());
        assertFalse(result.isTimeout());
    }

    @Test
    void shouldReturnFailureForNonZeroExit() {
        ClaudeCodeCliExecutor exec = newExecutorFor("java");

        // java with no args prints usage and exits with non-zero on most JVMs.
        ClaudeCodeCliResult result = exec.execute("--this-flag-is-not-supported-12345");

        assertFalse(result.isSuccess());
        assertFalse(result.isTimeout());
    }

    @Test
    void shouldProbeTrueWhenCommandAvailable() {
        ClaudeCodeCliExecutor exec = newExecutorFor("java");

        assertTrue(exec.probe());
    }

    @Test
    void shouldProbeFalseWhenCommandUnavailable() {
        ClaudeCodeCliExecutor exec = newExecutorFor("definitely-not-a-real-command-xyz");

        assertFalse(exec.probe());
    }

    @Test
    void shouldHandleNullArgEntriesGracefully() {
        ClaudeCodeCliExecutor exec = newExecutorFor("java");

        ClaudeCodeCliResult result = exec.execute("-version", null, null);

        // Null entries are silently skipped — the version flag should still resolve.
        assertTrue(result.isSuccess(),
                "expected successful java -version call, got: " + result);
    }

    @Test
    void shouldExposeConfigBehaviourOnSuccessAndFailure() {
        ClaudeCodeClientConfig config = new ClaudeCodeClientConfig();
        config.setLocalExecutable("java");
        config.setLocalTimeoutSeconds(10);

        ClaudeCodeCliExecutor exec = new ClaudeCodeCliExecutor(config);

        ClaudeCodeCliResult result = exec.execute("-version");

        assertEquals(0, result.getExitCode());
        assertTrue(result.isSuccess());
        assertFalse(result.isTimeout());
    }
}
