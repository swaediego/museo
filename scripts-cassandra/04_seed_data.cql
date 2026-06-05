-- =============================================================================
-- Sprint 2 — Datos de Prueba (Seed Data)
-- Inserts de ejemplo para verificar las consultas gerenciales y espaciales.
-- =============================================================================

-- =============================================================================
-- HISTORIAL DE PRECIOS
-- =============================================================================
INSERT INTO museo_history.historial_precio_por_obra (id_relacional, fecha_cambio, id_evento, precio_anterior, precio_nuevo, motivo, usuario_admin)
VALUES (1, '2026-01-15T10:00:00Z', uuid(), 1500.00, 2000.00, 'Revalorización anual', 'admin_ana');

INSERT INTO museo_history.historial_precio_por_obra (id_relacional, fecha_cambio, id_evento, precio_anterior, precio_nuevo, motivo, usuario_admin)
VALUES (1, '2026-03-20T14:30:00Z', uuid(), 2000.00, 2500.00, 'Incremento por demanda', 'admin_ana');

INSERT INTO museo_history.historial_precio_por_obra (id_relacional, fecha_cambio, id_evento, precio_anterior, precio_nuevo, motivo, usuario_admin)
VALUES (2, '2026-02-10T09:00:00Z', uuid(), 1000.00, 1200.00, 'Ajuste de mercado', 'admin_ana');

-- =============================================================================
-- AUDITORÍA DE VENTAS
-- =============================================================================
INSERT INTO museo_history.auditoria_ventas_por_periodo (periodo, fecha_venta, id_factura, id_obra, id_comprador, monto, impuesto, metodo_pago, estatus_factura)
VALUES ('2026-06', '2026-06-01T15:00:00Z', 1001, 1, 1, 2500.00, 375.00, 'Transferencia', 'PAGADA');

INSERT INTO museo_history.auditoria_ventas_por_periodo (periodo, fecha_venta, id_factura, id_obra, id_comprador, monto, impuesto, metodo_pago, estatus_factura)
VALUES ('2026-06', '2026-06-03T11:30:00Z', 1002, 3, 2, 750000.00, 112500.00, 'Tarjeta', 'PAGADA');

INSERT INTO museo_history.auditoria_ventas_por_periodo (periodo, fecha_venta, id_factura, id_obra, id_comprador, monto, impuesto, metodo_pago, estatus_factura)
VALUES ('2026-05', '2026-05-20T16:45:00Z', 1000, 2, 3, 1200.00, 180.00, 'Efectivo', 'PAGADA');

