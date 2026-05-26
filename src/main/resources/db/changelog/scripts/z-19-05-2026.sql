-- liquibase formatted sql

-- changeset lyndexter:1747688400-1-alter-duration-to-bigint
-- comment: Change duration column from INTERVAL to BIGINT (storing minutes) for Hibernate compatibility
ALTER TABLE peti.caretaker_rrule
  ALTER COLUMN duration DROP DEFAULT;

ALTER TABLE peti.caretaker_rrule
  ALTER COLUMN duration TYPE BIGINT
  USING EXTRACT(EPOCH FROM duration) / 60;

