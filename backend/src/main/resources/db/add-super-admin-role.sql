-- Platform super-admin role (used for JWT; login is config-based, not stored in tenant DB)
INSERT INTO role (role_name, description)
SELECT 'SUPER_ADMIN', 'Platform operator — manage tenant client accounts'
WHERE NOT EXISTS (SELECT 1 FROM role WHERE role_name = 'SUPER_ADMIN');
