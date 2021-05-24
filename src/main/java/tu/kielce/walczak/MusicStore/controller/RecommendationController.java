package tu.kielce.walczak.MusicStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.service.RecommendationService;

import java.util.List;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/api/rec")
public class RecommendationController {
    private RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/testRecommendations")
    public List<AlbumWrapper> getTestRecommendations(@RequestParam("id") Long albumId, @RequestParam("size") int size) {
        System.out.println("Prośba o testowe rekomendacje dla id=" + albumId + ", rozmiar listy=" + size);
        return recommendationService.getTestRecommendations(albumId, size);
    }

    @GetMapping("/euclidGenreRecs")
    public List<AlbumWrapper> getEuclidGenreRecommendations(@RequestParam("id") Long albumId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje euclidGenre dla id=" + albumId + ", rozmiar listy=" + size);
        return recommendationService.getEuclidGenreRecs(albumId, size);
    }
}
