-- liquibase formatted sql

-- changeset lyndexter:1746316800-1-drop-price-currency-from-rrule
-- comment: Remove price and currency from caretaker_rrule — event pricing now sourced from CaretakerPreferences
ALTER TABLE peti.caretaker_rrule DROP COLUMN IF EXISTS price;
ALTER TABLE peti.caretaker_rrule DROP COLUMN IF EXISTS currency;

