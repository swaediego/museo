# Sprint 2 — Informe Guía para el Entregable Final

## Históricos, Auditoría y Reportes (Apache Cassandra)

---

## 1. Lo que pide el entregable

El enunciado del Sprint 2 establece:

> **Metas y Entregables:**
> - Diseñar familias de columnas basadas **estrictamente en las consultas del administrador** (Query-Driven Modeling).
> - Definir correctamente las **Partition Keys** y **Clustering Columns**.
> - **Entregable:** Modelo de familias de columnas y **scripts CQL** para tablas espaciales y consultas gerenciales.

Es decir, se debe demostrar que:
1. Cada tabla Cassandra está diseñada para responder **una y solo una consulta** del administrador.
2. Las claves de partición y clustering se eligieron con criterio según el patrón de acceso.
3. Existen scripts CQL funcionales que crean el esquema, insertan datos de prueba y ejecutan las consultas.

---

## 2. Resumen de la Implementación

| Aspecto | Implementación |
|---------|---------------|
| Motor | Apache Cassandra 5.0 |
| Keyspace | `museo_history` |
| Estrategia de replicación | `SimpleStrategy` (entorno dev; en producción usar `NetworkTopologyStrategy`) |
| Total de tablas | 12 |
| Enfoque | Query-Driven Modeling — cada tabla responde **exactamente una consulta** |
| Duplicación de datos | Intencional y controlada (ej: `bitacora_eventos` + `bitacora_por_tipo`) |
| Integración con backend | Spring Data Cassandra + Event-Driven Architecture |
| Esquema | Auto-creado por Hibernate (`CREATE_IF_NOT_EXISTS`) y respaldado por scripts CQL |

---

## 3. Query-Driven Modeling (Enfoque Central)

Cassandra no permite JOINs ni subconsultas. La única forma de modelar correctamente es:

> **"Cada tabla se diseña para responder una consulta específica. Si dos consultas necesitan los mismos datos con diferentes filtros, se duplica la data en dos tablas con diferentes Partition Keys."**

En este proyecto, cada tabla se creó respondiendo: **"¿Qué consulta específica va a ejecutar el administrador contra esta tabla?"**

### Tabla de correspondencia: Consulta → Tabla

| # | Consulta del Administrador | Tabla Cassandra |
|---|---------------------------|-----------------|
| 1 | "Ver historial de cambios de precio de una obra" | `historial_precio_por_obra` |
| 2 | "Generar resumen de facturación mensual" | `auditoria_ventas_por_periodo` |
| 3 | "Mostrar todos los eventos ocurridos en una fecha" | `bitacora_eventos` |
| 4 | "Filtrar eventos por tipo en un día específico" | `bitacora_por_tipo` |
| 5 | "¿Cuál es la obra más vendida? / Total por obra" | `ventas_por_obra` |
| 6 | "Reporte de ingresos por categoría y período" | `ingresos_por_categoria` |
| 7 | "Contar eventos críticos por tipo y severidad" | `eventos_por_tipo_severidad` |
| 8 | "Listar salas del museo con su capacidad" | `salas_museo` |
| 9 | "¿Qué obras están en la sala X?" | `ubicacion_obras_por_sala` |
| 10 | "¿Dónde ha estado esta obra a lo largo del tiempo?" | `historial_ubicacion_obras` |
| 11 | "Mostrar exhibiciones temporales activas" | `exhibiciones_temporales` |
| 12 | "Listar obras en almacenamiento" | `almacenamiento_obras` |

---

## 4. Diseño de Familias de Columnas

### 4.1 Tablas de Históricos y Auditoría (01_schema.cql)

#### `historial_precio_por_obra`

Propósito: Registrar cada cambio de precio que sufre una obra, de forma inmutable.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `id_relacional` | Todos los cambios de una misma obra caen en la misma partición |
| **Clustering 1** | `fecha_cambio DESC` | Las consultas siempre piden "más reciente primero" |
| **Clustering 2** | `id_evento ASC` | UUID aleatorio para evitar colisiones si hay dos cambios en el mismo instante |

```sql
PRIMARY KEY (id_relacional, fecha_cambio, id_evento)
WITH CLUSTERING ORDER BY (fecha_cambio DESC, id_evento ASC);
```

#### `auditoria_ventas_por_periodo`

Propósito: Resumen de facturación para el departamento de contabilidad.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `periodo` (YYYY-MM) | Todas las ventas de un mes en una partición — tamaño predecible |
| **Clustering 1** | `fecha_venta DESC` | Reportes mensuales: más reciente primero |
| **Clustering 2** | `id_factura ASC` | Desempate para facturas del mismo día |

