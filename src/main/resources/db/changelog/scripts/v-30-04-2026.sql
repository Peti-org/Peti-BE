-- liquibase formatted sql

-- changeset lyndexter:1746201600-1-drop-event-slots-table
-- comment: Remove event_slots join table — events now reference a CaretakerRRule directly
DROP TABLE IF EXISTS peti.event_slots;

-- changeset lyndexter:1746201600-2-add-rrule-id-to-event
-- comment: Add rrule_id FK to event linking each event to its source CaretakerRRule
ALTER TABLE peti.event ADD COLUMN rrule_id UUID;
ALTER TABLE peti.event
  ADD CONSTRAINT event_rrule_fk
    FOREIGN KEY (rrule_id) REFERENCES peti.caretaker_rrule (rrule_id);
CREATE INDEX idx_event_rrule_id ON peti.event (rrule_id);

-- changeset lyndexter:1746201600-3-drop-event-is-deleted
-- comment: Drop event_is_deleted column — soft-delete is represented by status=DELETED
ALTER TABLE peti.event DROP COLUMN IF EXISTS event_is_deleted;

-- changeset lyndexter:1746201600-4-add-price-to-caretaker-rrule
-- comment: Add price and currency to CaretakerRRule (event price is derived from these)
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN IF NOT EXISTS price NUMERIC(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN IF NOT EXISTS currency VARCHAR(5) NOT NULL DEFAULT 'UAH';

-- changeset lyndexter:1746201600-5-event-status-index
-- comment: Index status for fast list queries that exclude DELETED rows
CREATE INDEX IF NOT EXISTS idx_event_status ON peti.event (status);
CREATE INDEX IF NOT EXISTS idx_event_caretaker_id ON peti.event (caretaker_id);
CREATE INDEX IF NOT EXISTS idx_event_user_id ON peti.event (user_id);
CREATE INDEX IF NOT EXISTS idx_event_dt_from ON peti.event (datetime_from);
CREATE INDEX IF NOT EXISTS idx_event_dt_to ON peti.event (datetime_to);

