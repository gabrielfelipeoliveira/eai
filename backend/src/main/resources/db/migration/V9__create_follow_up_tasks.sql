CREATE TABLE follow_up_tasks (
    id UUID PRIMARY KEY,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(160) NOT NULL,
    description TEXT,
    due_at TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT follow_up_tasks_status_check CHECK (status IN ('PENDING', 'DONE', 'CANCELED', 'OVERDUE'))
);

CREATE INDEX idx_follow_up_tasks_lead_id ON follow_up_tasks(lead_id);
CREATE INDEX idx_follow_up_tasks_user_id_status_due_at ON follow_up_tasks(user_id, status, due_at);
CREATE INDEX idx_follow_up_tasks_due_at ON follow_up_tasks(due_at);
