-- liquibase formatted sql

-- changeset lyndexter:1737849600-1-create-caretaker-rrule-table
-- comment: Create table for storing caretaker recurrence rules (RRule) for availability patterns

CREATE TABLE peti.caretaker_rrule
(
  rrule_id     UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
  caretaker_id UUID        NOT NULL,
  rrule        VARCHAR(500) NOT NULL,
  dtstart      TIMESTAMP   NOT NULL,
  dtend        TIMESTAMP,
  description  VARCHAR(255),
  slot_type    VARCHAR(50) NOT NULL DEFAULT 'STANDARD',
  created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_caretaker_rrule_caretaker
    FOREIGN KEY (caretaker_id)
      REFERENCES peti.caretaker (caretaker_id)
      ON DELETE CASCADE
);

-- Create index for faster lookups by caretaker
CREATE INDEX idx_caretaker_rrule_caretaker_id ON peti.caretaker_rrule (caretaker_id);

-- changeset lyndexter:1737849600-2-insert-mock-rrule-data
-- comment: Insert mock data for caretaker RRules

INSERT INTO peti.caretaker_rrule (rrule_id, caretaker_id, rrule, dtstart, dtend, description, slot_type, created_at)
VALUES
  -- Olena Kovalenko (c1d2e3f4-a5b6-7890-1234-567890abcdef) - Weekends availability
  ('11111111-1111-1111-1111-111111111111', 'c1d2e3f4-a5b6-7890-1234-567890abcdef',
   'FREQ=WEEKLY;BYDAY=SA,SU',
   '2026-01-01 09:00:00',
   '2026-12-31 18:00:00',
   'Available on weekends 9am-6pm',
   'STANDARD',
   '2026-01-01 08:00:00'),

  -- Serhiy Melnyk (d2e3f4a5-b6c7-8901-2345-67890abcdef0) - Weekdays availability
  ('22222222-2222-2222-2222-222222222222', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0',
   'FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR',
   '2026-01-01 09:00:00',
   '2026-12-31 18:00:00',
   'Weekdays 9am-6pm',
   'STANDARD',
   '2026-01-01 08:00:00'),

  -- Serhiy Melnyk (d2e3f4a5-b6c7-8901-2345-67890abcdef0) - Evening hours
  ('33333333-3333-3333-3333-333333333333', 'd2e3f4a5-b6c7-8901-2345-67890abcdef0',
   'FREQ=WEEKLY;BYDAY=MO,WE,FR',
   '2026-01-01 18:00:00',
   '2026-12-31 21:00:00',
   'Evening availability Mon/Wed/Fri 6pm-9pm',
   'PREMIUM',
   '2026-01-01 08:00:00'),

  -- Alice Smith (a1b2c3d4-e5f6-7890-1234-567890abcd1f) - Evenings
  ('44444444-4444-4444-4444-444444444444', 'a1b2c3d4-e5f6-7890-1234-567890abcd1f',
   'FREQ=WEEKLY;BYDAY=TU,TH',
   '2026-01-01 17:00:00',
   '2026-06-30 22:00:00',
   'Tuesday and Thursday evenings',
   'STANDARD',
   '2026-01-01 08:00:00'),

  -- Bob Johnson (c3d4e5f6-a7b8-9012-3456-7890abcde491) - Weekend mornings
  ('55555555-5555-5555-5555-555555555555', 'c3d4e5f6-a7b8-9012-3456-7890abcde491',
   'FREQ=WEEKLY;BYDAY=SA,SU',
   '2026-01-01 08:00:00',
   NULL,
   'Weekend mornings from 8am',
   'STANDARD',
   '2026-01-01 08:00:00'),

  -- Carol Williams (c3d4e5f6-a7b8-9012-3456-7890abcde691) - Daily mornings
  ('66666666-6666-6666-6666-666666666666', 'c3d4e5f6-a7b8-9012-3456-7890abcde691',
   'FREQ=DAILY',
   '2026-01-01 07:00:00',
   '2026-12-31 12:00:00',
   'Available every morning 7am-12pm',
   'STANDARD',
   '2026-01-01 08:00:00');

