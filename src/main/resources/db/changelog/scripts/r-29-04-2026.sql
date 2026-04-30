-- liquibase formatted sql

-- changeset lyndexter:1745856000-7-alter-article-summary
-- comment: Increase article summary length from 1000 to 3000
ALTER TABLE peti.article ALTER COLUMN summary TYPE VARCHAR(3000);
