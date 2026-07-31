package io.github.easy4j.claudecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Claude Code agents --json 输出中的后台 agent 信息。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeAgent {

    private String id;
    private String name;
    private String status;
    private String cwd;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("model")
    private String model;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("last_active")
    private String lastActive;
}
