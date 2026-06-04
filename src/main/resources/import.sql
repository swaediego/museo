-- =============================================================================
-- import.sql — Datos seed para la galería
-- Se ejecuta automáticamente al arrancar Spring Boot (spring.sql.init.mode=always)
-- Es IDEMPOTENTE: usa ON CONFLICT DO NOTHING para no romper en reinicios
-- Los INSERTs respetan el orden de foreign keys
-- =============================================================================

-- 1. CATÁLOGOS BASE (géneros) ----------------------------------------------
INSERT INTO genre (nombre) VALUES ('Pintura')         ON CONFLICT (nombre) DO NOTHING;
INSERT INTO genre (nombre) VALUES ('Escultura')       ON CONFLICT (nombre) DO NOTHING;
INSERT INTO genre (nombre) VALUES ('Fotografia')      ON CONFLICT (nombre) DO NOTHING;
INSERT INTO genre (nombre) VALUES ('Orfebreria')      ON CONFLICT (nombre) DO NOTHING;
INSERT INTO genre (nombre) VALUES ('Ceramica')        ON CONFLICT (nombre) DO NOTHING;

-- 2. CARGOS DEL MUSEO -----------------------------------------------------
INSERT INTO cargo (nombre, descripcion) VALUES ('Director General', 'Máxima autoridad del museo')                       ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Subdirector', 'Segundo al mando')                                       ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Curador Principal', 'Responsable de colecciones y exhibiciones')        ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Curador Asistente', 'Apoyo al curador principal')                     ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Conservador', 'Preserva y mantiene las obras')                         ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Coordinador de Exhibiciones', 'Organiza exposiciones temporales')     ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Gerente de Ventas', 'Gestiona ventas y membresías')                    ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Coordinador de Educación', 'Programas educativos y visitas guiadas')  ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Administrador de Sistemas', 'Soporte técnico')                        ON CONFLICT (nombre) DO NOTHING;
INSERT INTO cargo (nombre, descripcion) VALUES ('Coordinador de Relaciones Públicas', 'Comunicación y medios')         ON CONFLICT (nombre) DO NOTHING;

-- 3. PREGUNTAS DE SEGURIDAD -----------------------------------------------
INSERT INTO security_question (pregunta) VALUES ('¿Nombre de tu primera mascota?')  ON CONFLICT DO NOTHING;
INSERT INTO security_question (pregunta) VALUES ('¿Ciudad de nacimiento de tu madre?') ON CONFLICT DO NOTHING;
INSERT INTO security_question (pregunta) VALUES ('¿Nombre de tu escuela primaria?')   ON CONFLICT DO NOTHING;

-- 4. ARTISTAS ------------------------------------------------------------
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES
    ('Vincent van Gogh',  'Pintor postimpresionista.',                              'Neerlandes',     '1853-03-30', 8.50),
    ('Leonardo da Vinci', 'Artista del Renacimiento italiano.',                    'Italiano',       '1452-04-15', 9.50),
    ('Auguste Rodin',     'Escultor francés, padre de la escultura moderna.',      'Francés',        '1840-11-12', 9.00),
    ('Ansel Adams',       'Fotógrafo y ambientalista estadounidense.',            'Estadounidense', '1902-02-20', 7.50)
ON CONFLICT DO NOTHING;

-- 5. OBRAS DE ARTE (asumiendo artistas y géneros ya cargados) -------------
-- Obra 1: La Noche Estrellada (Pintura, van Gogh)
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero)
SELECT 'La Noche Estrellada', 1500.00, 1889, 'Disponible', 'https://link-a-imagen.com/noche.jpg',
       (SELECT id FROM artist WHERE nombre = 'Vincent van Gogh'),
       (SELECT id FROM genre WHERE nombre = 'Pintura')
WHERE NOT EXISTS (SELECT 1 FROM art WHERE nombre = 'La Noche Estrellada');

INSERT INTO painting (id_obra, tecnica, estilo)
SELECT id, 'Oleo', 'Postimpresionismo' FROM art WHERE nombre = 'La Noche Estrellada'
ON CONFLICT (id_obra) DO NOTHING;

