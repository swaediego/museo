-- 1. CATÁLOGOS BASE
INSERT INTO genre (nombre) VALUES ('Pintura');
INSERT INTO genre (nombre) VALUES ('Escultura');
INSERT INTO genre (nombre) VALUES ('Fotografia');
INSERT INTO genre (nombre) VALUES ('Orfebreria');
INSERT INTO genre (nombre) VALUES ('Ceramica');
-- CATÁLOGOS BASE (Preguntas de seguridad)
INSERT INTO security_question (pregunta) VALUES ('¿Nombre de tu primera mascota?');
INSERT INTO security_question (pregunta) VALUES ('¿Ciudad de nacimiento de tu madre?');
INSERT INTO security_question (pregunta) VALUES ('¿Nombre de tu escuela primaria?');

-- 2. ARTISTA Y OBRA
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES ('Vincent van Gogh', 'Pintor postimpresionista.', 'Neerlandes', '1853-03-30', 8.50);
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('La Noche Estrellada', 1500.00, 1889, 'Disponible', 'https://link-a-imagen.com/noche.jpg', 1, 1);
INSERT INTO painting (id_obra, tecnica, estilo) VALUES (1, 'oleo', 'Postimpresionismo');

-- 3. USUARIOS
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('rhixeidys01', 'password123', 'rhixeidys', 'Usuario', 'rhixeidys@test.com', '04141234567', TRUE);
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('admin_ana', 'admin123', 'Ana', 'Admin', 'admin@galeria.com', '04141234567', TRUE);

-- 4. COMPRADORES Y ADMINS
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (1, '4540-XXXX-XXXX-1234', FALSE, 'Ciudad Guayana, Bolivar', 'SIN_PAGAR');
INSERT INTO admin (id_usuario, cargo) VALUES (2, 'Gerente de Ventas');

-- 5. RESPUESTAS
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 1, 'Bobby');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 2, 'Caracas');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 3, 'Escuela Bolivar');

INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 1, 'Max');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 2, 'Valencia');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 3, 'U.E. Humboldt');

UPDATE art SET estatus = 'Disponible' WHERE id = 1;

-- NUEVOS DATOS
-- Artistas adicionales
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES ('Leonardo da Vinci', 'Artista del Renacimiento italiano.', 'Italiano', '1452-04-15', 12.00);
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES ('Auguste Rodin', 'Escultor francés, considerado el padre de la escultura moderna.', 'Francés', '1840-11-12', 10.00);
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES ('Ansel Adams', 'Fotógrafo y ambientalista estadounidense.', 'Estadounidense', '1902-02-20', 7.50);

-- Obras adicionales
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('Mona Lisa', 860000000.00, 1503, 'Disponible', 'https://example.com/monalisa.jpg', 2, 1);
INSERT INTO painting (id_obra, tecnica, estilo) VALUES (2, 'Óleo sobre tabla', 'Renacimiento');
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('El Pensador', 750000.00, 1904, 'Disponible', 'https://example.com/pensador.jpg', 3, 2);
INSERT INTO sculpture (id_obra, material, peso) VALUES (3, 'Bronce', 650.0);
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('Monolith, the Face of Half Dome', 50000.00, 1927, 'Disponible', 'https://example.com/monolith.jpg', 4, 3);
INSERT INTO photograph (id_obra, tipo_camara, tipo_papel) VALUES (4, 'Cámara de gran formato', 'Papel de gelatina de plata');
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('Los Girasoles', 82500000.00, 1888, 'Disponible', 'https://example.com/girasoles.jpg', 1, 1);
INSERT INTO painting (id_obra, tecnica, estilo) VALUES (5, 'Óleo sobre lienzo', 'Postimpresionismo');
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('El Beso', 2000000.00, 1882, 'Reservada', 'https://example.com/elbeso.jpg', 3, 2);
INSERT INTO sculpture (id_obra, material, peso) VALUES (6, 'Mármol', 1815.0);

-- Nuevos usuarios y compradores
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('carlos_perez', 'carlos123', 'Carlos', 'Perez', 'carlos.perez@email.com', '04129876543', TRUE);
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (3, '5555-XXXX-XXXX-5678', TRUE, 'Valencia, Carabobo', 'ABC123XYZ');
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('laura_gomez', 'laura456', 'Laura', 'Gomez', 'laura.gomez@email.com', '04161239876', FALSE);
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (4, '4111-XXXX-XXXX-1111', FALSE, 'Maracay, Aragua', 'SIN_PAGAR');
