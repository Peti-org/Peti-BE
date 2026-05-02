-- liquibase formatted sql

-- changeset lyndexter:1746028800-1-alter-order-table
-- comment: Add status, created_at, updated_at, is_deleted columns to order; drop order_is_deleted
ALTER TABLE peti."order" ADD COLUMN status VARCHAR(30) NOT NULL DEFAULT 'PRE_RESERVED';
ALTER TABLE peti."order" ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE peti."order" ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE peti."order" ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE peti."order" DROP COLUMN IF EXISTS order_is_deleted;

-- changeset lyndexter:1746028800-2-alter-order-modification-table
-- comment: Rebuild order_modification with UUID PK, status, actor_id, comment columns
DROP TABLE IF EXISTS peti.order_modification;
CREATE TABLE peti.order_modification
(
    modification_id UUID        NOT NULL DEFAULT gen_random_uuid(),
    order_id        UUID        NOT NULL,
    status          VARCHAR(30) NOT NULL,
    created_at      TIMESTAMP   NOT NULL DEFAULT NOW(),
    actor_id        UUID        NOT NULL,
    comment         VARCHAR(500),
    CONSTRAINT order_modification_pk PRIMARY KEY (modification_id),
    CONSTRAINT order_modification_order_fk
        FOREIGN KEY (order_id) REFERENCES peti."order" (order_id) ON DELETE CASCADE
);

-- changeset lyndexter:1746028800-3-add-order-indexes
-- comment: Add indexes for order queries under high load
CREATE INDEX idx_order_client_id ON peti."order" (client_id);
CREATE INDEX idx_order_caretaker_id ON peti."order" (caretaker_id);
CREATE INDEX idx_order_event_id ON peti."order" (event_id);
CREATE INDEX idx_order_status ON peti."order" (status);
CREATE INDEX idx_order_created_at ON peti."order" (created_at DESC);
CREATE INDEX idx_order_mod_order_id ON peti.order_modification (order_id);
CREATE INDEX idx_order_mod_created_at ON peti.order_modification (created_at);

-- changeset lyndexter:1746028800-5-add-unique-event-per-order splitStatements:false
-- comment: Ensure one active order per event (partial unique index, skip if duplicates exist)
DO $$
BEGIN
  -- Remove failed changeset record if exists
  DELETE FROM peti.databasechangelog
    WHERE id = '1746028800-4-add-unique-event-per-order';
  -- Only create if no duplicates and index doesn't exist
  IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_order_event_unique') THEN
    IF NOT EXISTS (
      SELECT event_id FROM peti."order" WHERE is_deleted = FALSE
      GROUP BY event_id HAVING COUNT(*) > 1
    ) THEN
      CREATE UNIQUE INDEX idx_order_event_unique ON peti."order" (event_id) WHERE is_deleted = FALSE;
    END IF;
  END IF;
END $$;

