package com.uneg.galeria.services;

import com.uneg.galeria.dto.ImportArtRequest;
import com.uneg.galeria.dto.ImportArtResponse;
import com.uneg.galeria.dto.MetSearchResult;
import com.uneg.galeria.models.*;
import com.uneg.galeria.repositories.ArtistRepository;
import com.uneg.galeria.repositories.GenreRepository;
import com.uneg.galeria.services.impl.ArtServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ArtImportService {

    @Autowired
    private TranslationService translationService;

    @Autowired
    private MetMuseumService metMuseumService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ArtServiceImpl artService;

    @Autowired
    private WikidataService wikidataService;

    private static final String DEFAULT_IMAGE = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/Question_book_alternate_icon.svg/1024px-Question_book_alternate_icon.svg.png";

    public List<MetSearchResult> buscarObras(String busquedaEspanol) {
        String busquedaIngles = translationService.spanishToEnglish(busquedaEspanol);
        List<Long> objectIds = metMuseumService.search(busquedaIngles);

        List<MetSearchResult> resultados = new ArrayList<>();
        int limite = Math.min(objectIds.size(), 10);

        for (int i = 0; i < limite; i++) {
            MetMuseumService.MetArtResponse art = metMuseumService.getObject(objectIds.get(i));
            if (art != null && art.getPrimaryImage() != null && !art.getPrimaryImage().isBlank()) {
                resultados.add(MetSearchResult.from(
                    art.getObjectID(),
                    art.getTitle(),
                    art.getArtistDisplayName(),
                    art.getPrimaryImage(),
                    art.getClassification()
                ));
            }
        }
        return resultados;
    }

    @Transactional
    public ImportArtResponse importarObra(ImportArtRequest request) {
        try {
            Long objectId = request.getObjectId();
            String tituloEspanol = request.getTituloEspanol();

            MetMuseumService.MetArtResponse metArt = metMuseumService.getObject(objectId);
            if (metArt == null) {
                return ImportArtResponse.error("No se pudo obtener la obra del MET Museum");
            }

            String tituloFinal = (tituloEspanol != null && !tituloEspanol.isBlank())
                ? tituloEspanol
                : translationService.englishToSpanish(metArt.getTitle());

            String artistaNombre = (metArt.getArtistDisplayName() != null && !metArt.getArtistDisplayName().isBlank())
                ? metArt.getArtistDisplayName()
                : "Artista Desconocido";

            Artist artista = crearOFindArtista(artistaNombre);

            String generoNombre = mapearClasificacion(metArt.getClassification());
            Genre genero = crearOFindGenero(generoNombre);

            String imagenUrl = (metArt.getPrimaryImage() != null && !metArt.getPrimaryImage().isBlank())
                ? metArt.getPrimaryImage()
                : DEFAULT_IMAGE;

            String mediumEspanol = translationService.englishToSpanish(
                metArt.getMedium() != null ? metArt.getMedium() : ""
            );

            int fechaCreacion = extraerAnio(metArt.getObjectDate());

            String tipoArt = mapearClasificacion(metArt.getClassification());

            Art obra = crearObraSegunTipo(tipoArt, tituloFinal, metArt.getTitle(), mediumEspanol, imagenUrl, fechaCreacion, artista, genero);

            Art obraGuardada = artService.guardarObra(obra);

            return ImportArtResponse.success(
                obraGuardada.getId(),
                obraGuardada.getId(),
                obraGuardada.getNombre(),
                tipoArt,
                obraGuardada.getImagenUrl()
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ImportArtResponse.error("Error al importar: " + e.getMessage());
        }
    }

    private Artist crearOFindArtista(String nombre) {
        Optional<Artist> existente = artistRepository.findByNombre(nombre);
        if (existente.isPresent()) {
            return existente.get();
        }
        Artist nuevo = new Artist();
        nuevo.setNombre(nombre);
        nuevo.setNacionalidad("Internacional");
        nuevo.setBiografia("Artista importado desde MET Museum");
        nuevo.setFechaNacimiento(java.time.LocalDate.of(1800, 1, 1));
        nuevo.setPorcentajeGanancia(10.0);
        return artistRepository.save(nuevo);
    }

    private Genre crearOFindGenero(String nombre) {
        Optional<Genre> existente = genreRepository.findByNombreIgnoreCase(nombre);
        if (existente.isPresent()) {
            return existente.get();
        }
        Genre nuevo = new Genre();
        nuevo.setNombre(nombre);
        return genreRepository.save(nuevo);
    }

    private String mapearClasificacion(String classification) {
        if (classification == null) return "Pintura";
        String lower = classification.toLowerCase();
        if (lower.contains("painting") || lower.contains("canvas") || lower.contains("panel")) return "Pintura";
        if (lower.contains("sculpture") || lower.contains("statue")) return "Escultura";
        if (lower.contains("photograph") || lower.contains("photo")) return "Fotografía";
        if (lower.contains("ceramic") || lower.contains("pottery")) return "Cerámica";
        if (lower.contains("gold") || lower.contains("silver") || lower.contains("jewelry") || lower.contains("metal")) return "Orfebrería";
        return "Pintura";
    }

    private Art crearObraSegunTipo(String tipo, String titulo, String tituloIngles,
                                   String medium, String imagenUrl, int fechaCreacion,
                                   Artist artista, Genre genero) {

        Double precioBase = 1000.0;
        String estiloWikidata = obtenerEstiloDeWikidata(tituloIngles);

        return switch (tipo) {
            case "Pintura" -> {
                Painting p = new Painting();
                p.setTecnica(medium);
                p.setEstilo(estiloWikidata != null ? estiloWikidata : "Estilo artístico");
                p.setNombre(titulo);
                p.setPrecioBase(precioBase);
                p.setFechaCreacion(fechaCreacion);
                p.setEstatus("Disponible");
                p.setImagenUrl(imagenUrl);
                p.setArtista(artista);
                p.setGenero(genero);
                yield p;
            }
            case "Escultura" -> {
                Sculpture s = new Sculpture();
                s.setMaterial(medium);
                s.setPeso(extraerPeso(null));
                s.setLargo(30.0);
                s.setAncho(30.0);
                s.setProfundidad(30.0);
                s.setNombre(titulo);
                s.setPrecioBase(precioBase);
                s.setFechaCreacion(fechaCreacion);
                s.setEstatus("Disponible");
                s.setImagenUrl(imagenUrl);
                s.setArtista(artista);
                s.setGenero(genero);
                yield s;
            }
            case "Fotografía" -> {
                Photograph f = new Photograph();
                f.setTipoImpresion(medium);
                f.setPapel("Papel de algodón");
                f.setEdicion("1/10");
                f.setNombre(titulo);
                f.setPrecioBase(precioBase);
                f.setFechaCreacion(fechaCreacion);
                f.setEstatus("Disponible");
                f.setImagenUrl(imagenUrl);
                f.setArtista(artista);
                f.setGenero(genero);
                yield f;
            }
            case "Cerámica" -> {
                Ceramic c = new Ceramic();
                c.setTipoArcilla(medium);
                c.setTemperaturaCoccion(900.0);
                c.setNombre(titulo);
                c.setPrecioBase(precioBase);
                c.setFechaCreacion(fechaCreacion);
                c.setEstatus("Disponible");
                c.setImagenUrl(imagenUrl);
                c.setArtista(artista);
                c.setGenero(genero);
                yield c;
            }
            case "Orfebrería" -> {
                Orphebrery o = new Orphebrery();
                o.setMetalBase(medium);
                o.setPurezaMetal("18K");
                o.setPeso(extraerPeso(null));
                o.setNombre(titulo);
                o.setPrecioBase(precioBase);
                o.setFechaCreacion(fechaCreacion);
                o.setEstatus("Disponible");
                o.setImagenUrl(imagenUrl);
                o.setArtista(artista);
                o.setGenero(genero);
                yield o;
            }
            default -> {
                Painting p = new Painting();
                p.setTecnica(medium);
                p.setEstilo(estiloWikidata != null ? estiloWikidata : "Estilo artístico");
                p.setNombre(titulo);
                p.setPrecioBase(precioBase);
                p.setFechaCreacion(fechaCreacion);
                p.setEstatus("Disponible");
                p.setImagenUrl(imagenUrl);
                p.setArtista(artista);
                p.setGenero(genero);
                yield p;
            }
        };
    }

    private String obtenerEstiloDeWikidata(String tituloIngles) {
        try {
            String estiloIngles = wikidataService.obtenerEstiloArtistico(tituloIngles);
            if (estiloIngles != null && !estiloIngles.isBlank()) {
                return translationService.englishToSpanish(estiloIngles);
            }
        } catch (Exception e) {
            System.err.println("No se pudo obtener estilo de Wikidata: " + e.getMessage());
        }
        return null;
    }

    private int extraerAnio(String objectDate) {
        if (objectDate == null) return 2000;
        String digits = objectDate.replaceAll("[^0-9]", "");
        if (digits.length() >= 4) {
            return Integer.parseInt(digits.substring(0, 4));
        }
        return 2000;
    }

    private Double extraerPeso(String dimensions) {
        if (dimensions == null) return 1.0;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("([\\d.]+)\\s*(kg|kilograms?)", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher m = p.matcher(dimensions);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return 1.0;
    }
}