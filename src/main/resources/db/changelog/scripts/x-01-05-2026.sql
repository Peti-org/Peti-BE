-- liquibase formatted sql

-- changeset lyndexter:1746403200-1-order-drop-creation-time
-- comment: Drop legacy creation_time from order — replaced by created_at and updated_at.
--          Backfill created_at and updated_at from creation_time for existing rows before dropping.

-- Step 1: backfill created_at from creation_time where created_at is null (legacy orders)
UPDATE peti."order"
SET created_at = creation_time
WHERE created_at IS NULL;

-- Step 2: backfill updated_at from creation_time where updated_at is null (legacy orders)
UPDATE peti."order"
SET updated_at = creation_time
WHERE updated_at IS NULL;

-- Step 3: drop the legacy column
ALTER TABLE peti."order"
    DROP COLUMN IF EXISTS creation_time;

