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
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link ClaudeResult} and its nested {@link ClaudeResult.Usage}.
 *
 * @since 3.0.0
 */
class ClaudeResultTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Test
    void shouldDeserialiseFullResultEnvelope() throws Exception {
        String json = "{" +
                "\"type\":\"result\"," +
                "\"result\":\"done\"," +
                "\"session_id\":\"sess\"," +
                "\"total_cost_usd\":0.0123," +
                "\"usage\":{" +
                "\"input_tokens\":100," +
                "\"output_tokens\":50," +
                "\"cache_creation_tokens\":10," +
                "\"cache_read_tokens\":5" +
                "}}";

        ClaudeResult res = mapper.readValue(json, ClaudeResult.class);

        assertNotNull(res);
        assertEquals("result", res.getType());
        assertEquals("done", res.getResult());
        assertEquals("sess", res.getSessionId());
        assertEquals(0.0123, res.getTotalCostUsd(), 0.0001);

        ClaudeResult.Usage usage = res.getUsage();
        assertNotNull(usage);
        assertEquals(100, usage.getInputTokens());
        assertEquals(50, usage.getOutputTokens());
        assertEquals(10, usage.getCacheCreationTokens());
        assertEquals(5, usage.getCacheReadTokens());
    }

    @Test
    void shouldIgnoreUnknownProperties() throws Exception {
        String json = "{\"type\":\"result\",\"result\":\"x\",\"unexpected\":\"y\"}";

        ClaudeResult res = mapper.readValue(json, ClaudeResult.class);

        assertEquals("x", res.getResult());
        assertNull(res.getUsage());
    }

    @Test
    void shouldSerialiseAndRoundTrip() throws Exception {
        ClaudeResult original = new ClaudeResult();
        original.setType("result");
        original.setResult("done");
        original.setSessionId("s");
        original.setTotalCostUsd(0.5);

        ClaudeResult.Usage usage = new ClaudeResult.Usage();
        usage.setInputTokens(1);
        usage.setOutputTokens(2);
        usage.setCacheCreationTokens(3);
        usage.setCacheReadTokens(4);
        original.setUsage(usage);

        String json = mapper.writeValueAsString(original);
        ClaudeResult parsed = mapper.readValue(json, ClaudeResult.class);

        assertEquals(original.getType(), parsed.getType());
        assertEquals(original.getResult(), parsed.getResult());
        assertEquals(original.getSessionId(), parsed.getSessionId());
        assertEquals(original.getTotalCostUsd(), parsed.getTotalCostUsd());
        assertNotNull(parsed.getUsage());
        assertEquals(1, parsed.getUsage().getInputTokens());
        assertEquals(2, parsed.getUsage().getOutputTokens());
        assertEquals(3, parsed.getUsage().getCacheCreationTokens());
        assertEquals(4, parsed.getUsage().getCacheReadTokens());
    }
}
