package com.uneg.galeria.services;

import com.uneg.galeria.models.Artist;
import com.uneg.galeria.repositories.ArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArtistService {

    @Autowired private ArtistRepository artistRepository;

    public List<Artist> getAllArtists() { return artistRepository.findAll(); }

    public Artist getArtistById(Long id) { return artistRepository.findById(id).orElse(null); }

    public Artist saveArtist(Artist artist) { return artistRepository.save(artist); }

    public void deleteArtist(Long id) { artistRepository.deleteById(id); }
}