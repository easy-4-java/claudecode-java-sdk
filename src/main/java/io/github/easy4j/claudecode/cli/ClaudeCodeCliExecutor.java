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
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Subprocess executor for the local {@code claude} CLI.
 *
 * <p>Every invocation spawns a fresh process via Apache Commons Exec,
 * captures stdout/stderr in memory and bounds execution by
 * {@link ClaudeCodeClientConfig#getLocalTimeoutSeconds()}.</p>
 *
 * <p>{@link #probe()} is a thin wrapper around {@code claude --version}
 * that returns {@code true} when the CLI is reachable on the local
 * {@code PATH} (or via {@link ClaudeCodeClientConfig#getLocalExecutable()}).</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ClaudeCodeCli
 * @see ClaudeCodeCliResult
 */
public class ClaudeCodeCliExecutor {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCodeCliExecutor.class);

    private final ClaudeCodeClientConfig config;

    /**
     * Construct an executor bound to the supplied configuration.
     *
     * @param config the client configuration (must not be {@code null})
     */
    public ClaudeCodeCliExecutor(ClaudeCodeClientConfig config) {
        this.config = config;
    }

    /**
     * Run the CLI with the supplied argument vector.
     *
     * <p>{@code null} entries inside {@code args} are silently skipped
     * to make optional setters easier to forward.</p>
     *
     * @param args CLI arguments appended after the configured executable
     * @return the captured exit code, stdout and stderr
     */
    public ClaudeCodeCliResult execute(String... args) {
        CommandLine cmd = CommandLine.parse(config.getLocalExecutable());
        for (String arg : args) {
            if (arg != null) {
                cmd.addArgument(arg);
            }
        }

        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        executor.setStreamHandler(new org.apache.commons.exec.PumpStreamHandler(stdout, stderr));

        long timeoutMs = config.getLocalTimeoutSeconds() * 1000L;
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutMs);
        executor.setWatchdog(watchdog);

        try {
            int exitCode = executor.execute(cmd);
            String out = stdout.toString().trim();
            String err = stderr.toString().trim();
            log.debug("claude CLI executed: exitCode={}, stdout.len={}", exitCode, out.length());
            if (watchdog.killedProcess()) {
                return new ClaudeCodeCliResult(-1, out, "claude CLI timed out after " + timeoutMs + " ms\n" + err);
            }
            return new ClaudeCodeCliResult(exitCode, out, err);
        } catch (IOException e) {
            return new ClaudeCodeCliResult(-1, "", e.getMessage());
        }
    }

    /**
     * Probe whether the local CLI is reachable.
     *
     * <p>Runs {@code claude --version} and returns {@code true} when the
     * command exits with status 0.</p>
     *
     * @return {@code true} when the CLI is reachable, {@code false} on any
     *         exception or non-zero exit code
     */
    public boolean probe() {
        try {
            ClaudeCodeCliResult result = execute("--version");
            return result.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }
}
