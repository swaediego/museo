-- 1. LIMPIEZA
DROP TABLE IF EXISTS membership_payment CASCADE;
DROP TABLE IF EXISTS invoice CASCADE;
DROP TABLE IF EXISTS orfebreria CASCADE;
DROP TABLE IF EXISTS ceramic CASCADE;
DROP TABLE IF EXISTS photograph CASCADE;
DROP TABLE IF EXISTS sculpture CASCADE;
DROP TABLE IF EXISTS painting CASCADE;
DROP TABLE IF EXISTS art CASCADE;
DROP TABLE IF EXISTS artist_genre CASCADE;
DROP TABLE IF EXISTS artist CASCADE;
DROP TABLE IF EXISTS user_answers CASCADE;
DROP TABLE IF EXISTS buyer CASCADE;
DROP TABLE IF EXISTS admin CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genre CASCADE;
DROP TABLE IF EXISTS security_question CASCADE;

-- 2. TABLAS INDEPENDIENTES (Nivel 1)
CREATE TABLE security_question (
                                   id SERIAL PRIMARY KEY,
                                   pregunta VARCHAR(255) NOT NULL
);

CREATE TABLE cargo (
                        id SERIAL PRIMARY KEY,
                        nombre VARCHAR(100) UNIQUE NOT NULL,
                        descripcion TEXT
);

CREATE TABLE genre (
                       id SERIAL PRIMARY KEY,
                       nombre VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE artist (
                        id SERIAL PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        biografia TEXT,
                        nacionalidad VARCHAR(50),
                        fecha_nacimiento DATE,
                        foto_url VARCHAR(255),
    -- El museo se queda entre 5% y 10%
                        porcentaje_ganancia DECIMAL(4,2) CHECK (porcentaje_ganancia BETWEEN 5 AND 10)
);

-- 3. USUARIOS (Nivel 2)
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       login VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       nombre VARCHAR(100) NOT NULL,
                       apellido VARCHAR(100) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       telefono VARCHAR(20),
                       fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       activo BOOLEAN DEFAULT TRUE
);

-- 4. SUBCLASES DE USUARIO Y RESPUESTAS (Nivel 3)
CREATE TABLE buyer (
                       id_usuario INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                       datos_tarjeta_mask VARCHAR(20) NOT NULL,
                       membresia_paga BOOLEAN DEFAULT FALSE,
                       direccion_envio TEXT NOT NULL,
                       codigo_seguridad VARCHAR(10) NOT NULL -- Generado aleatoriamente
);

CREATE TABLE admin (
                         id_usuario INT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                         id_cargo INT NOT NULL REFERENCES cargo(id),
                         rol VARCHAR(20) NOT NULL DEFAULT 'SECUNDARIO' CHECK (rol IN ('PRINCIPAL', 'SECUNDARIO'))
);

CREATE TABLE user_answers (
                              id SERIAL PRIMARY KEY,
                              user_id INT NOT NULL REFERENCES users(id),
                              question_id INT NOT NULL REFERENCES security_question(id),
                              respuesta VARCHAR(255) NOT NULL -- Para recuperar código
);

-- 5. OBRAS Y RELACIONES (Nivel 4)
CREATE TABLE artist_genre (
                              id SERIAL PRIMARY KEY,
                              artist_id INT NOT NULL REFERENCES artist(id),
                              genre_id INT NOT NULL REFERENCES genre(id)
);

CREATE TABLE art (
                     id SERIAL PRIMARY KEY,
                     nombre VARCHAR(150) NOT NULL,
                     precio_base DECIMAL(12,2) NOT NULL,
                     fecha_creacion INTEGER NOT NULL,
    -- Estatus: Disponible, Reservada, Vendida
                     estatus VARCHAR(20) DEFAULT 'Disponible',
                     imagen_url VARCHAR(255) NOT NULL,
                     id_artista INT NOT NULL REFERENCES artist(id),
                     id_genero INT NOT NULL REFERENCES genre(id)
);

-- 6. ESPECIALIZACIONES DE OBRA (Nivel 5)

CREATE TABLE painting (
                          id_obra INT PRIMARY KEY REFERENCES art(id) ON DELETE CASCADE,
                          tecnica VARCHAR(100) NOT NULL,
                          estilo VARCHAR(100)
);

CREATE TABLE sculpture (
                           id_obra INT PRIMARY KEY REFERENCES art(id) ON DELETE CASCADE,
                           material VARCHAR(100) NOT NULL,
                           peso DECIMAL(10,2) NOT NULL,
                           largo DECIMAL(10,2),
                           ancho DECIMAL(10,2),
                           profundidad DECIMAL(10,2)
);

CREATE TABLE photograph (
                            id_obra INT PRIMARY KEY REFERENCES art(id) ON DELETE CASCADE,
                            tipo_impresion VARCHAR(100),
                            papel VARCHAR(100),
                            edicion VARCHAR(50) -- Ejemplo: "1/10"
);

CREATE TABLE ceramic (
                         id_obra INT PRIMARY KEY REFERENCES art(id) ON DELETE CASCADE,
                         tipo_arcilla VARCHAR(100),
                         temperatura_coccion DECIMAL(6,2)
);

CREATE TABLE orfebreria (
                            id_obra INT PRIMARY KEY REFERENCES art(id) ON DELETE CASCADE,
                            pureza_metal VARCHAR(50),
                            peso DECIMAL(10,2),
                            metal_base VARCHAR(100)
);

-- 7. TRANSACCIONES FINALES (Nivel 6)
CREATE TABLE invoice (
                         id SERIAL PRIMARY KEY,
                         id_obra INT UNIQUE NOT NULL REFERENCES art(id), -- Una obra por factura
                         id_comprador INT NOT NULL REFERENCES buyer(id_usuario),
                         id_admin INT NOT NULL REFERENCES admin(id_usuario),
                         fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         subtotal DECIMAL(12,2) NOT NULL, -- Precio sin IVA
                         iva DECIMAL(12,2) NOT NULL,
                         porcentaje_ganancia DECIMAL(4,2) NOT NULL,
                         monto_ganancia DECIMAL(12,2) NOT NULL,
                         total DECIMAL(12,2) NOT NULL,
                         direccion_destino TEXT NOT NULL -- Datos de envío
);

CREATE TABLE membership_payment (
                                    id SERIAL PRIMARY KEY,
                                    id_comprador INT NOT NULL REFERENCES buyer(id_usuario),
                                    monto DECIMAL(10,2) DEFAULT 10.00, -- Costo fijo de membresía
                                    fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);