package com.uneg.galeria.services;

import com.uneg.galeria.models.Art;
import java.util.List;
import java.util.Optional;

public interface ArtService {
    // Listar todas las obras para la galería principal
    List<Art> obtenerTodasDisponibles();

    // Listar TODAS las obras sin filtro (admin)
    List<Art> obtenerTodas();

    // Filtros
    List<Art> buscarPorGenero(String nombreGenero);
    List<Art> buscarPorArtista(Long artistaId);
    List<Art> listarPorPrecioMenorAMayor();

    // Para ver el detalle de una obra específica
    Optional<Art> obtenerPorId(Long id);

    // CRUD para el Administrador
    Art guardarObra(Art obra);
    boolean esReservada(Long id);
    void eliminarObra(Long id);


    void reservarObra(Long obraId, Long compradorId);

    Art cancelarReserva(Long artId);
}