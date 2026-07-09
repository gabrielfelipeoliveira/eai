CREATE TABLE conversation_message_events (
    id UUID PRIMARY KEY,
    message_id UUID REFERENCES conversation_messages(id) ON DELETE SET NULL,
    external_message_id VARCHAR(160) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('RECEIVED', 'SENT', 'DELIVERED', 'READ', 'FAILED')),
    failure_reason TEXT,
    raw_payload TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_conversation_message_events_message_id ON conversation_message_events(message_id);
CREATE INDEX idx_conversation_message_events_external_message_id ON conversation_message_events(external_message_id);
CREATE INDEX idx_conversation_message_events_status ON conversation_message_events(status);
CREATE INDEX idx_conversation_message_events_occurred_at ON conversation_message_events(occurred_at DESC);
