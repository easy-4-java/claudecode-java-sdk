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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link ClaudeMessage}.
 *
 * @since 3.0.0
 */
class ClaudeMessageTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void shouldDeserialiseAssistantMessage() throws Exception {
        Map<String, Object> content = new HashMap<>();
        content.put("text", "hello");
        String json = mapper.writeValueAsString(new HashMap<String, Object>() {{
            put("type", "assistant");
            put("message", "msg body");
            put("uuid", "uuid-1");
            put("session_id", "sess-1");
            put("content", content);
            put("parent_tool_use_id", "tool-1");
            put("tool_use_result", "ok");
        }});

        ClaudeMessage msg = mapper.readValue(json, ClaudeMessage.class);

        assertNotNull(msg);
        assertEquals("assistant", msg.getType());
        assertEquals("msg body", msg.getMessage());
        assertEquals("uuid-1", msg.getUuid());
        assertEquals("sess-1", msg.getSessionId());
        assertNotNull(msg.getContent());
        assertEquals("tool-1", msg.getParentToolUseId());
        assertEquals("ok", msg.getToolUseResult());
    }

    @Test
    void shouldIgnoreUnknownProperties() throws Exception {
        String json = "{\"type\":\"system\",\"message\":\"sys\",\"unknown\":\"x\"}";

        ClaudeMessage msg = mapper.readValue(json, ClaudeMessage.class);

        assertEquals("system", msg.getType());
        assertEquals("sys", msg.getMessage());
    }

    @Test
    void shouldAcceptNullAndAbsentFields() throws Exception {
        String json = "{\"type\":\"user\"}";

        ClaudeMessage msg = mapper.readValue(json, ClaudeMessage.class);

        assertEquals("user", msg.getType());
        assertEquals(null, msg.getMessage());
        assertEquals(null, msg.getUuid());
        assertEquals(null, msg.getSessionId());
        assertEquals(null, msg.getContent());
        assertEquals(null, msg.getParentToolUseId());
        assertEquals(null, msg.getToolUseResult());
    }
}
