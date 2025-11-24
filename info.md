### Enlaces importantes:
 - Google OAUTH: https://console.cloud.google.com/auth/clients/287224420958-mn5u1l0v7kbhn5n8o6vb8n48t5037hhc.apps.googleusercontent.com?project=petcare-478810
 - GitHub OAUTH: https://github.com/settings/applications/3258548

### Color usado:
#438a56

### AÑADIR Y BORRAR ROLES A USUARIOS
INSERT INTO user_roles (user_id, role) VALUES (user_id_value, 'ROLE_ADMIN');

DELETE FROM user_roles WHERE user_id = user_id_value AND role = 'ROLE_ADMIN';