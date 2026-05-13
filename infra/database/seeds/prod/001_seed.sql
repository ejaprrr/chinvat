INSERT INTO rbac_role (role_name)
VALUES
	('USER'),
	('ADMIN'),
	('SUPERADMIN')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO rbac_permission (permission_code, description)
VALUES
	('PROFILE:READ', 'Read profile details'),
	('PROFILE:WRITE', 'Update profile details'),
	('USERS:MANAGE', 'Manage users'),
	('RBAC:MANAGE', 'Manage RBAC model'),
	('AUTH:MANAGE', 'Manage authentication flows')
ON CONFLICT (permission_code) DO UPDATE
SET description = EXCLUDED.description;

INSERT INTO rbac_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM rbac_role r
JOIN rbac_permission p
  ON p.permission_code = ANY (
      CASE r.role_name
				WHEN 'USER' THEN ARRAY['PROFILE:READ', 'PROFILE:WRITE']
        WHEN 'ADMIN' THEN ARRAY['PROFILE:READ', 'PROFILE:WRITE', 'USERS:MANAGE']
        WHEN 'SUPERADMIN' THEN ARRAY['PROFILE:READ', 'PROFILE:WRITE', 'USERS:MANAGE', 'RBAC:MANAGE', 'AUTH:MANAGE']
        ELSE ARRAY[]::VARCHAR[]
      END
  )
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO auth_audit_event (event_type, details)
SELECT 'PROD_SEED_COMPLETED', jsonb_build_object('seed', '001_seed.sql')
WHERE NOT EXISTS (
	SELECT 1
	FROM auth_audit_event
	WHERE event_type = 'PROD_SEED_COMPLETED'
);

