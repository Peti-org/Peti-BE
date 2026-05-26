-- liquibase formatted sql

-- changeset lyndexter:1747612800-1-add-duration-to-caretaker-rrule
-- comment: Add duration column to caretaker_rrule — stores slot duration as INTERVAL (mapped to java.time.Duration)
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN IF NOT EXISTS duration INTERVAL NOT NULL DEFAULT INTERVAL '1 hour';

-- changeset lyndexter:1747612800-2-alter-dtstart-to-time
-- comment: Change dtstart from TIMESTAMP to TIME — entity maps it as LocalTime
ALTER TABLE peti.caretaker_rrule
  ALTER COLUMN dtstart TYPE TIME USING dtstart::TIME;

-- changeset lyndexter:1747612800-3-add-updated-at-to-caretaker-rrule
-- comment: Add updated_at column to caretaker_rrule for audit tracking
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- changeset lyndexter:1747612800-4-drop-dtend-from-caretaker-rrule
-- comment: Drop dtend column — no longer used, duration replaces the end time concept
ALTER TABLE peti.caretaker_rrule
  DROP COLUMN IF EXISTS dtend;

