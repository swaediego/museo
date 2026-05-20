package com.uneg.galeria.controllers;

import com.uneg.galeria.models.MembershipPayment;
import com.uneg.galeria.repositories.MembershipPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/memberships")
@CrossOrigin(origins = "http://localhost:3000")
public class MembershipController {

    @Autowired
    private MembershipPaymentRepository paymentRepository;

    @GetMapping("/report")
    public ResponseEntity<?> getMembershipsByPeriod(
            @RequestParam String inicio,
            @RequestParam String fin) {
        try {
            LocalDateTime start = LocalDateTime.parse(inicio);
            LocalDateTime end = LocalDateTime.parse(fin);
            
            List<MembershipPayment> pagos = paymentRepository.findByFechaPagoBetween(start, end);
            long totalMembresias = pagos.size();
            double totalRecaudado = totalMembresias * 10.0;
            
            return ResponseEntity.ok(Map.of(
                "pagos", pagos,
                "totalMembresias", totalMembresias,
                "totalRecaudado", totalRecaudado
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
