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

import lombok.Data;

/**
 * Immutable result of a {@code claude} CLI invocation.
 *
 * <p>The class carries the subprocess exit code alongside the captured
 * stdout/stderr text. Convenience predicates
 * ({@link #isSuccess()}, {@link #isTimeout()}) make common success and
 * timeout checks cheap.</p>
 *
 * @author easy-4-java contributors
 * @since 3.0.0
 */
@Data
public class ClaudeCodeCliResult {

    /** Exit code returned by the CLI ({@code -1} on watchdog timeout or IOException). */
    private final int exitCode;

    /** Trimmed stdout captured from the CLI subprocess. */
    private final String stdout;

    /** Trimmed stderr captured from the CLI subprocess. */
    private final String stderr;

    /**
     * @return {@code true} when {@link #exitCode} equals {@code 0}
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }

    /**
     * @return {@code true} when the subprocess was killed by the watchdog
     *         timeout and the resulting stderr contains the word
     *         {@code "timed out"}
     */
    public boolean isTimeout() {
        return exitCode == -1 && stderr != null && stderr.contains("timed out");
    }
}
