package com.uneg.galeria.repositories;

import com.uneg.galeria.models.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerRepository extends JpaRepository<Buyer, Long> {

    // 1. Para el Login: Buscar por nombre de usuario (login)
    Optional<Buyer> findByLogin(String login);

    // 2. recuperación de código: Buscar por correo electrónico
    Optional<Buyer> findByEmail(String email);

    // 3. Verificar si ya existe un login o email antes de registrar (Validación)
    boolean existsByLogin(String login);
    boolean existsByEmail(String email);


    @Query("SELECT b FROM Buyer b")
    List<Buyer> findAllBuyersIgnoringStatus();

    // Filtro explícito
    @Query("SELECT b FROM Buyer b WHERE b.activo = true")
    List<Buyer> findByActivoTrue();

    }