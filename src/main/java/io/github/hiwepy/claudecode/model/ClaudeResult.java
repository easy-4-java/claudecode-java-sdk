package io.github.hiwepy.claudecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Claude Code stream-json 结果消息（type=result）。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResult {

    private String type;
    private String result;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("total_cost_usd")
    private Double totalCostUsd;

    private Usage usage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("input_tokens")
        private long inputTokens;

        @JsonProperty("output_tokens")
        private long outputTokens;

        @JsonProperty("cache_creation_tokens")
        private long cacheCreationTokens;

        @JsonProperty("cache_read_tokens")
        private long cacheReadTokens;
    }
}