-- Obra 2: Mona Lisa (Pintura, da Vinci)
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero)
SELECT 'Mona Lisa', 860000000.00, 1503, 'Disponible', 'https://example.com/monalisa.jpg',
       (SELECT id FROM artist WHERE nombre = 'Leonardo da Vinci'),
       (SELECT id FROM genre WHERE nombre = 'Pintura')
WHERE NOT EXISTS (SELECT 1 FROM art WHERE nombre = 'Mona Lisa');

INSERT INTO painting (id_obra, tecnica, estilo)
SELECT id, 'Óleo sobre tabla', 'Renacimiento' FROM art WHERE nombre = 'Mona Lisa'
ON CONFLICT (id_obra) DO NOTHING;

-- Obra 3: El Pensador (Escultura, Rodin)
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero)
SELECT 'El Pensador', 750000.00, 1904, 'Disponible', 'https://example.com/pensador.jpg',
       (SELECT id FROM artist WHERE nombre = 'Auguste Rodin'),
       (SELECT id FROM genre WHERE nombre = 'Escultura')
WHERE NOT EXISTS (SELECT 1 FROM art WHERE nombre = 'El Pensador');

INSERT INTO sculpture (id_obra, material, peso)
SELECT id, 'Bronce', 650.0 FROM art WHERE nombre = 'El Pensador'
ON CONFLICT (id_obra) DO NOTHING;

-- Obra 4: Monolith, the Face of Half Dome (Fotografia, Adams)
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero)
SELECT 'Monolith, the Face of Half Dome', 50000.00, 1927, 'Disponible', 'https://example.com/monolith.jpg',
       (SELECT id FROM artist WHERE nombre = 'Ansel Adams'),
       (SELECT id FROM genre WHERE nombre = 'Fotografia')
WHERE NOT EXISTS (SELECT 1 FROM art WHERE nombre = 'Monolith, the Face of Half Dome');

-- Columnas correctas según el modelo JPA: tipo_impresion, papel, edicion
INSERT INTO photograph (id_obra, tipo_impresion, papel, edicion)
SELECT id, 'Cámara de gran formato', 'Papel de gelatina de plata', '1/1' FROM art WHERE nombre = 'Monolith, the Face of Half Dome'
ON CONFLICT (id_obra) DO NOTHING;

-- Obra 5: Los Girasoles (Pintura, van Gogh)
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero)
SELECT 'Los Girasoles', 82500000.00, 1888, 'Disponible', 'https://example.com/girasoles.jpg',
       (SELECT id FROM artist WHERE nombre = 'Vincent van Gogh'),
       (SELECT id FROM genre WHERE nombre = 'Pintura')
WHERE NOT EXISTS (SELECT 1 FROM art WHERE nombre = 'Los Girasoles');

INSERT INTO painting (id_obra, tecnica, estilo)
SELECT id, 'Óleo sobre lienzo', 'Postimpresionismo' FROM art WHERE nombre = 'Los Girasoles'
ON CONFLICT (id_obra) DO NOTHING;

-- Obra 6: El Beso (Escultura, Rodin) - Reservada
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero)
SELECT 'El Beso', 2000000.00, 1882, 'Reservada', 'https://example.com/elbeso.jpg',
       (SELECT id FROM artist WHERE nombre = 'Auguste Rodin'),
       (SELECT id FROM genre WHERE nombre = 'Escultura')
WHERE NOT EXISTS (SELECT 1 FROM art WHERE nombre = 'El Beso');

INSERT INTO sculpture (id_obra, material, peso)
SELECT id, 'Mármol', 1815.0 FROM art WHERE nombre = 'El Beso'
ON CONFLICT (id_obra) DO NOTHING;

-- 6. USUARIOS -------------------------------------------------------------
-- Comprador 1: rhixeidys01
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo)
VALUES ('rhixeidys01', 'password123', 'rhixeidys', 'Usuario', 'rhixeidys@test.com', '04141234567', TRUE)
ON CONFLICT (login) DO NOTHING;

-- Admin 1: admin_ana (Gerente de Ventas)
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo)
VALUES ('admin_ana', 'admin123', 'Ana', 'Admin', 'admin@galeria.com', '04141234567', TRUE)
ON CONFLICT (login) DO NOTHING;

