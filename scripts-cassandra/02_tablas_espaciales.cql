-- =============================================================================
-- Sprint 2 — Tablas Espaciales: Ubicación de Obras y Salas del Museo
-- Cassandra no tiene índices geoespaciales nativos (como PostGIS).
-- Modelamos coordenadas como doubles y ubicaciones como texto estructurado.
-- =============================================================================

-- =============================================================================
-- 1. SALAS DEL MUSEO
 -- Query: "Listar todas las salas del museo con su capacidad y ubicación"
-- Partition Key: id_sala
-- Almacena metadatos de cada sala: nombre, piso, coordenadas geográficas
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.salas_museo (
    id_sala         int,
    nombre          text,
    piso            int,
    ala             text,               -- Ej: "Norte", "Sur", "Este", "Oeste"
    capacidad_max   int,
    latitud         double,             -- Coordenada geográfica
    longitud        double,             -- Coordenada geográfica
    descripcion     text,
    PRIMARY KEY (id_sala)
);

-- =============================================================================
-- 2. UBICACIÓN DE OBRAS EN SALAS
-- Query: "¿En qué sala está actualmente una obra?" / "¿Qué obras hay en una sala?"
-- Partition Key: id_sala → todas las obras en una sala juntas
-- Clustering: id_relacional DESC
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.ubicacion_obras_por_sala (
    id_sala         int,
    id_relacional   int,
    nombre_obra     text,
    nombre_artista  text,
    fecha_ingreso   timestamp,
    fecha_salida    timestamp,          -- NULL si aún está en la sala
    posicion_estante text,              -- Ej: "Estante B, Nivel 3"
    PRIMARY KEY (id_sala, id_relacional)
) WITH CLUSTERING ORDER BY (id_relacional DESC);

-- =============================================================================
-- 3. HISTORIAL DE UBICACIÓN DE OBRAS (trazabilidad)
-- Query: "¿Dónde ha estado esta obra a lo largo del tiempo?"
-- Partition Key: id_relacional → toda la historia de ubicación de una obra
-- Clustering: fecha_ingreso DESC → más reciente primero
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.historial_ubicacion_obras (
    id_relacional   int,
    fecha_ingreso   timestamp,
    id_sala         int,
    nombre_sala     text,
    posicion_estante text,
    PRIMARY KEY (id_relacional, fecha_ingreso)
) WITH CLUSTERING ORDER BY (fecha_ingreso DESC);

-- =============================================================================
-- 4. EXHIBICIONES TEMPORALES
-- Query: "Mostrar todas las exhibiciones activas en una fecha determinada"
-- Partition Key: (id_sala, fecha_inicio) → obras exhibidas juntas por sala+fecha
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.exhibiciones_temporales (
    id_sala         int,
    fecha_inicio    timestamp,
    id_exhibicion   uuid,
    nombre          text,
    descripcion     text,
    fecha_fin       timestamp,
    obras_incluidas frozen<list<int>>,  -- Lista de id_relacional de las obras
    curador         text,
    PRIMARY KEY ((id_sala, fecha_inicio), id_exhibicion)
) WITH CLUSTERING ORDER BY (id_exhibicion ASC);

-- =============================================================================
-- 5. ALMACENAMIENTO (Bodega / Depósito)
-- Query: "Listar obras en almacenamiento (no exhibidas)"
-- Partition Key: id_zona_almacen
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.almacenamiento_obras (
    id_zona_almacen int,
    id_relacional   int,
    nombre_obra     text,
    categoria       text,
    fecha_ingreso   timestamp,
    condiciones     text,               -- Ej: "Clima controlado 20°C, 45% HR"
    posicion        text,               -- Ej: "Pasillo C, Estante 12, Lote 3"
    PRIMARY KEY (id_zona_almacen, id_relacional)
) WITH CLUSTERING ORDER BY (id_relacional ASC);
