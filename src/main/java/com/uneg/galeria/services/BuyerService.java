package com.uneg.galeria.services;

import com.uneg.galeria.models.Buyer;
import java.util.List;
import java.util.Optional;

public interface BuyerService {
    Buyer registrarComprador(Buyer buyer);
    boolean procesarPagoMembresia(Long buyerId, String metodoPago);
    String recuperarCodigoSeguridad(String email, List<String> respuestas);
    Optional<Buyer> buscarPorLogin(String login);
    List<Buyer> listarTodos();
    Buyer login(String login, String password);
    // Métodos nuevos necesarios para los CRUDS y actualizaciones
    Optional<Buyer> obtenerPorId(Long id);
    Buyer guardarComprador(Buyer buyer);
    void desactivarComprador(Long id);
}