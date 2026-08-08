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
 * Single record emitted by Claude Code's {@code stream-json} output.
 *
 * <p>The shape of each record depends on its {@link #type}:
 * {@code assistant}, {@code user}, {@code tool_use}, {@code tool_result},
 * {@code system}, {@code result}, etc. Because the schema is open-ended,
 * unrecognised fields are tolerated
 * ({@link JsonIgnoreProperties}) and the {@link #content} /
 * {@link #toolUseResult} values are typed as {@link Object} so they can
 * hold either text, structured content blocks or arbitrary JSON.</p>
 *
 * @author [@Loong Wan](https://github.com/loong10k)
 * @since 3.0.0
 * @see ClaudeResult
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeMessage {

    /** Message type discriminator (e.g. {@code assistant}, {@code result}). */
    private String type;

    /** Raw message text or sub-envelope. */
    private String message;

    /** Unique identifier of the message. */
    private String uuid;

    /** Session identifier this message belongs to. */
    @JsonProperty("session_id")
    private String sessionId;

    /** Content blocks carried by the message (text, image, tool call, etc.). */
    private Object content;

    /** Parent tool-use identifier for nested invocations. */
    @JsonProperty("parent_tool_use_id")
    private String parentToolUseId;

    /** Result returned by a tool execution. */
    @JsonProperty("tool_use_result")
    private Object toolUseResult;
}
