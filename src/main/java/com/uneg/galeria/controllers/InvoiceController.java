package com.uneg.galeria.controllers;

import com.uneg.galeria.models.Invoice;
import com.uneg.galeria.services.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@CrossOrigin(origins = "http://localhost:3000")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    // Procesar una venta (El Admin ingresa los IDs y el código del cliente)
    @PostMapping("/sell")
    public ResponseEntity<?> createInvoice(@RequestBody Map<String, Object> request) {
        try {
            Long obraId = Long.valueOf(request.get("obraId").toString());
            Long compradorId = Long.valueOf(request.get("compradorId").toString());
            Long adminId = Long.valueOf(request.get("adminId").toString());
            String codigo = (String) request.get("codigoSeguridad");
            String direccion = (String) request.get("direccion");

            return ResponseEntity.ok(invoiceService.crearFactura(obraId, compradorId, adminId, codigo, direccion));
        } catch (RuntimeException e) {
            // Esto permite que el frontend lea el mensaje de error real
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Reporte de todas las ventas
    @GetMapping("/report")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.obtenerTodas());
    }

    // Historial del usuario: Obras reservadas y Facturas asociadas
    @GetMapping("/user/{compradorId}")
    public ResponseEntity<Map<String, Object>> getUserHistory(@PathVariable Long compradorId) {
        try {
            return ResponseEntity.ok(invoiceService.obtenerHistorialUsuario(compradorId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}