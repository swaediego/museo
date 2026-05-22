package com.uneg.galeria.services.impl;

import com.uneg.galeria.documents.ArtCatalogDocument;
import com.uneg.galeria.documents.ArtCatalogDocument.EmbeddedArtist;
import com.uneg.galeria.models.Art;
import com.uneg.galeria.models.Buyer;
import com.uneg.galeria.models.Ceramic;
import com.uneg.galeria.models.Orphebrery;
import com.uneg.galeria.models.Photograph;
import com.uneg.galeria.models.Painting;
import com.uneg.galeria.models.Sculpture;
import com.uneg.galeria.repositories.ArtRepository;
import com.uneg.galeria.repositories.BuyerRepository;
import com.uneg.galeria.services.ArtService;
import com.uneg.galeria.services.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ArtServiceImpl implements ArtService {

    @Autowired
    private ArtRepository artRepository;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private CatalogService catalogService;

    @Override
    @Transactional(readOnly = true)
    public List<Art> obtenerTodasDisponibles() {

        return artRepository.findByEstatusOrderByPrecioBaseAsc("Disponible");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Art> obtenerTodas() {
        return artRepository.findAll();
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
        Art saved = artRepository.save(obra);
        syncToMongo(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean esReservada(Long id) {
        return artRepository.findById(id)
            .map(obra -> "Reservada".equalsIgnoreCase(obra.getEstatus()))
            .orElse(false);
    }

    @Override
    @Transactional
    public void eliminarObra(Long id) {
        artRepository.deleteById(id);
        catalogService.deleteByIdRelacional(id);
    }

    @Override
    @Transactional
    public void reservarObra(Long obraId, Long compradorId, String securityCode) {
        Art obra = artRepository.findById(obraId).orElseThrow(() -> new RuntimeException("Obra no encontrada"));
        Buyer comprador = buyerRepository.findById(compradorId).orElseThrow(() -> new RuntimeException("Comprador no encontrado"));

        if (!"Disponible".equalsIgnoreCase(obra.getEstatus())) {
            throw new RuntimeException("La obra no está disponible para reservar.");
        }

        if (comprador.getMembresiaPaga() == null || !comprador.getMembresiaPaga()) {
            throw new RuntimeException("Debe pagar la membresía de $10.00 antes de reservar.");
        }

        if (comprador.getCodigoSeguridad() == null || !comprador.getCodigoSeguridad().equals(securityCode)) {
            throw new RuntimeException("Código de seguridad incorrecto.");
        }

        obra.setEstatus("Reservada");
        obra.setCompradorReserva(comprador);
        Art saved = artRepository.save(obra);
        syncToMongo(saved);
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
        obra.setCompradorReserva(null);

        Art saved = artRepository.save(obra);
        syncToMongo(saved);
        return saved;
    }

    private void syncToMongo(Art obra) {
        ArtCatalogDocument doc = new ArtCatalogDocument();
        doc.setIdRelacional(obra.getId());

        catalogService.findByIdRelacional(obra.getId()).ifPresent(existing -> {
            doc.setId(existing.getId());
        });
        doc.setNombre(obra.getNombre());
        doc.setPrecio(obra.getPrecioBase());
        doc.setEstatus(obra.getEstatus());
        doc.setImagenUrl(obra.getImagenUrl());
        doc.setFechaCreacion(obra.getFechaCreacion());

        EmbeddedArtist embeddedArtist = new EmbeddedArtist();
        if (obra.getArtista() != null) {
            embeddedArtist.setIdArtistaRelacional(obra.getArtista().getId());
            embeddedArtist.setNombre(obra.getArtista().getNombre());
            embeddedArtist.setNacionalidad(obra.getArtista().getNacionalidad());
            embeddedArtist.setBiografia(obra.getArtista().getBiografia());
        }
        doc.setArtista(embeddedArtist);

        if (obra.getGenero() != null) {
            doc.setGenero(obra.getGenero().getNombre());
        }

        doc.setMetObjectId(obra.getMetObjectId());

        Map<String, Object> detalles = new HashMap<>();

        if (obra instanceof Painting painting) {
            detalles.put("tecnica", painting.getTecnica());
            detalles.put("estilo", painting.getEstilo());
        } else if (obra instanceof Sculpture sculpture) {
            detalles.put("material", sculpture.getMaterial());
            detalles.put("peso", sculpture.getPeso());
            Map<String, Object> dimensiones = new HashMap<>();
            dimensiones.put("largo", sculpture.getLargo());
            dimensiones.put("ancho", sculpture.getAncho());
            dimensiones.put("profundidad", sculpture.getProfundidad());
            detalles.put("dimensiones", dimensiones);
        } else if (obra instanceof Photograph photograph) {
            detalles.put("tipoImpresion", photograph.getTipoImpresion());
            detalles.put("papel", photograph.getPapel());
            detalles.put("edicion", photograph.getEdicion());
        } else if (obra instanceof Ceramic ceramic) {
            detalles.put("tipoArcilla", ceramic.getTipoArcilla());
            detalles.put("temperaturaCoccion", ceramic.getTemperaturaCoccion());
        } else if (obra instanceof Orphebrery orphebrery) {
            detalles.put("purezaMetal", orphebrery.getPurezaMetal());
            detalles.put("peso", orphebrery.getPeso());
            detalles.put("metalBase", orphebrery.getMetalBase());
        }

        doc.setDetallesEspecificos(detalles);
        catalogService.save(doc);
    }
}