Para consultas que abarcan varios meses, el backend (Java) itera sobre cada partición mensual.

#### `bitacora_eventos`

Propósito: Bitácora inmutable de todas las operaciones del sistema, agrupada por día.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `periodo_dia` (YYYY-MM-DD) | Un día completo por partición — tamaño controlado |
| **Clustering** | `timestamp_evento DESC` | Orden cronológico inverso para auditoría |

#### `bitacora_por_tipo`

Propósito: Misma data que `bitacora_eventos`, pero con una Partition Key compuesta para permitir filtrado por tipo de evento **sin usar ALLOW FILTERING**.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key compuesta** | `(periodo_dia, tipo_evento)` | Consulta directa: "eventos de tipo X en el día Y" |
| **Clustering** | `timestamp_evento DESC` | Orden cronológico inverso |

Esto es un ejemplo clásico de Query-Driven Modeling: se duplica la data en una segunda tabla para servir un patrón de consulta diferente, porque Cassandra no permite filtrar por columnas no-clave sin ALLOW FILTERING.

#### `eventos_por_tipo_severidad`

Propósito: Auditoría de seguridad — contar eventos críticos (WARN/ERROR) por tipo.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key compuesta** | `(tipo_evento, severidad)` | Consulta: "todos los OBRA_ELIMINADA con severidad WARN" |
| **Clustering 1** | `fecha_evento DESC` | Rango de fechas para análisis temporal |
| **Clustering 2** | `id_entidad ASC` | Identificador único del evento |

### 4.2 Tablas Espaciales (02_tablas_espaciales.cql)

Cassandra no tiene índices geoespaciales como PostGIS. Se modelan coordenadas como `double` y ubicaciones como texto estructurado.

#### `salas_museo`

Propósito: Catálogo de salas físicas del museo (lookup table).

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `id_sala` | Consulta por ID de sala (point lookup) |

Almacena coordenadas (`latitud`, `longitud`) y metadatos (piso, ala, capacidad). Es una tabla pequeña (< 100 filas), por lo que se puede consultar con `IN` desde la aplicación.

#### `ubicacion_obras_por_sala`

Propósito: Responder "¿qué obras están en la sala X?"

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `id_sala` | Todas las obras de una sala en una partición |
| **Clustering** | `id_relacional DESC` | Orden por ID de obra |

`fecha_salida = NULL` indica que la obra está actualmente en esa sala.

#### `historial_ubicacion_obras`

Propósito: Trazabilidad completa de movimientos de una obra.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `id_relacional` | Todo el historial de una obra en una partición |
| **Clustering** | `fecha_ingreso DESC` | Más reciente primero |

Es la inversa de `ubicacion_obras_por_sala`: mientras una consulta es "sala → obras", esta es "obra → salas".

#### `exhibiciones_temporales`

Propósito: Gestionar exhibiciones que ocupan una sala por un período limitado.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key compuesta** | `(id_sala, fecha_inicio)` | Agrupa exhibiciones por sala y fecha de inicio |
| **Clustering** | `id_exhibicion ASC` | UUID como identificador único |

Usa `frozen<list<int>>` para `obras_incluidas` porque Cassandra requiere colecciones congeladas en tablas con clustering columns.

#### `almacenamiento_obras`

Propósito: Inventario de obras en bodega o depósito (no exhibidas).

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `id_zona_almacen` | Todas las obras en una zona de almacén |
| **Clustering** | `id_relacional ASC` | Orden por ID de obra |

### 4.3 Tablas Gerenciales (03_consultas_gerenciales.cql)

#### `ventas_por_obra`

Propósito: Determinar la obra más vendida y el total recaudado por obra.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key** | `id_relacional` | Todas las ventas de una obra en una partición |
| **Clustering 1** | `fecha_venta DESC` | Ventas más recientes primero |
| **Clustering 2** | `id_factura ASC` | Desempate |

La agregación (suma de montos) se hace en Java después de recuperar las filas de la partición.

#### `ingresos_por_categoria`

Propósito: Reporte de ingresos agrupados por género artístico y período.

| Componente | Columna | Justificación |
|------------|---------|---------------|
| **Partition Key compuesta** | `(categoria, periodo)` | Consulta: "¿cuánto generó Pintura en 2026-06?" |
| **Clustering** | `id_factura ASC` | Detalle por factura |

---

## 5. Estrategia de Particionado — Resumen

