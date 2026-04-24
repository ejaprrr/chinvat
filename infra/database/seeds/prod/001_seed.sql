INSERT INTO auth_audit_event (event_type, details)
SELECT 'PROD_SEED_COMPLETED', jsonb_build_object('seed', '001_seed.sql')
WHERE NOT EXISTS (
	SELECT 1
	FROM auth_audit_event
	WHERE event_type = 'PROD_SEED_COMPLETED'
);

