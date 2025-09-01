-- Create schemas
CREATE SCHEMA IF NOT EXISTS planmate AUTHORIZATION postgres;
CREATE SCHEMA IF NOT EXISTS keycloak AUTHORIZATION postgres;

GRANT USAGE ON SCHEMA planmate TO postgres;
GRANT USAGE ON SCHEMA keycloak TO postgres;