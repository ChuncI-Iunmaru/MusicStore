package tu.kielce.walczak.MusicStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tu.kielce.walczak.MusicStore.dto.AlbumDto;
import tu.kielce.walczak.MusicStore.service.AlbumCRUDService;

import java.util.List;

@RestController
@RequestMapping("/api/crud/album")
public class AlbumCRUDController {
    private AlbumCRUDService service;

    @Autowired
    public AlbumCRUDController(AlbumCRUDService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public long addAlbum(@RequestBody AlbumDto dto){
        return service.addAlbum(dto);
    }

    @DeleteMapping("/delete")
    public long deleteAlbum(@RequestParam("id") Long albumId){
        return service.deleteAlbum(albumId);
    }

    @PostMapping("/update")
    public long updateAlbum(@RequestBody AlbumDto dto) {
        return service.updateAlbum(dto);
    }

    @GetMapping("/genreNames")
    public List<String> getAllGenreNames() {
        return service.getAllGenreNames();
    }

    @GetMapping("/subgenreNames")
    public List<String> getAllSubgenreNames() {
        return service.getAllSubgenreNames();
    }
}
