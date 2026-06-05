package com.uneg.galeria.history.controller;

import com.uneg.galeria.history.domain.AuditoriaVenta;
import com.uneg.galeria.history.domain.BitacoraEvento;
import com.uneg.galeria.history.domain.BitacoraPorTipo;
import com.uneg.galeria.history.domain.HistorialPrecio;
import com.uneg.galeria.history.domain.VentaPorObra;
import com.uneg.galeria.history.repository.AuditoriaVentaRepository;
import com.uneg.galeria.history.repository.BitacoraEventoRepository;
import com.uneg.galeria.history.repository.BitacoraPorTipoRepository;
import com.uneg.galeria.history.repository.HistorialPrecioRepository;
import com.uneg.galeria.history.repository.VentaPorObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistorialPrecioRepository precioRepo;
    private final AuditoriaVentaRepository ventaRepo;
    private final BitacoraEventoRepository bitacoraRepo;
    private final BitacoraPorTipoRepository bitacoraPorTipoRepo;
    private final VentaPorObraRepository ventaPorObraRepo;

    @GetMapping("/precios/{idRelacional}")
    public ResponseEntity<List<HistorialPrecio>> historialPrecios(@PathVariable Integer idRelacional) {
        return ResponseEntity.ok(precioRepo.findByIdRelacional(idRelacional));
    }

    @GetMapping("/ventas")
    public ResponseEntity<List<AuditoriaVenta>> ventas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<AuditoriaVenta> resultado = new ArrayList<>();
        LocalDate mes = desde.withDayOfMonth(1);
        while (!mes.isAfter(hasta)) {
            String periodo = mes.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            resultado.addAll(ventaRepo.findByKeyPeriodo(periodo));
            mes = mes.plusMonths(1);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/bitacora")
    public ResponseEntity<List<BitacoraEvento>> bitacora(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<BitacoraEvento> resultado = new ArrayList<>();
        LocalDate dia = desde;
        while (!dia.isAfter(hasta)) {
            String periodoDia = dia.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            resultado.addAll(bitacoraRepo.findByKeyPeriodoDia(periodoDia));
            dia = dia.plusDays(1);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/bitacora/{tipo}")
    public ResponseEntity<List<BitacoraPorTipo>> bitacoraPorTipo(
            @PathVariable String tipo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {

        List<BitacoraPorTipo> resultado = new ArrayList<>();
        LocalDate dia = desde;
        while (!dia.isAfter(hasta)) {
            String periodoDia = dia.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            resultado.addAll(bitacoraPorTipoRepo.findByPeriodoDiaAndTipoEvento(periodoDia, tipo));
            dia = dia.plusDays(1);
        }
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/gerencial/ventas-por-obra")
    public ResponseEntity<List<VentaPorObra>> ventasPorObra() {
        return ResponseEntity.ok(ventaPorObraRepo.findAll());
    }

    @GetMapping("/gerencial/ventas-por-obra/{idRelacional}")
    public ResponseEntity<List<VentaPorObra>> ventasPorObra(@PathVariable Integer idRelacional) {
        List<VentaPorObra> ventas = ventaPorObraRepo.findByIdRelacional(idRelacional);
        return ventas.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(ventas);
    }

    @GetMapping("/precios/{idRelacional}/csv")
    public ResponseEntity<String> exportarPreciosCsv(@PathVariable Integer idRelacional) {
        List<HistorialPrecio> historial = precioRepo.findByIdRelacional(idRelacional);
        StringBuilder csv = new StringBuilder();
        csv.append("id_relacional,fecha_cambio,precio_anterior,precio_nuevo,motivo,usuario_admin\n");
        for (HistorialPrecio hp : historial) {
            csv.append(hp.getIdRelacional()).append(",");
            csv.append(hp.getFechaCambio()).append(",");
            csv.append(hp.getPrecioAnterior()).append(",");
            csv.append(hp.getPrecioNuevo()).append(",");
            csv.append(hp.getMotivo()).append(",");
            csv.append(hp.getUsuarioAdmin()).append("\n");
        }
        return ResponseEntity.ok()
            .header("Content-Type", "text/csv")
            .header("Content-Disposition", "attachment; filename=historial_precios_" + idRelacional + ".csv")
            .body(csv.toString());
    }
}