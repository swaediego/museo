-- =============================================================================
-- Sprint 2 — Esquema Base: Keyspace + Tablas de Históricos y Auditoría
-- Query-Driven Modeling: cada tabla está diseñada para una consulta específica
-- =============================================================================

-- 1. KEYSPACE (SimpleStrategy por ser entorno dev; en prod usar NetworkTopology)
CREATE KEYSPACE IF NOT EXISTS museo_history
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
}
AND durable_writes = true;

-- =============================================================================
-- 2. HISTORIAL DE PRECIOS POR OBRA
-- Query: "Mostrar el historial de cambios de precio de una obra específica"
-- Partition Key: id_relacional → todos los cambios de una obra juntos
-- Clustering: fecha_cambio DESC, id_evento → orden cronológico inverso
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.historial_precio_por_obra (
    id_relacional   int,
    fecha_cambio    timestamp,
    id_evento       uuid,
    precio_anterior decimal,
    precio_nuevo    decimal,
    motivo          text,
    usuario_admin   text,
    PRIMARY KEY (id_relacional, fecha_cambio, id_evento)
) WITH CLUSTERING ORDER BY (fecha_cambio DESC, id_evento ASC);

-- =============================================================================
-- 3. AUDITORÍA DE VENTAS POR PERÍODO
-- Query: "Generar resumen de facturación mensual entre dos fechas"
-- Partition Key: periodo (yyyy-MM) → todas las ventas de un mes juntas
-- Clustering: fecha_venta DESC, id_factura ASC → más reciente primero
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.auditoria_ventas_por_periodo (
    periodo         text,
    fecha_venta     timestamp,
    id_factura      int,
    id_obra         int,
    id_comprador    int,
    monto           decimal,
    impuesto        decimal,
    metodo_pago     text,
    estatus_factura text,
    PRIMARY KEY (periodo, fecha_venta, id_factura)
) WITH CLUSTERING ORDER BY (fecha_venta DESC, id_factura ASC);

-- =============================================================================
-- 4. BITÁCORA DE EVENTOS (inmutable) — Todos los eventos de un día
-- Query: "Mostrar todos los eventos ocurridos en una fecha"
-- Partition Key: periodo_dia (yyyy-MM-dd)
-- Clustering: timestamp_evento DESC → ordenados del más reciente al más antiguo
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.bitacora_eventos (
    periodo_dia     text,
    timestamp_evento timestamp,
    tipo_evento     text,
    id_entidad      int,
    tipo_entidad    text,
    detalle_json    text,
    severidad       text,
    usuario_origen  text,
    PRIMARY KEY (periodo_dia, timestamp_evento)
) WITH CLUSTERING ORDER BY (timestamp_evento DESC);

-- =============================================================================
-- 5. BITÁCORA POR TIPO DE EVENTO — Para filtrar eventos por tipo en CQL
-- Query: "Mostrar eventos de tipo VENTA_REGISTRADA en un día específico"
-- Partition Key compuesta: (periodo_dia, tipo_evento)
-- Clustering: timestamp_evento DESC
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.bitacora_por_tipo (
    periodo_dia     text,
    tipo_evento     text,
    timestamp_evento timestamp,
    id_entidad      int,
    tipo_entidad    text,
    detalle_json    text,
    severidad       text,
    usuario_origen  text,
    PRIMARY KEY ((periodo_dia, tipo_evento), timestamp_evento)
) WITH CLUSTERING ORDER BY (timestamp_evento DESC);

-- =============================================================================
-- 5. VENTAS POR OBRA (Agregación gerencial)
-- Query: "¿Cuál es la obra más vendida? / Total recaudado por obra"
-- Partition Key: id_relacional → toda la actividad de venta de una obra
-- Permite sumar montos y contar transacciones por obra
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.ventas_por_obra (
    id_relacional   int,
    fecha_venta     timestamp,
    id_factura      int,
    monto           decimal,
    impuesto        decimal,
    id_comprador    int,
    PRIMARY KEY (id_relacional, fecha_venta, id_factura)
) WITH CLUSTERING ORDER BY (fecha_venta DESC, id_factura ASC);

-- =============================================================================
-- 6. INGRESOS POR CATEGORÍA (Agregación gerencial)
-- Query: "Generar reporte de ingresos agrupados por género / artista / período"
-- Partition Key: categoria (ej: "Pintura", "Escultura") o "ARTISTA:Van Gogh"
-- Permite consultas del tipo "cuánto generó la Pintura en 2024-01"
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.ingresos_por_categoria (
    categoria       text,
    periodo         text,
    id_factura      int,
    id_obra         int,
    monto           decimal,
    impuesto        decimal,
    PRIMARY KEY ((categoria, periodo), id_factura)
) WITH CLUSTERING ORDER BY (id_factura ASC);

-- =============================================================================
-- 7. EVENTOS POR TIPO Y SEVERIDAD (Auditoría de seguridad)
-- Query: "Contar eventos críticos de un tipo específico en un rango de fechas"
-- Partition Key: (tipo_evento, severidad) → consultas directas por tipo+severidad
-- =============================================================================
CREATE TABLE IF NOT EXISTS museo_history.eventos_por_tipo_severidad (
    tipo_evento     text,
    severidad       text,
    fecha_evento    timestamp,
    id_entidad      int,
    detalle_json    text,
    usuario_origen  text,
    PRIMARY KEY ((tipo_evento, severidad), fecha_evento, id_entidad)
) WITH CLUSTERING ORDER BY (fecha_evento DESC, id_entidad ASC);