-- =============================================================================
-- BITÁCORA DE EVENTOS (bitacora_eventos + bitacora_por_tipo)
-- Se inserta en ambas tablas para soportar ambos patrones de consulta.
-- =============================================================================
INSERT INTO museo_history.bitacora_eventos (periodo_dia, timestamp_evento, tipo_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-05', '2026-06-05T10:00:00Z', 'PRECIO_ACTUALIZADO', 1, 'Art', '{"precioAnterior":2000,"precioNuevo":2500,"motivo":"Incremento por demanda"}', 'INFO', 'admin_ana');

INSERT INTO museo_history.bitacora_por_tipo (periodo_dia, tipo_evento, timestamp_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-05', 'PRECIO_ACTUALIZADO', '2026-06-05T10:00:00Z', 1, 'Art', '{"precioAnterior":2000,"precioNuevo":2500,"motivo":"Incremento por demanda"}', 'INFO', 'admin_ana');

INSERT INTO museo_history.bitacora_eventos (periodo_dia, timestamp_evento, tipo_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-05', '2026-06-05T11:15:00Z', 'VENTA_REGISTRADA', 1001, 'Invoice', '{"idObra":1,"idComprador":1,"monto":2500,"estatus":"PAGADA"}', 'INFO', null);

INSERT INTO museo_history.bitacora_por_tipo (periodo_dia, tipo_evento, timestamp_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-05', 'VENTA_REGISTRADA', '2026-06-05T11:15:00Z', 1001, 'Invoice', '{"idObra":1,"idComprador":1,"monto":2500,"estatus":"PAGADA"}', 'INFO', null);

INSERT INTO museo_history.bitacora_eventos (periodo_dia, timestamp_evento, tipo_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-04', '2026-06-04T09:30:00Z', 'ESTATUS_CAMBIADO', 5, 'Art', '{"estatusAnterior":"Disponible","estatusNuevo":"Reservada"}', 'INFO', 'carlos_perez');

INSERT INTO museo_history.bitacora_por_tipo (periodo_dia, tipo_evento, timestamp_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-04', 'ESTATUS_CAMBIADO', '2026-06-04T09:30:00Z', 5, 'Art', '{"estatusAnterior":"Disponible","estatusNuevo":"Reservada"}', 'INFO', 'carlos_perez');

INSERT INTO museo_history.bitacora_eventos (periodo_dia, timestamp_evento, tipo_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-03', '2026-06-03T16:00:00Z', 'OBRA_ELIMINADA', 6, 'Art', '{"nombreObra":"El Beso"}', 'WARN', 'admin_ana');

INSERT INTO museo_history.bitacora_por_tipo (periodo_dia, tipo_evento, timestamp_evento, id_entidad, tipo_entidad, detalle_json, severidad, usuario_origen)
VALUES ('2026-06-03', 'OBRA_ELIMINADA', '2026-06-03T16:00:00Z', 6, 'Art', '{"nombreObra":"El Beso"}', 'WARN', 'admin_ana');

-- =============================================================================
-- SALAS DEL MUSEO (Espacial)
-- =============================================================================
INSERT INTO museo_history.salas_museo (id_sala, nombre, piso, ala, capacidad_max, latitud, longitud, descripcion)
VALUES (1, 'Sala Principal', 1, 'Norte', 50, 10.4806, -66.9036, 'Sala central de exhibiciones permanentes');

INSERT INTO museo_history.salas_museo (id_sala, nombre, piso, ala, capacidad_max, latitud, longitud, descripcion)
VALUES (2, 'Sala de Esculturas', 1, 'Este', 30, 10.4806, -66.9036, 'Espacio dedicado a esculturas de gran formato');

INSERT INTO museo_history.salas_museo (id_sala, nombre, piso, ala, capacidad_max, latitud, longitud, descripcion)
VALUES (3, 'Galería de Fotografía', 2, 'Sur', 25, 10.4806, -66.9036, 'Exhibiciones de fotografía contemporánea');

-- =============================================================================
-- UBICACIÓN DE OBRAS EN SALAS
-- =============================================================================
INSERT INTO museo_history.ubicacion_obras_por_sala (id_sala, id_relacional, nombre_obra, nombre_artista, fecha_ingreso, posicion_estante)
VALUES (1, 1, 'La Noche Estrellada', 'Vincent van Gogh', '2026-01-10T09:00:00Z', 'Pared Norte, Marco A1');

INSERT INTO museo_history.ubicacion_obras_por_sala (id_sala, id_relacional, nombre_obra, nombre_artista, fecha_ingreso, posicion_estante)
VALUES (2, 3, 'El Pensador', 'Auguste Rodin', '2026-01-10T09:00:00Z', 'Pedestal Central');

INSERT INTO museo_history.ubicacion_obras_por_sala (id_sala, id_relacional, nombre_obra, nombre_artista, fecha_ingreso, posicion_estante)
VALUES (3, 4, 'Monolith, the Face of Half Dome', 'Ansel Adams', '2026-02-01T10:00:00Z', 'Pared Oeste, Marco F7');

-- =============================================================================
-- HISTORIAL DE UBICACIÓN DE OBRAS
-- =============================================================================
INSERT INTO museo_history.historial_ubicacion_obras (id_relacional, fecha_ingreso, id_sala, nombre_sala, posicion_estante)
VALUES (1, '2025-06-01T09:00:00Z', 2, 'Sala de Esculturas', 'Esquina Noroeste (exhibición temporal)');

INSERT INTO museo_history.historial_ubicacion_obras (id_relacional, fecha_ingreso, id_sala, nombre_sala, posicion_estante)
VALUES (1, '2026-01-10T09:00:00Z', 1, 'Sala Principal', 'Pared Norte, Marco A1');

-- =============================================================================
-- VENTAS POR OBRA (Gerencial)
-- =============================================================================
INSERT INTO museo_history.ventas_por_obra (id_relacional, fecha_venta, id_factura, monto, impuesto, id_comprador)
VALUES (1, '2026-06-01T15:00:00Z', 1001, 2500.00, 375.00, 1);

INSERT INTO museo_history.ventas_por_obra (id_relacional, fecha_venta, id_factura, monto, impuesto, id_comprador)
VALUES (3, '2026-06-03T11:30:00Z', 1002, 750000.00, 112500.00, 2);

-- =============================================================================
-- INGRESOS POR CATEGORÍA (Gerencial)
-- =============================================================================
INSERT INTO museo_history.ingresos_por_categoria (categoria, periodo, id_factura, id_obra, monto, impuesto)
VALUES ('Pintura', '2026-06', 1001, 1, 2500.00, 375.00);

INSERT INTO museo_history.ingresos_por_categoria (categoria, periodo, id_factura, id_obra, monto, impuesto)
VALUES ('Escultura', '2026-06', 1002, 3, 750000.00, 112500.00);

-- =============================================================================
-- EVENTOS POR TIPO Y SEVERIDAD (Auditoría de seguridad)
-- =============================================================================
INSERT INTO museo_history.eventos_por_tipo_severidad (tipo_evento, severidad, fecha_evento, id_entidad, detalle_json, usuario_origen)
VALUES ('OBRA_ELIMINADA', 'WARN', '2026-06-03T16:00:00Z', 6, '{"nombreObra":"El Beso"}', 'admin_ana');

INSERT INTO museo_history.eventos_por_tipo_severidad (tipo_evento, severidad, fecha_evento, id_entidad, detalle_json, usuario_origen)
VALUES ('PRECIO_ACTUALIZADO', 'INFO', '2026-06-05T10:00:00Z', 1, '{"precioAnterior":2000,"precioNuevo":2500}', 'admin_ana');