-- Comprador 2: carlos_perez
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo)
VALUES ('carlos_perez', 'carlos123', 'Carlos', 'Perez', 'carlos.perez@email.com', '04129876543', TRUE)
ON CONFLICT (login) DO NOTHING;

-- Comprador 3: laura_gomez
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo)
VALUES ('laura_gomez', 'laura456', 'Laura', 'Gomez', 'laura.gomez@email.com', '04161239876', FALSE)
ON CONFLICT (login) DO NOTHING;

-- 7. TABLAS buyer / admin (vinculadas por login, no por id hardcodeado) ---
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad)
SELECT id, '4540-XXXX-XXXX-1234', FALSE, 'Ciudad Guayana, Bolivar', 'SIN_PAGAR' FROM users WHERE login = 'rhixeidys01'
ON CONFLICT (id_usuario) DO NOTHING;

INSERT INTO admin (id_usuario, id_cargo, rol)
SELECT u.id, c.id, 'PRINCIPAL' FROM users u, cargo c
WHERE u.login = 'admin_ana' AND c.nombre = 'Gerente de Ventas'
ON CONFLICT (id_usuario) DO NOTHING;

INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad)
SELECT id, '5555-XXXX-XXXX-5678', TRUE, 'Valencia, Carabobo', 'ABC123XYZ' FROM users WHERE login = 'carlos_perez'
ON CONFLICT (id_usuario) DO NOTHING;

INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad)
SELECT id, '4111-XXXX-XXXX-1111', FALSE, 'Maracay, Aragua', 'SIN_PAGAR' FROM users WHERE login = 'laura_gomez'
ON CONFLICT (id_usuario) DO NOTHING;

-- 8. RESPUESTAS DE SEGURIDAD ----------------------------------------------
-- rhixeidys01
INSERT INTO user_answers (user_id, question_id, respuesta)
SELECT u.id, q.id, 'Bobby' FROM users u, security_question q
WHERE u.login = 'rhixeidys01' AND q.pregunta = '¿Nombre de tu primera mascota?'
AND NOT EXISTS (SELECT 1 FROM user_answers WHERE user_id = u.id AND question_id = q.id);

INSERT INTO user_answers (user_id, question_id, respuesta)
SELECT u.id, q.id, 'Caracas' FROM users u, security_question q
WHERE u.login = 'rhixeidys01' AND q.pregunta = '¿Ciudad de nacimiento de tu madre?'
AND NOT EXISTS (SELECT 1 FROM user_answers WHERE user_id = u.id AND question_id = q.id);

INSERT INTO user_answers (user_id, question_id, respuesta)
SELECT u.id, q.id, 'Escuela Bolivar' FROM users u, security_question q
WHERE u.login = 'rhixeidys01' AND q.pregunta = '¿Nombre de tu escuela primaria?'
AND NOT EXISTS (SELECT 1 FROM user_answers WHERE user_id = u.id AND question_id = q.id);

-- admin_ana
INSERT INTO user_answers (user_id, question_id, respuesta)
SELECT u.id, q.id, 'Max' FROM users u, security_question q
WHERE u.login = 'admin_ana' AND q.pregunta = '¿Nombre de tu primera mascota?'
AND NOT EXISTS (SELECT 1 FROM user_answers WHERE user_id = u.id AND question_id = q.id);

INSERT INTO user_answers (user_id, question_id, respuesta)
SELECT u.id, q.id, 'Valencia' FROM users u, security_question q
WHERE u.login = 'admin_ana' AND q.pregunta = '¿Ciudad de nacimiento de tu madre?'
AND NOT EXISTS (SELECT 1 FROM user_answers WHERE user_id = u.id AND question_id = q.id);

INSERT INTO user_answers (user_id, question_id, respuesta)
SELECT u.id, q.id, 'U.E. Humboldt' FROM users u, security_question q
WHERE u.login = 'admin_ana' AND q.pregunta = '¿Nombre de tu escuela primaria?'
AND NOT EXISTS (SELECT 1 FROM user_answers WHERE user_id = u.id AND question_id = q.id);
