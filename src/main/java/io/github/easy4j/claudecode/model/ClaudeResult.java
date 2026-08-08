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
package io.github.easy4j.claudecode.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Terminal result envelope produced by Claude Code
 * ({@code type == "result"} inside {@code stream-json}).
 *
 * <p>The class exposes the textual {@link #result}, the owning
 * {@link #sessionId}, the USD cost reported for the run
 * ({@link #totalCostUsd}) and a nested {@link Usage} breakdown of token
 * consumption.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ClaudeMessage
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeResult {

    /** Envelope type discriminator, always {@code result}. */
    private String type;

    /** Final textual result returned by the agent. */
    private String result;

    /** Session identifier this result belongs to. */
    @JsonProperty("session_id")
    private String sessionId;

    /** Total USD cost reported for the run. */
    @JsonProperty("total_cost_usd")
    private Double totalCostUsd;

    /** Token usage breakdown. */
    private Usage usage;

    /**
     * Token usage breakdown for a single invocation.
     *
     * @author [@Loong Wan](https://github.com/loong10k)
     * @since 3.0.0
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {

        /** Number of input tokens billed. */
        @JsonProperty("input_tokens")
        private long inputTokens;

        /** Number of output tokens billed. */
        @JsonProperty("output_tokens")
        private long outputTokens;

        /** Number of cache-creation tokens billed. */
        @JsonProperty("cache_creation_tokens")
        private long cacheCreationTokens;

        /** Number of cache-read tokens billed. */
        @JsonProperty("cache_read_tokens")
        private long cacheReadTokens;
    }
}
