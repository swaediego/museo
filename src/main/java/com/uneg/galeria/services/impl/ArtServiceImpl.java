package com.uneg.galeria.services.impl;

import com.uneg.galeria.models.Art;
import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.repositories.ArtRepository;
import com.uneg.galeria.repositories.BuyerRepository;
import com.uneg.galeria.services.ArtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ArtServiceImpl implements ArtService {

    @Autowired
    private ArtRepository artRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Art> obtenerTodasDisponibles() {

        return artRepository.findByEstatusOrderByPrecioBaseAsc("Disponible");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Art> buscarPorGenero(String nombreGenero) {
        return artRepository.findByGeneroNombreIgnoreCaseAndEstatus(nombreGenero, "Disponible");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Art> buscarPorArtista(Long artistaId) {
        return artRepository.findByArtistaId(artistaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Art> listarPorPrecioMenorAMayor() {
        return artRepository.findByEstatusOrderByPrecioBaseAsc("Disponible");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Art> obtenerPorId(Long id) {
        return artRepository.findById(id);
    }

    @Override
    @Transactional
    public Art guardarObra(Art obra) {
        return artRepository.save(obra);
    }

    @Override
    @Transactional
    public void eliminarObra(Long id) {
        artRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void reservarObra(Long obraId, Long compradorId) {
        Art obra = artRepository.findById(obraId).orElseThrow(() -> new RuntimeException("Obra no encontrada"));
        Buyer comprador = buyerRepository.findById(compradorId).orElseThrow(() -> new RuntimeException("Comprador no encontrado"));

        if (!"Disponible".equalsIgnoreCase(obra.getEstatus())) {
            throw new RuntimeException("La obra no está disponible para reservar.");
        }

        obra.setEstatus("Reservada");
        obra.setCompradorReserva(comprador);
        artRepository.save(obra);
    }

    @Override
    @Transactional
    public Art cancelarReserva(Long artId) {
        Art obra = artRepository.findById(artId)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + artId));

        if (!"Reservada".equalsIgnoreCase(obra.getEstatus())) {
            throw new RuntimeException("La obra no está reservada, por lo que no se puede cancelar la reserva.");
        }

        obra.setEstatus("Disponible");
        obra.setCompradorReserva(null); // Elimina la referencia al comprador

        return artRepository.save(obra);
    }
}