package com.uneg.galeria.services;

import com.uneg.galeria.dto.ImportArtRequest;
import com.uneg.galeria.dto.ImportArtResponse;
import com.uneg.galeria.dto.MetSearchResult;
import com.uneg.galeria.models.*;
import com.uneg.galeria.repositories.ArtRepository;
import com.uneg.galeria.repositories.ArtistRepository;
import com.uneg.galeria.repositories.GenreRepository;
import com.uneg.galeria.services.impl.ArtServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private ArtRepository artRepository;

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

            Optional<Art> obraExistente = artRepository.findByMetObjectId(objectId);
            if (obraExistente.isPresent()) {
                Art existente = obraExistente.get();
                String existingType = existente.getClass().getSimpleName();
                if ("Painting".equals(existingType)) existingType = "Pintura";
                else if ("Sculpture".equals(existingType)) existingType = "Escultura";
                else if ("Photograph".equals(existingType)) existingType = "Fotografía";
                else if ("Ceramic".equals(existingType)) existingType = "Cerámica";
                else if ("Orphebrery".equals(existingType)) existingType = "Orfebrería";

                return ImportArtResponse.success(
                    existente.getId(),
                    existente.getId(),
                    existente.getNombre(),
                    existingType,
                    existente.getImagenUrl()
                );
            }

            String tituloEspanol = request.getTituloEspanol();

            MetMuseumService.MetArtResponse metArt = metMuseumService.getObject(objectId);
            if (metArt == null) {
                throw new RuntimeException("No se pudo obtener la obra del MET Museum (ID: " + objectId + ")");
            }

            String tituloFinal = (tituloEspanol != null && !tituloEspanol.isBlank())
                ? tituloEspanol
                : translationService.englishToSpanish(metArt.getTitle());

            String artistaNombre = (metArt.getArtistDisplayName() != null && !metArt.getArtistDisplayName().isBlank())
                ? metArt.getArtistDisplayName()
                : "Artista Desconocido";

            Artist artista = crearOFindArtista(artistaNombre);

            String imagenUrl = (metArt.getPrimaryImage() != null && !metArt.getPrimaryImage().isBlank())
                ? metArt.getPrimaryImage()
                : DEFAULT_IMAGE;

            String mediumEspanol = translationService.englishToSpanish(
                metArt.getMedium() != null ? metArt.getMedium() : ""
            );

            int fechaCreacion = extraerAnio(metArt.getObjectDate());

            // 1. Prioritize Ollama AI analysis
            com.uneg.galeria.dto.OllamaArtResponse ollamaData = null;
            try {
                ollamaData = ollamaService.analizarObra(
                    metArt.getTitle(),
                    metArt.getArtistDisplayName(),
                    metArt.getMedium(),
                    metArt.getDimensions(),
                    metArt.getObjectDate(),
                    metArt.getClassification()
                );
            } catch (Exception e) {
                throw new RuntimeException("Error en la conexión con el servicio de IA (Ollama): " + e.getMessage(), e);
            }

            if (ollamaData == null) {
                throw new RuntimeException("El servicio de IA (Ollama) no devolvió una respuesta válida.");
            }

            String tipoArt = traducirGenero(ollamaData.getGenre());
            String clasificacionSugeridaIA = tipoArt; // Store the original AI recommendation

            // Fallback to MET classification if Ollama returned null or unrecognized genre
            if (tipoArt == null) {
                tipoArt = mapearClasificacion(metArt.getClassification());
            }

            Genre genero = crearOFindGenero(tipoArt);

            Art obra = crearObraSegunTipo(tipoArt, tituloFinal, metArt.getTitle(), mediumEspanol, imagenUrl, fechaCreacion, artista, genero, ollamaData);
            obra.setMetObjectId(objectId);

            Art obraGuardada = artService.guardarObra(obra);

            ImportArtResponse response = ImportArtResponse.success(
                obraGuardada.getId(),
                obraGuardada.getId(),
                obraGuardada.getNombre(),
                tipoArt,
                obraGuardada.getImagenUrl()
            );
            response.setClasificacionSugeridaIA(clasificacionSugeridaIA != null ? clasificacionSugeridaIA : tipoArt);
            if (ollamaData.getAttributes() != null) {
                response.setDetallesExtraidos(mapearDetalles(ollamaData.getAttributes()));
            }
            return response;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Artist crearOFindArtista(String nombre) {
        Optional<Artist> existente = artistRepository.findByNombreIgnoreCase(nombre);
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
                                   Artist artista, Genre genero, com.uneg.galeria.dto.OllamaArtResponse ollamaData) {

        Double precioBase = 1000.0;
        String estiloWikidata = obtenerEstiloDeWikidata(tituloIngles);

        String estiloFinal = null;
        if (ollamaData != null && ollamaData.getAttributes() != null) {
            estiloFinal = ollamaData.getAttributes().getEstilo();
        }
        if (estiloFinal == null) {
            estiloFinal = estiloWikidata != null ? estiloWikidata : "Estilo artístico";
        }

        return switch (tipo) {
            case "Pintura" -> {
                Painting p = new Painting();
                String tecnica = (ollamaData != null && ollamaData.getAttributes() != null && ollamaData.getAttributes().getTecnica() != null)
                    ? ollamaData.getAttributes().getTecnica() : medium;
                p.setTecnica(tecnica);
                p.setEstilo(estiloFinal);
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
                String material = (ollamaData != null && ollamaData.getAttributes() != null && ollamaData.getAttributes().getMaterial() != null)
                    ? ollamaData.getAttributes().getMaterial() : medium;
                s.setMaterial(material);
                Double peso = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getPeso() : null;
                s.setPeso(peso != null ? peso : 1.0);
                Double largo = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getLargo() : null;
                s.setLargo(largo != null ? largo : 30.0);
                Double ancho = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getAncho() : null;
                s.setAncho(ancho != null ? ancho : 30.0);
                Double profundidad = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getProfundidad() : null;
                s.setProfundidad(profundidad != null ? profundidad : 30.0);
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
                String tipoImpresion = (ollamaData != null && ollamaData.getAttributes() != null && ollamaData.getAttributes().getTipoImpresion() != null)
                    ? ollamaData.getAttributes().getTipoImpresion() : medium;
                f.setTipoImpresion(tipoImpresion);
                String papel = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getPapel() : null;
                f.setPapel(papel != null ? papel : "Papel de algodón");
                String edicion = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getEdicion() : null;
                f.setEdicion(edicion != null ? edicion : "1/10");
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
                String tipoArcilla = (ollamaData != null && ollamaData.getAttributes() != null && ollamaData.getAttributes().getTipoArcilla() != null)
                    ? ollamaData.getAttributes().getTipoArcilla() : medium;
                c.setTipoArcilla(tipoArcilla);
                Double temp = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getTemperaturaCoccion() : null;
                c.setTemperaturaCoccion(temp != null ? temp : 900.0);
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
                String metalBase = (ollamaData != null && ollamaData.getAttributes() != null && ollamaData.getAttributes().getMetalBase() != null)
                    ? ollamaData.getAttributes().getMetalBase() : medium;
                o.setMetalBase(metalBase);
                String pureza = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getPurezaMetal() : null;
                o.setPurezaMetal(pureza != null ? pureza : "18K");
                Double pesoOrf = (ollamaData != null && ollamaData.getAttributes() != null)
                    ? ollamaData.getAttributes().getPeso() : null;
                o.setPeso(pesoOrf != null ? pesoOrf : 1.0);
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
                p.setEstilo(estiloFinal);
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

    private String traducirGenero(String genre) {
        if (genre == null) return null;
        return switch (genre.trim().toLowerCase()) {
            case "painting" -> "Pintura";
            case "sculpture" -> "Escultura";
            case "orphebrery" -> "Orfebrería";
            case "photograph" -> "Fotografía";
            case "ceramic" -> "Cerámica";
            default -> null;
        };
    }

    private Map<String, Object> mapearDetalles(com.uneg.galeria.dto.OllamaArtResponse.Attributes attrs) {
        Map<String, Object> map = new HashMap<>();
        if (attrs == null) return map;
        if (attrs.getTecnica() != null) map.put("tecnica", attrs.getTecnica());
        if (attrs.getEstilo() != null) map.put("estilo", attrs.getEstilo());
        if (attrs.getMaterial() != null) map.put("material", attrs.getMaterial());
        if (attrs.getPeso() != null) map.put("peso", attrs.getPeso());
        if (attrs.getLargo() != null) map.put("largo", attrs.getLargo());
        if (attrs.getAncho() != null) map.put("ancho", attrs.getAncho());
        if (attrs.getProfundidad() != null) map.put("profundidad", attrs.getProfundidad());
        if (attrs.getPurezaMetal() != null) map.put("purezaMetal", attrs.getPurezaMetal());
        if (attrs.getMetalBase() != null) map.put("metalBase", attrs.getMetalBase());
        if (attrs.getTipoImpresion() != null) map.put("tipoImpresion", attrs.getTipoImpresion());
        if (attrs.getPapel() != null) map.put("papel", attrs.getPapel());
        if (attrs.getEdicion() != null) map.put("edicion", attrs.getEdicion());
        if (attrs.getTipoArcilla() != null) map.put("tipoArcilla", attrs.getTipoArcilla());
        if (attrs.getTemperaturaCoccion() != null) map.put("temperaturaCoccion", attrs.getTemperaturaCoccion());
        if (attrs.getFechaCreacion() != null) map.put("fechaCreacion", attrs.getFechaCreacion());
        return map;
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