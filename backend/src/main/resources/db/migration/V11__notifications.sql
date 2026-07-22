CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(60) NOT NULL CHECK (type IN ('EMAIL_ACCOUNT_FAILURE')),
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('INFO', 'WARNING', 'ERROR')),
    title VARCHAR(180) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(80),
    related_entity_id UUID,
    external_delivery_status VARCHAR(40) NOT NULL CHECK (external_delivery_status IN ('PENDING_EXTERNAL_DELIVERY', 'DELIVERED', 'FAILED')),
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_notifications_recipient_created_at ON notifications(recipient_user_id, created_at DESC);
CREATE INDEX idx_notifications_recipient_read_at ON notifications(recipient_user_id, read_at);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_related_entity ON notifications(related_entity_type, related_entity_id);
