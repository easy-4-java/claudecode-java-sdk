package io.github.hiwepy.claudecode.cli;

import lombok.Data;

/**
 * Claude Code CLI 执行结果。
 */
@Data
public class ClaudeCodeCliResult {

    private final int exitCode;
    private final String stdout;
    private final String stderr;

    public boolean isSuccess() {
        return exitCode == 0;
    }

    public boolean isTimeout() {
        return exitCode == -1 && stderr != null && stderr.contains("timed out");
    }
}
