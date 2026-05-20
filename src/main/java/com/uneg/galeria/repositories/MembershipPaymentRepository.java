package com.uneg.galeria.repositories;

import com.uneg.galeria.models.MembershipPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MembershipPaymentRepository extends JpaRepository<MembershipPayment, Long> {
    // Para ver el historial de pagos de un cliente
    List<MembershipPayment> findByCompradorId(Long compradorId);

    // Para reporte de membresías por período
    List<MembershipPayment> findByFechaPagoBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}