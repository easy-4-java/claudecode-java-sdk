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
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
 * @author <a href="https://github.com/loong10k">Loong Wan</a>
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
        return runProcess(null, args);
    }

    /**
     * Run the CLI with the supplied argument vector, feeding {@code stdin} to
     * the child process's standard input.
     *
     * <p>Use this for commands that consume piped payloads, such as
     * {@code cat file | claude -p "query"} or
     * {@code --input-format stream-json} conversations. A {@code null} or
     * empty {@code stdin} behaves exactly like {@link #execute(String...)}:
     * the child receives an immediately-closed stdin pipe rather than an
     * inherited TTY, so non-interactive commands are never blocked.</p>
     *
     * @param stdin optional text piped to the child's standard input
     * @param args  CLI arguments appended after the configured executable
     * @return the captured exit code, stdout and stderr
     */
    public ClaudeCodeCliResult executeWithStdin(String stdin, String... args) {
        return runProcess(stdin, args);
    }

    private ClaudeCodeCliResult runProcess(String stdin, String... args) {
        CommandLine cmd = CommandLine.parse(config.getLocalExecutable());
        for (String arg : args) {
            if (arg != null) {
                // handleQuoting=false: the child is spawned via exec(argv), not
                // a shell — commons-exec's default quoting would embed literal
                // double quotes inside arguments containing spaces (prompts,
                // tool lists, paths), corrupting them on arrival.
                cmd.addArgument(arg, false);
            }
        }

        DefaultExecutor executor = new DefaultExecutor();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        // Always hand the child a (possibly empty) stdin pipe that closes right
        // after the payload: consumers read to EOF, and a closed pipe cannot
        // race the input pump.
        byte[] stdinBytes = stdin == null ? new byte[0] : stdin.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        executor.setStreamHandler(new org.apache.commons.exec.PumpStreamHandler(stdout, stderr,
                new ByteArrayInputStream(stdinBytes)));

        Map<String, String> environment = resolveEnvironment();

        long timeoutMs = config.getLocalTimeoutSeconds() * 1000L;
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeoutMs);
        executor.setWatchdog(watchdog);

        long startNanos = System.nanoTime();
        try {
            int exitCode = environment == null
                    ? executor.execute(cmd)
                    : executor.execute(cmd, environment);
            String out = stdout.toString(java.nio.charset.StandardCharsets.UTF_8).trim();
            String err = stderr.toString(java.nio.charset.StandardCharsets.UTF_8).trim();
            log.debug("claude CLI executed: exitCode={}, stdout.len={}", exitCode, out.length());
            if (watchdog.killedProcess()) {
                return new ClaudeCodeCliResult(-1, out, "claude CLI timed out after " + timeoutMs + " ms\n" + err);
            }
            return new ClaudeCodeCliResult(exitCode, out, err);
        } catch (ExecuteException e) {
            // commons-exec throws ExecuteException for EVERY non-zero exit
            // (and for watchdog kills). The stream pumps are joined before it
            // is thrown, so both buffers are complete — surface them together
            // with the real exit code instead of discarding the output. The
            // deadline check makes the timeout verdict race-free even when
            // {@code watchdog.killedProcess()} has not observed the kill yet.
            String out = stdout.toString(java.nio.charset.StandardCharsets.UTF_8).trim();
            String err = stderr.toString(java.nio.charset.StandardCharsets.UTF_8).trim();
            boolean timedOut = watchdog.killedProcess()
                    || System.nanoTime() - startNanos >= timeoutMs * 1_000_000L;
            if (timedOut) {
                return new ClaudeCodeCliResult(-1, out, "claude CLI timed out after " + timeoutMs + " ms\n" + err);
            }
            log.debug("claude CLI failed: exitCode={}, stdout.len={}, stderr.len={}",
                    e.getExitValue(), out.length(), err.length());
            return new ClaudeCodeCliResult(e.getExitValue(), out, err);
        } catch (IOException e) {
            return new ClaudeCodeCliResult(-1, "", e.getMessage());
        }
    }

    /**
     * Merges {@link ClaudeCodeClientConfig#getEnvironment()} over the JVM's
     * current process environment so the child sees the caller's ANTHROPIC and
     * CLAUDE variable overrides without losing the inherited environment.
     * Returns {@code null} when there are no overrides, meaning "inherit
     * everything unchanged".
     */
    private Map<String, String> resolveEnvironment() {
        Map<String, String> overrides = config.getEnvironment();
        if (overrides == null || overrides.isEmpty()) {
            return null;
        }
        try {
            Map<String, String> merged = new HashMap<>(org.apache.commons.exec.environment.EnvironmentUtils.getProcEnvironment());
            merged.putAll(overrides);
            return merged;
        } catch (IOException e) {
            log.debug("Falling back to overrides-only environment: {}", e.getMessage());
            return new HashMap<>(overrides);
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