| Tabla | Partition Key | Tamaño estimado por partición | ¿Hot partition? |
|-------|---------------|------------------------------|-----------------|
| `historial_precio_por_obra` | `id_relacional` | ~10-50 filas/obra | No |
| `auditoria_ventas_por_periodo` | `periodo` (YYYY-MM) | ~100-1000 filas/mes | Moderado (fin de mes) |
| `bitacora_eventos` | `periodo_dia` (YYYY-MM-DD) | ~100-500 filas/día | No |
| `bitacora_por_tipo` | `(periodo_dia, tipo_evento)` | ~10-100 filas/tipo/día | No |
| `ventas_por_obra` | `id_relacional` | ~1-10 filas/obra | No |
| `ingresos_por_categoria` | `(categoria, periodo)` | ~10-100 filas/cat/mes | No |
| `eventos_por_tipo_severidad` | `(tipo_evento, severidad)` | ~10-50 filas | No |
| `salas_museo` | `id_sala` | 1 fila/sala | No |
| `ubicacion_obras_por_sala` | `id_sala` | ~5-50 filas/sala | No |
| `historial_ubicacion_obras` | `id_relacional` | ~5-20 filas/obra | No |
| `exhibiciones_temporales` | `(id_sala, fecha_inicio)` | ~1-5 filas/sala | No |
| `almacenamiento_obras` | `id_zona_almacen` | ~10-100 filas/zona | No |

Ninguna tabla presenta riesgo de *hot partition* porque todas usan particiones con tamaño acotado (días, meses, o IDs individuales).

---

## 6. Arquitectura de Integración (Event-Driven)

Cassandra se integra con el backend transaccional (PostgreSQL) mediante un patrón **Event-Driven** con Spring:

```
Usuario (Frontend)
    │
    ▼
Controlador REST ──→ ArtService.actualizarPrecio() [@Transactional]
                            │
                            ├── ArtRepository.save() → PostgreSQL (commit)
                            │
                            └── eventPublisher.publishEvent(PrecioActualizadoEvent)
                                        │
                                        ▼
                            CassandraHistoryListener [@TransactionalEventListener(AFTER_COMMIT)]
                                        │
                                        ├── HistorialPrecioRepository.save() → Cassandra
                                        ├── BitacoraEventoRepository.save() → Cassandra
                                        └── BitacoraPorTipoRepository.save() → Cassandra
```

### Eventos implementados

| Evento | Publicado por | Disparador | Tablas Cassandra afectadas |
|--------|--------------|------------|---------------------------|
| `PrecioActualizadoEvent` | `ArtServiceImpl` | Cambio de precio | `historial_precio_por_obra`, `bitacora_eventos`, `bitacora_por_tipo` |
| `VentaRegistradaEvent` | `InvoiceServiceImpl` | Facturación | `auditoria_ventas_por_periodo`, `bitacora_eventos`, `bitacora_por_tipo` |
| `EstatusCambiadoEvent` | `ArtServiceImpl` / `InvoiceServiceImpl` | Reserva/cancelación/venta | `bitacora_eventos`, `bitacora_por_tipo` |
| `ObraEliminadaEvent` | `ArtServiceImpl` | Eliminación de obra | `bitacora_eventos`, `bitacora_por_tipo` |

### Decisiones de diseño importantes

1. **TransactionalEventListener(AFTER_COMMIT)**: La escritura en Cassandra ocurre **después** de que PostgreSQL confirma la transacción. Si la transacción de PostgreSQL falla, el evento nunca se publica.

2. **Fail-safe**: Si Cassandra está caído, el listener captura la excepción y la registra en log, pero **no bloquea** la transacción de PostgreSQL. Esto sigue el principio **AP sobre CP** (Availability > Consistency) para el historial.

3. **No hay consistencia fuerte**: Cassandra es eventualmente consistente. Si Cassandra falla, el historial se pierde (se registra en logs como fallback). Esto es aceptable para datos de auditoría, pero no para datos transaccionales.

---

## 7. Endpoints REST expuestos (HistoryController)

| Método | Ruta | Descripción | Tabla consultada |
|--------|------|-------------|-----------------|
| GET | `/api/history/precios/{idRelacional}` | Historial de precios de una obra | `historial_precio_por_obra` |
| GET | `/api/history/ventas?desde=&hasta=` | Auditoría de ventas por rango de meses | `auditoria_ventas_por_periodo` |
| GET | `/api/history/bitacora?desde=&hasta=` | Bitácora de eventos por rango de días | `bitacora_eventos` |
| GET | `/api/history/bitacora/{tipo}?desde=&hasta=` | Bitácora filtrada por tipo de evento | `bitacora_por_tipo` |
| GET | `/api/history/gerencial/ventas-por-obra` | Todas las ventas agrupadas por obra | `ventas_por_obra` |
| GET | `/api/history/gerencial/ventas-por-obra/{id}` | Ventas de una obra específica | `ventas_por_obra` |
| GET | `/api/history/precios/{idRelacional}/csv` | Exportar historial de precios a CSV | `historial_precio_por_obra` |

