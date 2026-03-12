package com.uneg.galeria.services;

import com.uneg.galeria.models.Invoice;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface InvoiceService {
    Invoice crearFactura(Long obraId, Long compradorId, Long adminId, String codigoSeguridad, String direccion);

    // Reportes para el administrador
    List<Invoice> listarVentasPorPeriodo(LocalDateTime inicio, LocalDateTime fin);

    // Resumen total de facturación
    Double calcularTotalRecaudado(LocalDateTime inicio, LocalDateTime fin);

    List<Invoice> obtenerTodas();

    // Historial del usuario: Obras reservadas y Facturas asociadas
    Map<String, Object> obtenerHistorialUsuario(Long compradorId);

    // Obtener una factura por su ID
    Optional<Invoice> obtenerFacturaPorId(Long id);
}