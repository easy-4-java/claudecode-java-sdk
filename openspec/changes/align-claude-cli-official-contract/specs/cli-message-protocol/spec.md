# CLI Message Protocol

## Purpose

Define lossless parsing and normalized projection of Claude CLI JSON and stream-json output.

## Requirements

### Requirement: Lossless result parsing

The SDK MUST parse terminal result envelopes from the original JSON envelope rather than reconstructing them from a lossy generic message.

#### Scenario: Result includes cost and usage
- GIVEN a result envelope with result text, session id, cost and usage
- WHEN it is decoded
- THEN all supplied fields remain available.

### Requirement: Structured and unknown events

The SDK MUST preserve supported nested event structures and MUST retain unknown event envelopes in a raw representation.

#### Scenario: Unknown event type
- WHEN an otherwise valid JSON event has an unrecognized type
- THEN the stream remains usable
- AND callers can inspect the raw event.

### Requirement: Usage field compatibility

The SDK MUST support the official cache input-token field names and SHOULD provide explicitly documented compatibility aliases for known historical inputs.

#### Scenario: Official cache fields
- GIVEN `cache_creation_input_tokens` and `cache_read_input_tokens`
- WHEN usage is decoded
- THEN their values are preserved.

### Requirement: Parsing diagnostics

Malformed frames MUST be diagnosable and MUST NOT silently turn a required missing result into a successful empty result.
