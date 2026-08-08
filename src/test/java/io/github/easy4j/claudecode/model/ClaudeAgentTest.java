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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link ClaudeAgent}.
 *
 * @since 3.0.0
 */
class ClaudeAgentTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void shouldDeserialiseAllSnakeCaseFields() throws Exception {
        String json = "{" +
                "\"id\":\"agent-1\"," +
                "\"name\":\"helper\"," +
                "\"status\":\"running\"," +
                "\"cwd\":\"/tmp\"," +
                "\"session_id\":\"sess-1\"," +
                "\"model\":\"claude-sonnet-4-6\"," +
                "\"created_at\":\"2026-08-08T00:00:00Z\"," +
                "\"last_active\":\"2026-08-08T01:00:00Z\"" +
                "}";

        ClaudeAgent agent = mapper.readValue(json, ClaudeAgent.class);

        assertNotNull(agent);
        assertEquals("agent-1", agent.getId());
        assertEquals("helper", agent.getName());
        assertEquals("running", agent.getStatus());
        assertEquals("/tmp", agent.getCwd());
        assertEquals("sess-1", agent.getSessionId());
        assertEquals("claude-sonnet-4-6", agent.getModel());
        assertEquals("2026-08-08T00:00:00Z", agent.getCreatedAt());
        assertEquals("2026-08-08T01:00:00Z", agent.getLastActive());
    }

    @Test
    void shouldIgnoreUnknownProperties() throws Exception {
        String json = "{\"id\":\"a\",\"name\":\"b\",\"unknown_field\":\"ignored\"}";

        ClaudeAgent agent = mapper.readValue(json, ClaudeAgent.class);

        assertEquals("a", agent.getId());
        assertEquals("b", agent.getName());
    }

    @Test
    void shouldSerialiseAndRoundTrip() throws Exception {
        ClaudeAgent original = new ClaudeAgent();
        original.setId("x");
        original.setName("y");
        original.setStatus("done");
        original.setCwd("/work");
        original.setSessionId("s");
        original.setModel("m");
        original.setCreatedAt("c");
        original.setLastActive("l");

        String json = mapper.writeValueAsString(original);
        ClaudeAgent parsed = mapper.readValue(json, ClaudeAgent.class);

        assertEquals(original.getId(), parsed.getId());
        assertEquals(original.getName(), parsed.getName());
        assertEquals(original.getStatus(), parsed.getStatus());
        assertEquals(original.getCwd(), parsed.getCwd());
        assertEquals(original.getSessionId(), parsed.getSessionId());
        assertEquals(original.getModel(), parsed.getModel());
        assertEquals(original.getCreatedAt(), parsed.getCreatedAt());
        assertEquals(original.getLastActive(), parsed.getLastActive());
    }
}
