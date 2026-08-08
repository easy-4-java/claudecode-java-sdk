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
 * Description of a background agent returned by {@code claude agents --json}.
 *
 * <p>The class mirrors the JSON document produced by the CLI: known
 * fields are mapped directly, unknown ones are ignored
 * ({@link JsonIgnoreProperties}). Snake-case names such as {@code session_id}
 * are converted to camel-case via {@link JsonProperty}.</p>
 *
 * @author easy-4-java contributors
 * @since 3.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeAgent {

    /** Agent identifier. */
    private String id;

    /** Display name of the agent. */
    private String name;

    /** Current status of the agent. */
    private String status;

    /** Working directory the agent operates in. */
    private String cwd;

    /** Session identifier the agent belongs to. */
    @JsonProperty("session_id")
    private String sessionId;

    /** Model currently bound to the agent. */
    @JsonProperty("model")
    private String model;

    /** ISO-8601 creation timestamp. */
    @JsonProperty("created_at")
    private String createdAt;

    /** ISO-8601 timestamp of the last activity. */
    @JsonProperty("last_active")
    private String lastActive;
}
