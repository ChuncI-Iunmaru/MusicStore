package tu.kielce.walczak.MusicStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tu.kielce.walczak.MusicStore.dto.AlbumDto;
import tu.kielce.walczak.MusicStore.service.AlbumCRUDService;
import tu.kielce.walczak.MusicStore.service.RecommendationService;

import java.util.List;

@RestController
@RequestMapping("/api/crud/album")
public class AlbumCRUDController {
    private AlbumCRUDService service;
    private RecommendationService recommendationService;

    @Autowired
    public AlbumCRUDController(AlbumCRUDService service, RecommendationService recommendationService) {
        this.service = service;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/create")
    public long addAlbum(@RequestBody AlbumDto dto){
        System.out.println("Request dodania albumu " + dto);
        long id = service.addAlbum(dto);
        recommendationService.fillFastMapFromDB();
        return id;
    }

    @DeleteMapping("/delete")
    public long deleteAlbum(@RequestParam("id") Long albumId){
        long id = service.deleteAlbum(albumId);
        recommendationService.fillFastMapFromDB();
        return id;
    }

    @PostMapping("/update")
    public long updateAlbum(@RequestBody AlbumDto dto) {
        System.out.println("Request aktualizacji albumu " + dto);
        long id = service.updateAlbum(dto);
        recommendationService.fillFastMapFromDB();
        return id;
    }

    @GetMapping("/genreNames")
    public List<String> getAllGenreNames() {
        return service.getAllGenreNames();
    }

    @GetMapping("/subgenreNames")
    public List<String> getAllSubgenreNames() {
        return service.getAllSubgenreNames();
    }

    @GetMapping("/artistNameForAlbum")
    public String getArtistNameFromAlbum(@RequestParam("id") Long albumId) {
        return service.artistNameForAlbum(albumId);
    }
}
