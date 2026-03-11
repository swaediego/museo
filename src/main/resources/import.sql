-- 1. CATÁLOGOS BASE
INSERT INTO genre (nombre) VALUES ('Pintura');
INSERT INTO genre (nombre) VALUES ('Escultura');
INSERT INTO genre (nombre) VALUES ('Fotografia');
INSERT INTO genre (nombre) VALUES ('Orfebreria');
INSERT INTO genre (nombre) VALUES ('Ceramica');

INSERT INTO security_question (pregunta) VALUES ('¿Nombre de tu primera mascota?');
INSERT INTO security_question (pregunta) VALUES ('¿Ciudad de nacimiento de tu madre?');
INSERT INTO security_question (pregunta) VALUES ('¿Nombre de tu escuela primaria?');

-- 2. ARTISTA Y OBRA (ID Artista: 1, ID Obra: 1)
INSERT INTO artist (nombre, biografia, nacionalidad, fecha_nacimiento, porcentaje_ganancia) VALUES ('Vincent van Gogh', 'Pintor postimpresionista.', 'Neerlandés', '1853-03-30', 8.50);
INSERT INTO art (nombre, precio_base, fecha_creacion, estatus, imagen_url, id_artista, id_genero) VALUES ('La Noche Estrellada', 1500.00, '1889', 'Disponible', 'https://link-a-imagen.com/noche.jpg', 1, 1);
INSERT INTO painting (id_obra, tecnica, estilo) VALUES (1, 'Óleo', 'Postimpresionismo');

-- 3. USUARIO RHIXEIDYS (ID Usuario: 1) Y USUARIO CARLOS PEREZ (ID Uusario: 2)
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('rhixeidys01', 'password123', 'rhixeidys', 'Usuario', 'rhixeidys@test.com', '04141234567', TRUE);
INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('carlos_arte', 'password123', 'Carlos', 'Perez', 'carlos@test.com', '04149876543', TRUE);

-- 4. RHIXEIDYS COMO COMPRADOR (Relacionado al ID 1) con la membresia no pagada

INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (1, '4540-XXXX-XXXX-1234', FALSE, 'Ciudad Guayana, Bolivar', 'SIN_PAGAR');
-- CARLOS COMO COMPRADOR (Relacionado al ID 2) con la membresia pagada
INSERT INTO buyer (id_usuario, datos_tarjeta_mask, membresia_paga, direccion_envio, codigo_seguridad) VALUES (2, '5500-XXXX-XXXX-9999', TRUE, 'Puerto Ordaz, Bolivar', 'ABC123XYZ9');

-- 5. RESPUESTAS DE SEGURIDAD PARA RHIXEIDYS (Relacionadas al user_id: 1)
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 1, 'Bobby');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 2, 'Caracas');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (1, 3, 'Escuela Bolivar');
-- RESPUESTAS DE SEGURIDAD PARA CARLOS (Relacionadas al user_id: 2)
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 1, 'Max');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 2, 'Valencia');
INSERT INTO user_answers (user_id, question_id, respuesta) VALUES (2, 3, 'U.E. Humboldt');

--6. Crear un admin y asegurar que la primera obra siempre este disponible

INSERT INTO users (login, password, nombre, apellido, email, telefono, activo) VALUES ('admin_ana', 'admin123', 'Ana', 'Admin', 'admin@galeria.com', '04141234567', TRUE);
INSERT INTO admin (id_usuario, cargo) VALUES (2, 'Gerente de Ventas'); -- Asumiendo ID 2

UPDATE art SET estatus = 'Disponible' WHERE id = 1;