# Estado del Proyecto — Sprint 2 Cassandra

> Fecha: 2026-06-05
> Basado en: `.hermes/plans/2026-06-04_185117-sprint2-cassandra.md`

---

## Resumen ejecutivo

El Sprint 2 (Cassandra para históricos, auditoría y reportes) está **funcionalmente completo**. La integración de Cassandra como microservicio de apoyo está operativa, los 4 eventos (`PrecioActualizadoEvent`, `VentaRegistradaEvent`, `EstatusCambiadoEvent`, `ObraEliminadaEvent`) se publican desde los servicios transaccionales y el listener los persiste en Cassandra. La capa de seguridad con JWT está implementada y `/api/history/**` queda restringido a `ROLE_ADMIN`. **Pendiente solo lo procedural:** ejecutar el flujo end-to-end con datos reales (phase3-verify) y una revisión final de talking points para la defensa (phase3-review).

---

## Qué se hizo (completado)

### 1. Infraestructura Docker ✅

- **Servicio `cassandra`** agregado al `docker-compose.yml` con imagen `cassandra:5.0`
- Puerto `9042` expuesto, volumen `cassandra_data` nombrado
- `healthcheck` con `cqlsh -e 'describe cluster'`, `start_period: 60s`, `retries: 12`
- `depends_on` con `condition: service_healthy` en el backend
- Variables de entorno configuradas: `SPRING_DATA_CASSANDRA_CONTACT_POINTS`, `SPRING_DATA_CASSANDRA_LOCAL_DC`, `SPRING_DATA_CASSANDRA_KEYSPACE_NAME`, `SPRING_DATA_CASSANDRA_SCHEMA_ACTION=CREATE_IF_NOT_EXISTS`

### 2. Dependencias ✅

- `org.springframework.boot:spring-boot-starter-data-cassandra` en `build.gradle`
- `io.jsonwebtoken:jjwt-api:0.12.6` + runtime implementations para JWT (security)

### 3. Modelo de datos Cassandra ✅

Las 3 tablas del plan fueron creadas (via `CREATE_IF_NOT_EXISTS` de Spring Data Cassandra):

| Tabla | PK | Clustering | Verificado |
|---|---|---|---|
| `historial_precio_por_obra` | `id_relacional` | `fecha_cambio DESC, id_evento ASC` | ✅ CQLSH |
| `auditoria_ventas_por_periodo` | `periodo` (YYYY-MM) | `fecha_venta DESC, id_factura ASC` | ✅ CQLSH |
| `bitacora_eventos` | `periodo_dia` (YYYY-MM-DD) | `timestamp_evento DESC, tipo_evento ASC` | ✅ CQLSH |

### 4. Paquete `history` completo ✅

```
history/
├── event/
│   ├── PrecioActualizadoEvent.java
│   ├── VentaRegistradaEvent.java
│   ├── EstatusCambiadoEvent.java
│   └── ObraEliminadaEvent.java       ← agregado en sesión
├── domain/
│   ├── HistorialPrecio.java
│   ├── AuditoriaVenta.java
│   └── BitacoraEvento.java
├── repository/
│   ├── HistorialPrecioRepository.java
│   ├── AuditoriaVentaRepository.java
│   └── BitacoraEventoRepository.java
├── listener/
│   └── CassandraHistoryListener.java  ← maneja 4 eventos
└── controller/
    └── HistoryController.java        ← endpoints GET /api/history/*
```

**Dominio nota importante:** Se descubrió que el enum correcto para `@PrimaryKeyColumn` es `PrimaryKeyType.PARTITIONED` (no `PrimaryKeyColumn.Type.PARTITIONED`). El JAR `spring-data-cassandra-4.4.4.jar` fue inspeccionado con Python para confirmarlo.

### 5. Publicación de eventos ✅

**ArtServiceImpl:**
- `actualizarPrecio()` → publica `PrecioActualizadoEvent` con precio anterior y nuevo
- `eliminarObra()` → publica `ObraEliminadaEvent` con id, nombre y usuario
- `reservarObra()` → publica `EstatusCambiadoEvent` ("Disponible" → "Reservada")
- `cancelarReserva()` → publica `EstatusCambiadoEvent` ("Reservada" → "Disponible")

**InvoiceServiceImpl:**
- `crearFactura()` → publica `VentaRegistradaEvent` (después de `invoiceRepository.save(factura)`)
- `crearFactura()` → publica `EstatusCambiadoEvent` ("Reservada" → "Vendida") para que la bitácora registre la transición completa

### 6. Listener asíncrono ✅

`CassandraHistoryListener` con `@TransactionalEventListener(phase = AFTER_COMMIT)`:
- Implementa lógica de escritura en las 3 tablas de Cassandra
- **No relanza excepciones** — si Cassandra falla, Postgres sigue funcionando (patrón AP)
- También escribe en `bitacora_eventos` cuando cambia precio o se elimina una obra

### 7. Endpoints de lectura ✅