**Seguridad**: Todos los endpoints de `/api/history/**` requieren rol `ADMIN` (configurado en `SecurityConfig.java`).

---

## 8. Archivos que componen el entregable

### CQL Scripts (en `scripts-cassandra/`)

| Archivo | Contenido | Líneas |
|---------|-----------|--------|
| `01_schema.cql` | Keyspace + 7 tablas de históricos y auditoría | 131 |
| `02_tablas_espaciales.cql` | 5 tablas espaciales (salas, ubicación, almacenamiento) | 88 |
| `03_consultas_gerenciales.cql` | 10 consultas de administración con ejemplos | 121 |
| `04_seed_data.cql` | Datos de prueba para todas las tablas | 116 |

### Código Java (en `src/main/java/com/uneg/galeria/`)

| Archivo | Propósito |
|---------|-----------|
| `config/CassandraConfig.java` | Configuración de conexión y auto-creación de esquema |
| `history/domain/*.java` (12 clases) | Entidades `@Table` con Partition Keys y Clustering Columns |
| `history/repository/*.java` (10 interfaces) | Repositorios Spring Data con consultas CQL |
| `history/event/*.java` (4 records) | Eventos de dominio |
| `history/listener/CassandraHistoryListener.java` | Listeners que escriben en Cassandra después del commit |
| `history/controller/HistoryController.java` | Endpoints REST para consultar datos históricos |

---

## 9. Verificación del cumplimiento

### Lo que pedía el Sprint 2: ✅

| Requisito | Cómo se cumple |
|-----------|----------------|
| **Familias de columnas basadas en consultas** | Cada tabla responde exactamente una consulta (ver sección 3) |
| **Partition Keys y Clustering Columns correctamente definidas** | Cada tabla tiene PK y CC justificadas por patrón de acceso (sección 4) |
| **Scripts CQL** | 4 scripts: esquema, tablas espaciales, consultas gerenciales, seed data |
| **Tablas espaciales** | `salas_museo`, `ubicacion_obras_por_sala`, `historial_ubicacion_obras`, `exhibiciones_temporales`, `almacenamiento_obras` |
| **Consultas gerenciales** | `ventas_por_obra`, `ingresos_por_categoria`, `eventos_por_tipo_severidad` |
| **Alta disponibilidad** | Cassandra es AP por diseño; el listener es fail-safe |
| **Bitácora inmutable** | Solo INSERT, nunca UPDATE/DELETE en tablas de eventos |
| **Auditoría de seguridad** | `eventos_por_tipo_severidad` con severidad WARN/ERROR/INFO |

---

## 10. Puntos clave para la defensa

1. **"¿Por qué Cassandra y no PostgreSQL para el historial?"**
   - Cassandra ofrece escritura横向mente escalable (lineal) para ingesta masiva de eventos de auditoría.
   - PostgreSQL no escala horizontalmente de forma nativa; Cassandra sí (añadiendo nodos).
   - El historial es inmutable (solo INSERT), que es el caso de uso ideal para Cassandra.

2. **"¿Por qué duplicar datos entre `bitacora_eventos` y `bitacora_por_tipo`?"**
   - Cassandra no permite filtros eficientes por columnas no-clave. La duplicación es el patrón correcto en Cassandra: cada tabla sirve una consulta diferente.

3. **"¿Cómo se asegura la consistencia entre PostgreSQL y Cassandra?"**
   - No hay consistencia transaccional. Se usa un patrón Event-Driven con `@TransactionalEventListener(AFTER_COMMIT)`: primero se escribe en PostgreSQL, y solo si el commit es exitoso se propaga el evento a Cassandra. Si Cassandra falla, la operación principal no se bloquea.

4. **"¿Qué pasa si Cassandra está caído?"**
   - El listener captura la excepción, la registra en log, y la operación continúa. Los datos de auditoría se pierden temporalmente, pero el sistema transaccional sigue funcionando. Esto es una decisión arquitectónica: **AP > CP** para datos de auditoría.

5. **"¿Por qué no usar TTL de Cassandra para retención?"**
   - No se implementó TTL porque los requisitos de retención no estaban definidos. Se incluye una consulta de ejemplo para limpieza manual (`DELETE FROM bitacora_eventos WHERE periodo_dia = '2025-01-01'`).
