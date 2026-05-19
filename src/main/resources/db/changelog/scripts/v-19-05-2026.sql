--liquibase formatted sql
--changeset peti:v-19-05-2026-drop-event-rrule-id

-- Remove rrule_id foreign key and column from event table
DROP INDEX IF EXISTS peti.idx_event_rrule_id;
ALTER TABLE peti.event DROP CONSTRAINT IF EXISTS event_rrule_id_fkey;
ALTER TABLE peti.event DROP COLUMN IF EXISTS rrule_id;

