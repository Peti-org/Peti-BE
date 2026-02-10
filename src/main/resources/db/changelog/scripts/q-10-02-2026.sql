-- liquibase formatted sql

-- changeset lyndexter:1737849600-4-add-rrule-to-slot
-- comment: Add capacity and occupied_capacity to caretaker_slot table
ALTER TABLE peti.caretaker_slot
  ADD COLUMN is_repeated       BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN rrule_id          UUID,
  ADD CONSTRAINT fk_slot_rrule FOREIGN KEY (rrule_id) REFERENCES peti.caretaker_rrule (rrule_id) ON DELETE SET NULL;


-- changeset lyndexter:1737849600-5-modify-event-table
-- comment: Add capacity, interval_minutes, and generated_to fields to caretaker_rrule table
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN capacity         INTEGER NOT NULL DEFAULT 1,
  ADD COLUMN interval_minutes INTEGER NOT NULL DEFAULT 30,
  ADD COLUMN generated_to     DATE;

-- Create index for efficient scheduler queries
CREATE INDEX idx_rrule_generated_to ON peti.caretaker_rrule(caretaker_id, generated_to);

