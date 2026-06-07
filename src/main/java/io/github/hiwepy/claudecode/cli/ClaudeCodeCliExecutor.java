package io.github.hiwepy.claudecode.cli;

import io.github.hiwepy.claudecode.ClaudeCodeClientConfig;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Claude Code CLI 子进程执行器。
 */
public class ClaudeCodeCliExecutor {

    private static final Logger log = LoggerFactory.getLogger(ClaudeCodeCliExecutor.class);

    private final ClaudeCodeClientConfig config;

    public ClaudeCodeCliExecutor(ClaudeCodeClientConfig config) {
        this.config = config;
    }

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

    public boolean probe() {
        try {
            ClaudeCodeCliResult result = execute("--version");
            return result.isSuccess();
        } catch (Exception e) {
            return false;
        }
    }
}
