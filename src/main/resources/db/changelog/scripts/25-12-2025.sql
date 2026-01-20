-- liquibase formatted sql

-- changeset lyndexter:1721905000-1-add-capacity-to-slot
-- comment: Add capacity and occupied_capacity to caretaker_slot table
ALTER TABLE peti.caretaker_slot
  ADD COLUMN capacity          INTEGER NOT NULL DEFAULT 1,
  ADD COLUMN occupied_capacity INTEGER NOT NULL DEFAULT 0;

-- changeset lyndexter:1721905000-2-modify-event-table
-- comment: Drop slot_id foreign key and add datetime columns to event table
ALTER TABLE peti.event
  DROP COLUMN slot_id,
  ADD COLUMN datetime_from TIMESTAMP,
  ADD COLUMN datetime_to   TIMESTAMP,
  ADD COLUMN type          varchar(30) NOT NULL DEFAULT 'вигул';

-- changeset lyndexter:1721905000-3-create-event-slots-join-table
-- comment: Create join table for many-to-many relationship between event and slot
CREATE TABLE peti.event_slots
(
  event_id UUID NOT NULL,
  slot_id  UUID NOT NULL,
  PRIMARY KEY (event_id, slot_id),
  FOREIGN KEY (event_id) REFERENCES peti.event (event_id) ON DELETE CASCADE,
  FOREIGN KEY (slot_id) REFERENCES peti.caretaker_slot (slot_id) ON DELETE CASCADE
);


CREATE TABLE peti.event_pets
(
  event_id UUID NOT NULL,
  pet_id   UUID NOT NULL,
  PRIMARY KEY (event_id, pet_id),
  FOREIGN KEY (event_id) REFERENCES peti.event (event_id) ON DELETE CASCADE,
  FOREIGN KEY (pet_id) REFERENCES peti.pet (pet_id) ON DELETE CASCADE
);
