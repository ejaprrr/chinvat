INSERT INTO auth_audit_event (event_type, details)
VALUES ('PROD_SEED_COMPLETED', jsonb_build_object('seed', '001_seed.sql'));

