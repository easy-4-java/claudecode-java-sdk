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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ClaudeCodeCliResult}.
 *
 * @since 3.0.0
 */
class ClaudeCodeCliResultTest {

    @Test
    void shouldExposeConstructorArgumentsViaGetters() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(7, "out", "err");

        assertEquals(7, result.getExitCode());
        assertEquals("out", result.getStdout());
        assertEquals("err", result.getStderr());
    }

    @Test
    void shouldReturnTrueForSuccessWhenExitCodeIsZero() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(0, "", "");

        assertTrue(result.isSuccess());
        assertFalse(result.isTimeout());
    }

    @Test
    void shouldReturnFalseForSuccessWhenExitCodeIsNonZero() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(1, "x", "y");

        assertFalse(result.isSuccess());
        assertFalse(result.isTimeout());
    }

    @Test
    void shouldDetectTimeoutViaNegativeExitCodeAndMatchingStderr() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(-1, "partial", "claude CLI timed out after 5000 ms");

        assertTrue(result.isTimeout());
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotDetectTimeoutWhenStderrMissingMagicWord() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(-1, "", "something else went wrong");

        assertFalse(result.isTimeout());
    }

    @Test
    void shouldHandleNullStderrGracefully() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(-1, "x", null);

        assertFalse(result.isTimeout());
        assertNull(result.getStderr());
    }

    @Test
    void shouldRespectLombokEqualsAndHashCode() {
        ClaudeCodeCliResult a = new ClaudeCodeCliResult(0, "out", "err");
        ClaudeCodeCliResult b = new ClaudeCodeCliResult(0, "out", "err");
        ClaudeCodeCliResult c = new ClaudeCodeCliResult(1, "out", "err");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    @Test
    void shouldProduceReadableToString() {
        ClaudeCodeCliResult result = new ClaudeCodeCliResult(2, "o", "e");

        String text = result.toString();
        assertTrue(text.contains("exitCode=2"));
        assertTrue(text.contains("stdout=o"));
        assertTrue(text.contains("stderr=e"));
    }
}
