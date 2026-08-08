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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Test double for {@link ClaudeCodeCliExecutor} that records every
 * {@link #execute(String...)} invocation and returns a configurable result.
 *
 * <p>This class is package-private and intended solely for unit tests.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ClaudeCodeCliExecutor
 */
public class FakeClaudeCodeCliExecutor extends ClaudeCodeCliExecutor {

    /** Recorded argument vectors, one entry per {@link #execute} call. */
    public final List<String[]> calls = Collections.synchronizedList(new ArrayList<>());

    /** Result returned by every {@link #execute} call (may be overridden per test). */
    public ClaudeCodeCliResult result = new ClaudeCodeCliResult(0, "", "");

    /**
     * Create a new fake executor backed by the supplied configuration.
     *
     * @param config client configuration
     */
    public FakeClaudeCodeCliExecutor(ClaudeCodeClientConfig config) {
        super(config);
    }

    /**
     * Record the supplied argument vector and return the pre-configured result.
     *
     * @param args CLI arguments
     * @return the configured {@link ClaudeCodeCliResult}
     */
    @Override
    public ClaudeCodeCliResult execute(String... args) {
        calls.add(args);
        return result;
    }
}
