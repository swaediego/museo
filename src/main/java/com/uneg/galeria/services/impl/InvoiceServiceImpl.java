package com.uneg.galeria.services.impl;

import com.uneg.galeria.models.*;
import com.uneg.galeria.repositories.*;
import com.uneg.galeria.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ArtRepository artRepository;
    @Autowired private BuyerRepository buyerRepository;
    @Autowired private AdminRepository adminRepository;

    @Override
    @Transactional
    public Invoice crearFactura(Long obraId, Long compradorId, Long adminId, String codigoSeguridad, String direccion) {

        // 1. Validaciones de existencia
        Art obra = artRepository.findById(obraId)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada"));
        Buyer comprador = buyerRepository.findById(compradorId)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado"));
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin no encontrado"));

        // 2. REGLA DE NEGOCIO: La obra debe estar disponible
        if (!"Reservada".equalsIgnoreCase(obra.getEstatus())) {
            throw new RuntimeException("Solo se pueden facturar obras que hayan sido reservadas previamente.");
        }

        // 3. REGLA DE NEGOCIO: Validar código de seguridad y membresía
        if (!comprador.getMembresiaPaga()) {
            throw new RuntimeException("El comprador no ha pagado la membresía.");
        }
        if (!comprador.getCodigoSeguridad().equals(codigoSeguridad)) {
            throw new RuntimeException("Código de seguridad inválido.");
        }

        // 4. CÁLCULOS FINANCIEROS
        double precioBase = obra.getPrecioBase();
        double iva = precioBase * 0.16; // Asumiendo 16%, puedes ajustarlo
        double total = precioBase + iva;

        // Ganancia del museo (tomada del porcentaje pactado con el artista)
        double porcentajeMuseo = obra.getArtista().getPorcentajeGanancia() / 100.0;
        double montoGananciaMuseo = precioBase * porcentajeMuseo;

        // 5. CREAR LA FACTURA
        Invoice factura = new Invoice();
        factura.setObra(obra);
        factura.setComprador(comprador);
        factura.setAdministrador(admin);
        factura.setFechaVenta(LocalDateTime.now());
        factura.setSubtotal(precioBase);
        factura.setIva(iva);
        factura.setTotal(total);
        factura.setPorcentajeGanancia(obra.getArtista().getPorcentajeGanancia());
        factura.setMontoGanancia(montoGananciaMuseo);
        factura.setDireccionDestino(direccion);

        // 6. ACTUALIZAR ESTATUS DE LA OBRA
        obra.setEstatus("Vendido");
        artRepository.save(obra);

        return invoiceRepository.save(factura);
    }

    @Override
    public List<Invoice> listarVentasPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return invoiceRepository.findByFechaVentaBetween(inicio, fin);
    }

    @Override
    public Double calcularTotalRecaudado(LocalDateTime inicio, LocalDateTime fin) {
        Double total = listarVentasPorPeriodo(inicio, fin).stream()
                .mapToDouble(Invoice::getTotal)
                .sum();
        return total != null ? total : 0.0;
    }

    @Override
    public List<Invoice> obtenerTodas() {
        return invoiceRepository.findAll();
    }

    @Override
    public Map<String, Object> obtenerHistorialUsuario(Long compradorId) {
        // Validar que el comprador exista
        if (!buyerRepository.existsById(compradorId)) {
            throw new RuntimeException("Comprador no encontrado");
        }

        List<Art> reservadas = artRepository.findByCompradorReservaIdAndEstatus(compradorId, "Reservada");
        List<Invoice> facturas = invoiceRepository.findByCompradorId(compradorId);

        Map<String, Object> response = new HashMap<>();
        response.put("reservas", reservadas);
        response.put("facturas", facturas);

        return response;
    }

    @Override
    public Optional<Invoice> obtenerFacturaPorId(Long id) {
        return invoiceRepository.findById(id);
    }
}