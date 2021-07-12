package tu.kielce.walczak.MusicStore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dao.ArtistRepository;
import tu.kielce.walczak.MusicStore.dao.GenreRepository;
import tu.kielce.walczak.MusicStore.dao.SubgenreRepository;
import tu.kielce.walczak.MusicStore.dto.AlbumDto;
import tu.kielce.walczak.MusicStore.entity.Album;
import tu.kielce.walczak.MusicStore.entity.Artist;
import tu.kielce.walczak.MusicStore.entity.Genre;
import tu.kielce.walczak.MusicStore.entity.Subgenre;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AlbumCRUDService {
    private AlbumRepository albumRepository;
    private GenreRepository genreRepository;
    private SubgenreRepository subgenreRepository;
    private ArtistRepository artistRepository;

    @Autowired
    public AlbumCRUDService(AlbumRepository albumRepository, GenreRepository genreRepository, SubgenreRepository subgenreRepository, ArtistRepository artistRepository) {
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.subgenreRepository = subgenreRepository;
        this.artistRepository = artistRepository;
    }

    // Add album from album dto
    public long addAlbum(AlbumDto dto){
        //Check if exists
        Album existing = albumRepository.findAlbumByAlbumTitle(dto.getTitle()).orElse(null);
        if (existing != null) {
            return -1;
        }
        //Create new album
        Album newAlbum = new Album();
        //Find or create artist
        Artist artist = artistRepository.findArtistByArtistName(dto.getArtistName()).orElse(null);
        if (artist == null){
            artist = new Artist();
            artist.setArtistName(dto.getArtistName());
            artist.setAlbums(Collections.emptySet());
            artist = artistRepository.save(artist);
        }
        newAlbum.setArtist(artist);
        //Find genres
        List<Genre> genreList = new ArrayList<>();
        for (String s: dto.getGenres()){
            genreList.add(genreRepository.findGenreByGenreName(s));
        }
        newAlbum.setGenres(genreList);
        //Find subgenres
        List<Subgenre> subgenreList = new ArrayList<>();
        for (String s: dto.getSubgenres()){
            subgenreList.add(subgenreRepository.findSubgenreBySubgenreName(s));
        }
        newAlbum.setSubgenres(subgenreList);
        //Add rest of the info
        newAlbum.setAlbumTitle(dto.getTitle());
        newAlbum.setAlbumYear(dto.getYear());
        newAlbum.setAlbumPrice(dto.getPrice());
        newAlbum.setImageUrl(dto.getImageUrl());
        return albumRepository.save(newAlbum).getAlbumId();
    }
    // Delete album by id
    // Update album from album DAO
}
