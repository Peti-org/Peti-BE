-- liquibase formatted sql

-- changeset lyndexter:1739750400-1-add-rrule-fields
-- comment: Add isEnabled, isSchedule, isBusy, and priority fields to caretaker_rrule table
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN is_enabled  BOOLEAN NOT NULL DEFAULT TRUE,
  ADD COLUMN is_schedule BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN is_busy     BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN priority    INTEGER NOT NULL DEFAULT 0;

-- Create index for efficient filtering by enabled status
CREATE INDEX idx_rrule_is_enabled ON peti.caretaker_rrule(caretaker_id, is_enabled);

-- Create index for priority-based queries
CREATE INDEX idx_rrule_priority ON peti.caretaker_rrule(caretaker_id, priority DESC);

