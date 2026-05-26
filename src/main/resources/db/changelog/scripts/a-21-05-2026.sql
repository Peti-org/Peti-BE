--liquibase formatted sql

--changeset taras:move-generated-to-from-rrule-to-caretaker
--comment: Move generated_to from caretaker_rrule to caretaker table

ALTER TABLE peti.caretaker
  ADD COLUMN generated_to DATE;

DROP INDEX IF EXISTS peti.idx_rrule_generated_to;

ALTER TABLE peti.caretaker_rrule
  DROP COLUMN IF EXISTS generated_to;

