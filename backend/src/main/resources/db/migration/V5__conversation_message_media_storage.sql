ALTER TABLE conversation_messages ADD COLUMN media_storage_provider VARCHAR(50);
ALTER TABLE conversation_messages ADD COLUMN media_storage_key VARCHAR(500);
ALTER TABLE conversation_messages ADD COLUMN media_file_name VARCHAR(255);
ALTER TABLE conversation_messages ADD COLUMN media_size_bytes BIGINT;
ALTER TABLE conversation_messages ADD COLUMN media_sha256 VARCHAR(128);

CREATE INDEX idx_conversation_messages_media_storage
    ON conversation_messages (media_storage_provider, media_storage_key);
