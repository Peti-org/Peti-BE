-- liquibase formatted sql

-- changeset lyndexter:1748822400-1-rename-capacity-to-pet-capacity
-- comment: Rename caretaker_rrule.capacity to pet_capacity to distinguish it from people_capacity
ALTER TABLE peti.caretaker_rrule
  RENAME COLUMN capacity TO pet_capacity;

-- changeset lyndexter:1748822400-2-add-people-capacity-to-caretaker-rrule
-- comment: Add people_capacity column to caretaker_rrule (default 20 — generous, behavior-neutral)
ALTER TABLE peti.caretaker_rrule
  ADD COLUMN IF NOT EXISTS people_capacity INTEGER NOT NULL DEFAULT 20;

-- changeset lyndexter:1748822400-3-drop-interval-minutes-from-caretaker-rrule
-- comment: Drop interval_minutes — slot generation is fully driven by slot_duration (duration column)
ALTER TABLE peti.caretaker_rrule
  DROP COLUMN IF EXISTS interval_minutes;

