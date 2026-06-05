package com.uneg.galeria.history.listener;

import com.uneg.galeria.history.domain.AuditoriaVenta;
import com.uneg.galeria.history.domain.BitacoraEvento;
import com.uneg.galeria.history.domain.BitacoraPorTipo;
import com.uneg.galeria.history.domain.HistorialPrecio;
import com.uneg.galeria.history.event.EstatusCambiadoEvent;
import com.uneg.galeria.history.event.ObraEliminadaEvent;
import com.uneg.galeria.history.event.PrecioActualizadoEvent;
import com.uneg.galeria.history.event.VentaRegistradaEvent;
import com.uneg.galeria.history.repository.AuditoriaVentaRepository;
import com.uneg.galeria.history.repository.BitacoraEventoRepository;
import com.uneg.galeria.history.repository.BitacoraPorTipoRepository;
import com.uneg.galeria.history.repository.HistorialPrecioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CassandraHistoryListener {

    private final HistorialPrecioRepository precioRepo;
    private final AuditoriaVentaRepository ventaRepo;
    private final BitacoraEventoRepository bitacoraRepo;
    private final BitacoraPorTipoRepository bitacoraPorTipoRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPrecioCambiado(PrecioActualizadoEvent e) {
        try {
            HistorialPrecio hp = HistorialPrecio.builder()
                .idRelacional(e.idRelacional())
                .fechaCambio(e.fecha())
                .idEvento(UUID.randomUUID())
                .precioAnterior(e.precioAnterior())
                .precioNuevo(e.precioNuevo())
                .motivo(e.motivo())
                .usuarioAdmin(e.usuario())
                .build();
            precioRepo.save(hp);

            guardarBitacora("PRECIO_ACTUALIZADO", e.idRelacional(), "Art",
                objectMapper.createObjectNode()
                    .put("precioAnterior", e.precioAnterior().doubleValue())
                    .put("precioNuevo", e.precioNuevo().doubleValue())
                    .put("motivo", e.motivo()),
                "INFO", e.usuario());

            log.info("Historial precio guardado en Cassandra para obra idRelacional={}", e.idRelacional());
        } catch (Exception ex) {
            log.error("No se pudo escribir historial de precio en Cassandra: {}", ex.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVentaRegistrada(VentaRegistradaEvent e) {
        try {
            String periodo = DateTimeFormatter.ofPattern("yyyy-MM")
                .withZone(ZoneId.systemDefault())
                .format(e.fechaVenta());

            AuditoriaVenta av = AuditoriaVenta.builder()
                .periodo(periodo)
                .fechaVenta(e.fechaVenta())
                .idFactura(e.idFactura())
                .idObra(e.idObra())
                .idComprador(e.idComprador())
                .monto(e.monto())
                .impuesto(e.impuesto())
                .metodoPago(e.metodoPago())
                .estatusFactura(e.estatusFactura())
                .build();
            ventaRepo.save(av);

            guardarBitacora("VENTA_REGISTRADA", e.idFactura(), "Invoice",
                objectMapper.createObjectNode()
                    .put("idObra", e.idObra())
                    .put("idComprador", e.idComprador())
                    .put("monto", e.monto().doubleValue())
                    .put("impuesto", e.impuesto().doubleValue())
                    .put("estatus", e.estatusFactura()),
                "INFO", null);

            log.info("Auditoria venta guardada en Cassandra para factura id={}", e.idFactura());
        } catch (Exception ex) {
            log.error("No se pudo escribir auditoria de venta en Cassandra: {}", ex.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEstatusCambiado(EstatusCambiadoEvent e) {
        try {
            guardarBitacora("ESTATUS_CAMBIADO", e.idRelacional(), e.entidadTipo(),
                objectMapper.createObjectNode()
                    .put("estatusAnterior", e.estatusAnterior())
                    .put("estatusNuevo", e.estatusNuevo()),
                "INFO", e.usuario());

            log.info("Bitacora estatus cambiada para {} id={}", e.entidadTipo(), e.idRelacional());
        } catch (Exception ex) {
            log.error("No se pudo escribir bitacora de estatus en Cassandra: {}", ex.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onObraEliminada(ObraEliminadaEvent e) {
        try {
            guardarBitacora("OBRA_ELIMINADA", e.idRelacional(), "Art",
                objectMapper.createObjectNode()
                    .put("nombreObra", e.nombreObra()),
                "WARN", e.usuario());

            log.info("Bitacora obra eliminada para id={}", e.idRelacional());
        } catch (Exception ex) {
            log.error("No se pudo escribir bitacora de obra eliminada en Cassandra: {}", ex.getMessage());
        }
    }

    private void guardarBitacora(String tipoEvento, Integer idEntidad, String tipoEntidad,
                                com.fasterxml.jackson.databind.JsonNode detalle, String severidad, String usuario) {
        Instant ahora = Instant.now();
        String periodoDia = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault())
            .format(ahora);

        BitacoraEvento be = BitacoraEvento.builder()
            .periodoDia(periodoDia)
            .timestampEvento(ahora)
            .tipoEvento(tipoEvento)
            .idEntidad(idEntidad)
            .tipoEntidad(tipoEntidad)
            .detalleJson(detalle.toString())
            .severidad(severidad)
            .usuarioOrigen(usuario)
            .build();
        bitacoraRepo.save(be);

        BitacoraPorTipo bpt = BitacoraPorTipo.builder()
            .periodoDia(periodoDia)
            .tipoEvento(tipoEvento)
            .timestampEvento(ahora)
            .idEntidad(idEntidad)
            .tipoEntidad(tipoEntidad)
            .detalleJson(detalle.toString())
            .severidad(severidad)
            .usuarioOrigen(usuario)
            .build();
        bitacoraPorTipoRepo.save(bpt);
    }
}