-- For creation user with limited permissions

CREATE USER peti_user WITH ENCRYPTED PASSWORD '1111';

GRANT CONNECT, CREATE ON DATABASE peti TO peti_user;

GRANT USAGE ON SCHEMA peti TO peti_user;

-- Grant SELECT, INSERT, UPDATE, DELETE on all existing tables in the peti schema
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA peti TO peti_user;

-- Grant permissions on future tables that will be created in the peti schema
ALTER DEFAULT PRIVILEGES IN SCHEMA peti GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO peti_user;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA peti TO peti_user;

-- Grant permissions on future sequences that will be created in the peti schema
ALTER DEFAULT PRIVILEGES IN SCHEMA peti GRANT USAGE, SELECT ON SEQUENCES TO peti_user;

-- Grant permissions on future tables that will be created in the peti schema
-- for liquibase
GRANT CREATE ON SCHEMA peti TO peti_user;

