-- =============================================================================
-- Sprint 2 — Consultas Gerenciales (Query-Driven Modeling)
-- Cada consulta refleja un caso de uso real del administrador del museo.
-- =============================================================================

-- =============================================================================
-- CONSULTA 1: Historial de precios de una obra específica
-- Query original: historial_precio_por_obra
-- =============================================================================
-- Obtener todos los cambios de precio de "La Noche Estrellada" (id_relacional=1)
SELECT id_relacional, fecha_cambio, precio_anterior, precio_nuevo, motivo, usuario_admin
FROM museo_history.historial_precio_por_obra
WHERE id_relacional = 1;

-- Últimos 5 cambios de precio
SELECT id_relacional, fecha_cambio, precio_nuevo, motivo
FROM museo_history.historial_precio_por_obra
WHERE id_relacional = 1
LIMIT 5;

-- =============================================================================
-- CONSULTA 2: Resumen de facturación mensual
-- Query original: auditoria_ventas_por_periodo
-- =============================================================================
-- Todas las ventas de un mes específico
SELECT * FROM museo_history.auditoria_ventas_por_periodo
WHERE periodo = '2026-06';

-- Ventas entre dos meses (se consulta cada partición mensual por separado)
SELECT * FROM museo_history.auditoria_ventas_por_periodo WHERE periodo = '2026-01';
SELECT * FROM museo_history.auditoria_ventas_por_periodo WHERE periodo = '2026-02';
SELECT * FROM museo_history.auditoria_ventas_por_periodo WHERE periodo = '2026-03';

-- =============================================================================
-- CONSULTA 3: Bitácora de eventos por día con filtro por tipo
-- Query original: bitacora_eventos + bitacora_por_tipo
-- =============================================================================
-- 3a. Todos los eventos de un día (tabla bitacora_eventos)
SELECT periodo_dia, timestamp_evento, tipo_evento, severidad, usuario_origen
FROM museo_history.bitacora_eventos
WHERE periodo_dia = '2026-06-05';

-- 3b. Solo eventos de VENTA_REGISTRADA en un día (tabla bitacora_por_tipo)
-- Partition key compuesta: (periodo_dia, tipo_evento) → consulta directa sin ALLOW FILTERING
SELECT periodo_dia, tipo_evento, timestamp_evento, severidad
FROM museo_history.bitacora_por_tipo
WHERE periodo_dia = '2026-06-05'
  AND tipo_evento = 'VENTA_REGISTRADA';

-- 3c. Eventos de precio en rango de horas del mismo día
SELECT periodo_dia, tipo_evento, timestamp_evento, detalle_json
FROM museo_history.bitacora_por_tipo
WHERE periodo_dia = '2026-06-05'
  AND tipo_evento = 'PRECIO_ACTUALIZADO'
  AND timestamp_evento >= '2026-06-05T10:00:00Z'
  AND timestamp_evento <= '2026-06-05T18:00:00Z';

-- =============================================================================
-- CONSULTA 4: Obras más vendidas (top gerencial)
-- Tabla: ventas_por_obra
-- =============================================================================
-- Total recaudado por una obra específica
-- (En la aplicación se suma en Java después de la consulta)
SELECT id_relacional, fecha_venta, monto, impuesto
FROM museo_history.ventas_por_obra
WHERE id_relacional = 1;

-- =============================================================================
-- CONSULTA 5: Ingresos por categoría y período
-- Tabla: ingresos_por_categoria
-- =============================================================================
-- Ingresos generados por la categoría "Pintura" en junio 2026
SELECT * FROM museo_history.ingresos_por_categoria
WHERE categoria = 'Pintura'
  AND periodo = '2026-06';

-- =============================================================================
-- CONSULTA 6: Eventos críticos de seguridad
-- Tabla: eventos_por_tipo_severidad
-- =============================================================================
-- Eventos de tipo OBRA_ELIMINADA con severidad WARN (potencialmente crítico)
SELECT * FROM museo_history.eventos_por_tipo_severidad
WHERE tipo_evento = 'OBRA_ELIMINADA'
  AND severidad = 'WARN';

-- =============================================================================
-- CONSULTA 7: Ubicación actual de obras en salas
-- Tabla: ubicacion_obras_por_sala
-- =============================================================================
-- ¿Qué obras están actualmente en la Sala 1? (fecha_salida IS NULL)
SELECT * FROM museo_history.ubicacion_obras_por_sala
WHERE id_sala = 1;

-- ¿En qué sala ha estado una obra? (historial por obra)
SELECT * FROM museo_history.historial_ubicacion_obras
WHERE id_relacional = 1;

-- =============================================================================
-- CONSULTA 8: Salas del museo por piso
-- Tabla: salas_museo
-- NOTA: Cassandra no soporta SELECT sin WHERE. Para catálogos pequeños
-- se usa una tabla con partition key fija o se consulta desde la app.
-- =============================================================================
-- Consulta todas las salas usando IN (solo para catálogos pequeños)
SELECT * FROM museo_history.salas_museo
WHERE id_sala IN (1, 2, 3, 4, 5);

-- =============================================================================
-- CONSULTA 9: Exportación CSV de historial de precios
-- =============================================================================
-- Query plana para exportar a CSV via aplicación
COPY museo_history.historial_precio_por_obra (id_relacional, fecha_cambio, precio_anterior, precio_nuevo, motivo, usuario_admin)
TO '/tmp/historial_precios_obra_1.csv'
WHERE id_relacional = 1;

-- =============================================================================
-- CONSULTA 10: Limpieza de datos antiguos (TTL / retención)
-- =============================================================================
-- Para propósitos de retención, se pueden borrar particiones viejas
-- (ejecutado por un job programado, no en caliente)
DELETE FROM museo_history.bitacora_eventos WHERE periodo_dia = '2025-01-01';
