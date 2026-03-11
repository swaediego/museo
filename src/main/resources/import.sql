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
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES ('Vincent van Gogh', 'Pintor postimpresionista.', 'Neerlandés', '1853-03-30', 8.50);
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('La Noche Estrellada', 1500.00, 1889, 'Disponible', 'https://link-a-imagen.com/noche.jpg', 1, 1);
INSERT INTO painting (id_obra, tecnica, estilo) VALUES (1, 'Óleo', 'Postimpresionismo');

-- 3. USUARIOS
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('rhixeidys01', 'password123', 'rhixeidys', 'Usuario', 'rhixeidys@test.com', '04141234567', TRUE);
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('carlos_arte', 'password123', 'Carlos', 'Perez', 'carlos@test.com', '04149876543', TRUE);
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('admin_ana', 'admin123', 'Ana', 'Admin', 'admin@galeria.com', '04141234567', TRUE);

-- 4. COMPRADORES Y ADMINS
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (1, '4540-XXXX-XXXX-1234', FALSE, 'Ciudad Guayana, Bolivar', 'SIN_PAGAR');
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (2, '5500-XXXX-XXXX-9999', FALSE, 'Puerto Ordaz, Bolivar', 'SIN_PAGAR');
INSERT INTO admin (id_usuario, cargo) VALUES (3, 'Gerente de Ventas');

-- 5. RESPUESTAS
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 1, 'Bobby');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 2, 'Caracas');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 3, 'Escuela Bolivar');

INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 1, 'Max');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 2, 'Valencia');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 3, 'U.E. Humboldt');

UPDATE art SET estatus = 'Disponible' WHERE id = 1;