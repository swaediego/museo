package com.uneg.galeria.services;

import com.uneg.galeria.dto.BuscarObrasResponse;
import com.uneg.galeria.dto.ImportArtRequest;
import com.uneg.galeria.dto.ImportArtResponse;
import com.uneg.galeria.dto.MetSearchResult;
import com.uneg.galeria.dto.MultiSourceSearchResult;
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
    private RijksmuseumService rijksmuseumService;

    @Autowired
    private HarvardArtMuseumsService harvardService;

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

    @Autowired
    private SearchAssistanceService searchAssistanceService;

    private static final String DEFAULT_IMAGE = "https://upload.wikimedia.org/wikipedia/commons/thumb/5/59/Question_book_alternate_icon.svg/1024px-Question_book_alternate_icon.svg.png";

    public List<MetSearchResult> buscarObras(String busquedaEspanol) {
        return buscarObras(busquedaEspanol, null);
    }

    public List<MetSearchResult> buscarObras(String busquedaEspanol, String artista) {
        return buscarObrasConSugerencias(busquedaEspanol, artista).getResultados();
    }

    public BuscarObrasResponse buscarObrasConSugerencias(String busquedaEspanol, String artista) {
        String busquedaIngles = translationService.spanishToEnglish(busquedaEspanol);
        java.util.Set<MetSearchResult> resultadosSet = new java.util.LinkedHashSet<>();

        // 1. Buscar en MET
        System.out.println("[ArtImportService] Buscando en MET: " + busquedaIngles);
        List<Long> objectIds = metMuseumService.search(busquedaIngles, null, null, null, null);
        int limite = Math.min(objectIds.size(), 10);

        for (int i = 0; i < limite; i++) {
            MetMuseumService.MetArtResponse art = metMuseumService.getObject(objectIds.get(i));
            if (art != null && art.getPrimaryImage() != null && !art.getPrimaryImage().isBlank()) {
                resultadosSet.add(MetSearchResult.from(
                    art.getObjectID(),
                    art.getTitle(),
                    art.getArtistDisplayName(),
                    art.getPrimaryImage(),
                    art.getClassification()
                ));
            }
        }
        System.out.println("[ArtImportService] MET: " + resultadosSet.size() + " resultados");

        // 2. SIEMPRE buscar también en fuentes alternativas (no solo si MET falla)
        System.out.println("[ArtImportService] Buscando en Rijksmuseum y Harvard...");
        if (artista != null && !artista.isBlank()) {
            buscarEnRijksmuseum(busquedaIngles, artista, resultadosSet, 5);
            buscarEnHarvard(busquedaIngles, artista, resultadosSet, 5);
        }
        // Si no tenemos resultados aún, buscar sin artista
        if (resultadosSet.isEmpty() || resultadosSet.size() < 3) {
            buscarEnRijksmuseum(busquedaIngles, null, resultadosSet, 5);
            buscarEnHarvard(busquedaIngles, null, resultadosSet, 5);
        }
        System.out.println("[ArtImportService] Total tras todas las fuentes: " + resultadosSet.size() + " resultados");

        List<MetSearchResult> resultados = new ArrayList<>(resultadosSet);

        // Si no hay resultados, buscar sugerencias
        if (resultados.isEmpty()) {
            System.out.println("[ArtImportService] No se encontró la obra, buscando sugerencias...");

            if (artista != null && !artista.isBlank()) {
                List<MetSearchResult> sugerencias = buscarSugerenciasPorArtista(artista);
                if (!sugerencias.isEmpty()) {
                    return BuscarObrasResponse.noEncontrado(busquedaEspanol, sugerencias);
                }
            }

            List<MetSearchResult> sugerencias = buscarSugerenciasPorTitulo(busquedaIngles);
            return BuscarObrasResponse.noEncontrado(busquedaEspanol, sugerencias);
        }

        return BuscarObrasResponse.exito(resultados);
    }

    private List<MetSearchResult> buscarSugerenciasPorArtista(String artista) {
        java.util.Set<MetSearchResult> sugerencias = new java.util.LinkedHashSet<>();
        
        try {
            // Buscar obras del mismo artista en MET
            List<Long> objectIds = metMuseumService.search(artista, null, null, null, null);
            int limite = Math.min(objectIds.size(), 5);
            
            for (int i = 0; i < limite; i++) {
                MetMuseumService.MetArtResponse art = metMuseumService.getObject(objectIds.get(i));
                if (art != null && art.getPrimaryImage() != null && !art.getPrimaryImage().isBlank()) {
                    sugerencias.add(MetSearchResult.from(
                        art.getObjectID(),
                        art.getTitle(),
                        art.getArtistDisplayName(),
                        art.getPrimaryImage(),
                        art.getClassification()
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("[ArtImportService] Error al buscar sugerencias por artista: " + e.getMessage());
        }
        
        return new ArrayList<>(sugerencias);
    }

    private List<MetSearchResult> buscarSugerenciasPorTitulo(String tituloParcial) {
        java.util.Set<MetSearchResult> sugerencias = new java.util.LinkedHashSet<>();
        
        try {
            // Buscar con palabras parciales del título
            String[] palabras = tituloParcial.split("\\s+");
            for (String palabra : palabras) {
                if (palabra.length() > 3) {  // Ignorar palabras muy cortas
                    List<Long> objectIds = metMuseumService.search(palabra, null, null, null, null);
                    int limite = Math.min(objectIds.size(), 3);
                    
                    for (int i = 0; i < limite; i++) {
                        MetMuseumService.MetArtResponse art = metMuseumService.getObject(objectIds.get(i));
                        if (art != null && art.getPrimaryImage() != null && !art.getPrimaryImage().isBlank()) {
                            sugerencias.add(MetSearchResult.from(
                                art.getObjectID(),
                                art.getTitle(),
                                art.getArtistDisplayName(),
                                art.getPrimaryImage(),
                                art.getClassification()
                            ));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ArtImportService] Error al buscar sugerencias por título: " + e.getMessage());
        }
        
        return new ArrayList<>(sugerencias);
    }

    private void buscarEnRijksmuseum(String query, String artistName, java.util.Set<MetSearchResult> resultados, int limite) {
        try {
            List<RijksmuseumService.RijksmuseumResult> rijksResults = 
                rijksmuseumService.search(query, artistName);
            
            for (int i = 0; i < Math.min(rijksResults.size(), limite); i++) {
                RijksmuseumService.RijksmuseumResult r = rijksResults.get(i);
                resultados.add(MetSearchResult.fromRijksmuseum(
                    r.getTitulo(),
                    r.getArtista(),
                    r.getImagenUrl(),
                    r.getClasificacion(),
                    r.getObjectNumber()
                ));
            }
        } catch (Exception e) {
            System.err.println("Error en búsqueda Rijksmuseum: " + e.getMessage());
        }
    }

    private void buscarEnHarvard(String query, String artistName, java.util.Set<MetSearchResult> resultados, int limite) {
        try {
            List<HarvardArtMuseumsService.HarvardResult> harvardResults = 
                harvardService.search(query, artistName);
            
            for (int i = 0; i < Math.min(harvardResults.size(), limite); i++) {
                HarvardArtMuseumsService.HarvardResult h = harvardResults.get(i);
                resultados.add(MetSearchResult.fromHarvard(
                    h.getTitulo(),
                    h.getArtista(),
                    h.getImagenUrl(),
                    h.getClasificacion(),
                    h.getId()
                ));
            }
        } catch (Exception e) {
            System.err.println("Error en búsqueda Harvard: " + e.getMessage());
        }
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

                ImportArtResponse resp = ImportArtResponse.success(
                    existente.getId(),
                    existente.getId(),
                    existente.getNombre(),
                    existingType,
                    existente.getImagenUrl()
                );
                resp.setMessage("Esta obra ya existe en el sistema");
                return resp;
            }

            String tituloEspanol = request.getTituloEspanol();

            MetMuseumService.MetArtResponse metArt = metMuseumService.getObject(objectId);
            if (metArt == null) {
                throw new RuntimeException("No se pudo obtener la obra del MET Museum (ID: " + objectId + ")");
            }

            String tituloFinal = (tituloEspanol != null && !tituloEspanol.isBlank())
                ? tituloEspanol.trim()
                : translationService.englishToSpanish(metArt.getTitle()).trim();

            String artistaNombre = (metArt.getArtistDisplayName() != null && !metArt.getArtistDisplayName().isBlank())
                ? metArt.getArtistDisplayName().toLowerCase().trim()
                : "artista desconocido";

            // Verificar duplicado por título + artista normalizado (case-insensitive)
            Optional<Art> duplicado = artRepository.findAll().stream()
                .filter(a -> a.getNombre().toLowerCase().trim().equals(tituloFinal)
                          && a.getArtista().getNombre().toLowerCase().trim().equals(artistaNombre))
                .findFirst();

            if (duplicado.isPresent()) {
                Art existente = duplicado.get();
                String existingType = existente.getClass().getSimpleName();
                if ("Painting".equals(existingType)) existingType = "Pintura";
                else if ("Sculpture".equals(existingType)) existingType = "Escultura";
                else if ("Photograph".equals(existingType)) existingType = "Fotografía";
                else if ("Ceramic".equals(existingType)) existingType = "Cerámica";
                else if ("Orphebrery".equals(existingType)) existingType = "Orfebrería";

                ImportArtResponse resp = ImportArtResponse.success(
                    existente.getId(),
                    existente.getId(),
                    existente.getNombre(),
                    existingType,
                    existente.getImagenUrl()
                );
                resp.setMessage("Esta obra ya existe en el sistema (duplicado detectado por título y artista)");
                return resp;
            }

            // artistaNombre en formato correcto para guardar (primera mayúscula) pero normalizado
            String artistaNombreRaw = (metArt.getArtistDisplayName() != null && !metArt.getArtistDisplayName().isBlank())
                ? metArt.getArtistDisplayName()
                : "Artista Desconocido";

            Artist artista = crearOFindArtista(artistaNombreRaw.toLowerCase().trim());

            String imagenUrl = (metArt.getPrimaryImage() != null && !metArt.getPrimaryImage().isBlank())
                ? metArt.getPrimaryImage()
                : DEFAULT_IMAGE;

            String mediumEspanol = translationService.englishToSpanish(
                metArt.getMedium() != null ? metArt.getMedium() : ""
            );

            int fechaCreacion = extraerAnio(metArt.getObjectDate());

            // 1. Intentar análisis con IA (Ollama) - opcional, no bloquea importación
            com.uneg.galeria.dto.OllamaArtResponse ollamaData = null;
            String tipoArt = null;
            String clasificacionSugeridaIA = null;
            
            try {
                ollamaData = ollamaService.analizarObra(
                    metArt.getTitle(),
                    metArt.getArtistDisplayName(),
                    metArt.getMedium(),
                    metArt.getDimensions(),
                    metArt.getObjectDate(),
                    metArt.getClassification()
                );
                
                if (ollamaData != null && ollamaData.getGenre() != null) {
                    tipoArt = traducirGenero(ollamaData.getGenre());
                    clasificacionSugeridaIA = tipoArt;
                    System.out.println("[ArtImportService] Ollama clasificó: " + tipoArt);
                }
            } catch (Exception e) {
                System.err.println("[ArtImportService] Ollama no disponible, usando fallback MET: " + e.getMessage());
            }

            // Fallback: clasificar según el MET si Ollama falló o devolvió género desconocido
            if (tipoArt == null || tipoArt.isBlank()) {
                tipoArt = mapearClasificacion(metArt.getClassification());
                System.out.println("[ArtImportService] Usando clasificación MET: " + tipoArt);
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
            if (ollamaData != null && ollamaData.getAttributes() != null) {
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

        // Consultar Wikidata para datos reales
        java.util.Map<String, String> datosWikidata = wikidataService.obtenerDatosArtista(nombre);

        String nationality = datosWikidata.get("nationality");
        String birthDateStr = datosWikidata.get("birthDate");
        String description = datosWikidata.get("description");

        nuevo.setNacionalidad(nationality != null ? nationality : "Internacional");

        if (birthDateStr != null && birthDateStr.matches("\\d{4}")) {
            try {
                nuevo.setFechaNacimiento(java.time.LocalDate.of(Integer.parseInt(birthDateStr), 1, 1));
            } catch (Exception e) {
                nuevo.setFechaNacimiento(java.time.LocalDate.of(1800, 1, 1));
            }
        } else {
            nuevo.setFechaNacimiento(java.time.LocalDate.of(1800, 1, 1));
        }

        nuevo.setBiografia(description != null ? description : "Artista importado desde MET Museum");
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
            case "orfebrery" -> "Orfebrería";
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