| Endpoint | Descripción | Estado |
|---|---|---|
| `GET /api/history/precios/{id}` | Historial de precios de una obra | ✅ funcional |
| `GET /api/history/precios/{id}/csv` | Exportación CSV del historial | ✅ funcional |
| `GET /api/history/ventas?desde=&hasta=` | Auditoría de ventas por rango de meses | ✅ implementado |
| `GET /api/history/bitacora?desde=&hasta=&tipo=` | Bitácora filtrable por día y tipo | ✅ implementado |

### 8. Capa de seguridad JWT ✅

- `JwtUtil.java` — genera/valida tokens HS256, 24h expiry, roles como claim
- `JwtAuthenticationFilter.java` — filtra `Authorization: Bearer <token>` en cada request
- `SecurityConfig.java` (en `config/`, merge del existente) — configura:
  - `/api/auth/**` → público (login)
  - `/api/catalog`, `/api/arts`, `/api/artistas`, `/api/generos`, `/api/buyers` → público (GET)
  - `/api/history/**` → `hasRole("ADMIN")`
  - Todo lo demás → autenticado
- `AuthController`原来的) extendido para retornar JWT junto con datos del usuario
- Login: `admin_ana / admin123` → `ROLE_ADMIN` | `rhixeidys01 / password123` → `ROLE_BUYER`

**Tests de security verificados:**
- Sin token → `403`
- Token buyer (no admin) → `403`
- Token admin → `200` + datos

---

## Qué falta (pendiente)

### Falta de la lista original ✅ phase3-4

