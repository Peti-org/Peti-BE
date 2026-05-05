-- liquibase formatted sql

-- changeset lyndexter:1746115200-1-event-status-uppercase
-- comment: Normalize existing event status values to uppercase enum form
UPDATE peti.event SET status = 'CREATED' WHERE LOWER(status) = 'created';
UPDATE peti.event SET status = 'APPROVED' WHERE LOWER(status) = 'approved';
UPDATE peti.event SET status = 'CANCELLED' WHERE LOWER(status) IN ('cancelled', 'canceled');

-- changeset lyndexter:1746115200-2-event-status-length
-- comment: Ensure event.status column is wide enough for enum names
ALTER TABLE peti.event ALTER COLUMN status TYPE VARCHAR(20);

-- changeset lyndexter:1746115200-3-order-status-default
-- comment: Change default order status from PRE_RESERVED to RESERVED (PRE_RESERVED no longer used)
ALTER TABLE peti."order" ALTER COLUMN status SET DEFAULT 'RESERVED';
UPDATE peti."order" SET status = 'RESERVED' WHERE status = 'PRE_RESERVED';

