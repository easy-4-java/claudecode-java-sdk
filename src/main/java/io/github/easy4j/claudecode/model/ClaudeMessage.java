package io.github.easy4j.claudecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Claude Code stream-json 输出中的单条消息。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeMessage {

    private String type;
    private String message;
    private String uuid;

    @JsonProperty("session_id")
    private String sessionId;

    private Object content;

    @JsonProperty("parent_tool_use_id")
    private String parentToolUseId;

    @JsonProperty("tool_use_result")
    private Object toolUseResult;
}
