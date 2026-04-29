INSERT INTO rbac_role (role_name, permissions_csv)
VALUES
	('USER', 'PROFILE:READ'),
	('ADMIN', 'PROFILE:READ,PROFILE:WRITE,USERS:MANAGE'),
	('SUPERADMIN', 'PROFILE:READ,PROFILE:WRITE,USERS:MANAGE,RBAC:MANAGE,AUTH:MANAGE')
ON CONFLICT (role_name) DO UPDATE
SET permissions_csv = EXCLUDED.permissions_csv;

INSERT INTO auth_audit_event (event_type, details)
SELECT 'PROD_SEED_COMPLETED', jsonb_build_object('seed', '001_seed.sql')
WHERE NOT EXISTS (
	SELECT 1
	FROM auth_audit_event
	WHERE event_type = 'PROD_SEED_COMPLETED'
);