> **phase3-4: Fase 3: Security (rutas /api/history/* con rol ADMIN)** — ✅ completado y verificado

### phase3-review: Spec + Quality Review

Revisión de que todo el código coincide con el plan y la defensa es sostenible. **No realizado.**

### phase3-verify: Compilación + verificación completa

Verificación final de todos los flujos end-to-end. **Pendiente.** El código de los 4 eventos y sus listeners está listo, falta ejecutar el flujo de venta completo end-to-end para confirmar que las 4 inserciones en Cassandra (historial_precio_por_obra, auditoria_ventas_por_periodo, bitacora_eventos) funcionan con datos reales.

### Resumen de pendientes

| Item | Prioridad | Comentario |
|---|---|---|
| Publicar `VentaRegistradaEvent` en InvoiceServiceImpl | — | ✅ Ya estaba hecho (línea 89) |
| Publicar `EstatusCambiadoEvent` (Reservada→Vendida) en InvoiceServiceImpl | — | ✅ Hecho en este pase |
| phase3-review (spec + quality) | Alta | Revisión de defensa |
| phase3-verify (verificación completa) | Alta | Tests end-to-end con datos reales |

---

## Cómo se harían los pendientes

### 1. `VentaRegistradaEvent` y `EstatusCambiadoEvent` en InvoiceServiceImpl

✅ **Ya implementados.** El publish de `VentaRegistradaEvent` estaba desde el inicio (línea 89 de `InvoiceServiceImpl.crearFactura`, después de `invoiceRepository.save(factura)`). El de `EstatusCambiadoEvent` con la transición "Reservada" → "Vendida" se agregó en este pase, también dentro de `crearFactura`, después del save de la factura y antes del `VentaRegistradaEvent`.

### 2. Probar `EstatusCambiadoEvent`

El listener maneja `EstatusCambiadoEvent` disparado desde `ArtServiceImpl.reservarObra`, `ArtServiceImpl.cancelarReserva` e `InvoiceServiceImpl.crearFactura`. No hay un endpoint dedicado para cambiar estatus a mano; hay que disparar el flujo de negocio real.

```bash
# 1. Obtener token admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin_ana","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 2. Disparar la transición Reservada -> Vendida (la que faltaba).
#    El endpoint es POST /api/invoices/sell (InvoiceController.java línea 22).
#    Pre-condición: la obra debe estar en estatus "Reservada" con un
#    comprador que tenga membresía paga y código de seguridad.
curl -s -X POST http://localhost:8080/api/invoices/sell \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"obraId": 1, "compradorId": 1, "adminId": 1, "codigoSeguridad": "1234", "direccion": "..."}'

# 3. Verificar en Cassandra que se escribieron las 3 cosas:
#    - 1 fila en auditoria_ventas_por_periodo
#    - 1 fila en bitacora_eventos con tipo_evento = 'VENTA_REGISTRADA'
#    - 1 fila en bitacora_eventos con tipo_evento = 'ESTATUS_CAMBIADO' (la que se agregó en este pase)
docker exec cassandra-galeria cqlsh -e \
  "SELECT tipo_evento, detalle_json, severidad FROM museo_history.bitacora_eventos WHERE periodo_dia = '2026-06-05';"

docker exec cassandra-galeria cqlsh -e \
  "SELECT periodo, id_factura, id_obra, monto FROM museo_history.auditoria_ventas_por_periodo WHERE periodo = '2026-06';"
```

### 3. phase3-review

Revisar que cada talking point del plan esté respondido:

1. **CAP — Disponibilidad sobre Consistencia:** ✅ Cassandra caída no bloquea Postgres
2. **Query-driven modeling:** ✅ 3 tablas, cada una con PK = período, clustering DESC
3. **Asincronía desacoplada:** ✅ `@TransactionalEventListener(AFTER_COMMIT)`
4. **Append-only / inmutabilidad:** ✅ Solo INSERT, no UPDATE/DELETE en código
5. **Escalabilidad horizontal:** ✅ `contact-points` configurable

### 4. phase3-verify

Ejecutar el script de verificación completo del plan:

```bash
# 1. Verificar que los 3 contenedores están arriba
docker compose ps

# 2. Verificar que backend levantó sin errores de Cassandra
docker logs galeria-app-backend | grep -i cassandra

# 3. Login como admin
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"login":"admin_ana","password":"admin123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# 4. Verificar endpoint de precios
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/history/precios/1 | python3 -m json.tool

# 5. Verificar endpoint de bitácora
curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/history/bitacora?desde=2026-06-01&hasta=2026-06-30" | python3 -m json.tool

# 6. Verificar CSV export
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/history/precios/1/csv

# 7. Verificar CQLSH directo
docker exec cassandra-galeria cqlsh -e \
  "SELECT id_relacional, precio_anterior, precio_nuevo, fecha_cambio, usuario_admin FROM museo_history.historial_precio_por_obra WHERE id_relacional = 1;"
```

---

## Archivos modificados / creados (resumen)

| Archivo | Acción |
|---|---|
| `build.gradle` | Modificado — agregó `spring-boot-starter-data-cassandra` + `jjwt` |
| `docker-compose.yml` | Modificado — agregó servicio `cassandra` + volumen |
| `src/main/resources/application.properties` | Modificado — 4 líneas de configuración Cassandra |
| `src/main/java/com/uneg/galeria/config/SecurityConfig.java` | **Reescrito** — pasó de session-based a JWT stateless (filtro custom `JwtAuthenticationFilter`, `hasRole("ADMIN")` para `/api/history/**`, sin CSRF) |
| `src/main/java/com/uneg/galeria/services/impl/ArtServiceImpl.java` | Modificado — publica `PrecioActualizadoEvent` en `actualizarPrecio()`, `ObraEliminadaEvent` en `eliminarObra()`, y `EstatusCambiadoEvent` en `reservarObra()` y `cancelarReserva()` |
| `src/main/java/com/uneg/galeria/services/impl/InvoiceServiceImpl.java` | Modificado — publica `VentaRegistradaEvent` y `EstatusCambiadoEvent` ("Reservada" → "Vendida") en `crearFactura()` (este último agregado en este pase) |
| `src/main/java/com/uneg/galeria/history/event/PrecioActualizadoEvent.java` | Creado |
| `src/main/java/com/uneg/galeria/history/event/VentaRegistradaEvent.java` | Creado |
| `src/main/java/com/uneg/galeria/history/event/EstatusCambiadoEvent.java` | Creado |
| `src/main/java/com/uneg/galeria/history/event/ObraEliminadaEvent.java` | Creado |
| `src/main/java/com/uneg/galeria/history/domain/HistorialPrecio.java` | Creado |
| `src/main/java/com/uneg/galeria/history/domain/AuditoriaVenta.java` | Creado |
| `src/main/java/com/uneg/galeria/history/domain/BitacoraEvento.java` | Creado |
| `src/main/java/com/uneg/galeria/history/repository/HistorialPrecioRepository.java` | Creado |
| `src/main/java/com/uneg/galeria/history/repository/AuditoriaVentaRepository.java` | Creado |
| `src/main/java/com/uneg/galeria/history/repository/BitacoraEventoRepository.java` | Creado |
| `src/main/java/com/uneg/galeria/history/listener/CassandraHistoryListener.java` | Creado |
| `src/main/java/com/uneg/galeria/history/controller/HistoryController.java` | Creado |
| `src/main/java/com/uneg/galeria/security/JwtUtil.java` | Creado |
| `src/main/java/com/uneg/galeria/security/JwtAuthenticationFilter.java` | Creado |
| `src/main/java/com/uneg/galeria/security/AuthRequest.java` | Creado |
| `src/main/java/com/uneg/galeria/security/AuthResponse.java` | Creado |
| `src/main/java/com/uneg/galeria/controllers/AuthController.java` | Modificado — ahora retorna JWT |

**Archivos eliminados:**
- `src/main/java/com/uneg/galeria/security/AuthDto.java` (reemplazado por AuthRequest + AuthResponse)
- `src/main/java/com/uneg/galeria/security/SecurityConfig.java` (unificado en `config/SecurityConfig.java`)
- `src/main/java/com/uneg/galeria/security/JwtAuthController.java` (unificado en `controllers/AuthController.java`)