package tu.kielce.walczak.MusicStore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.service.RecommendationService;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rec")
public class RecommendationController {
    private RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostConstruct
    public void init(){
        this.recommendationService.fillDataFromDB();
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

    @GetMapping("/euclidSubgenreRecs")
    public List<AlbumWrapper> getEuclidSubenreRecommendations(@RequestParam("id") Long albumId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje euclidSubgenre dla id=" + albumId + ", rozmiar listy=" + size);
        return recommendationService.getEuclidSubgenreRecs(albumId, size);
    }
    @GetMapping("/mixedRecs")
    public List<AlbumWrapper> getMixedRecommendations(@RequestParam("id") Long albumId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje mixed dla id=" + albumId + ", rozmiar listy=" + size);
        return recommendationService.getMixedRecs(albumId, size);
    }

    @GetMapping("/cosineRecs")
    public List<AlbumWrapper> getCosineRecommendations(@RequestParam("id") Long albumId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje cosine item similarity dla id=" + albumId + ", rozmiar listy=" + size);
        return recommendationService.getCosineRecs(albumId, size);
    }

    @GetMapping("/dummyUserRecs")
    public List<AlbumWrapper> getDummyUserRecommendations(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje spearman on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getDummyUserRecs(userId, size);
    }

    @GetMapping("/pearsonNearest")
    public List<AlbumWrapper> getPearsonNearestNRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje pearson nearest n on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, RecommendationService.Mode.PearsonNearest);
    }

    @GetMapping("/pearsonThreshold")
    public List<AlbumWrapper> getPearsonThresholdRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje pearson threshold on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, RecommendationService.Mode.PearsonThreshold);
    }

    @GetMapping("/spearmanNearest")
    public List<AlbumWrapper> getSpearmanNearestNRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje spearman nearest n on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, RecommendationService.Mode.SpearmanNearest);
    }

    @GetMapping("/spearmanThreshold")
    public List<AlbumWrapper> getSpearmanThresholdRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje spearman threshold on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, RecommendationService.Mode.SpearmanThreshold);
    }

    @GetMapping("/recentBestsellers")
    public List<AlbumWrapper> getRecentBestsellers(@RequestParam("size") int size) {
        System.out.println("Prośba o bestsellery" + ", rozmiar listy=" + size);
        return recommendationService.getBestsellers(size);
    }

    @GetMapping("/metrics")
    public void getMetrics() {
        Map<Long, Long> resultEuclid = recommendationService.getCoverageAndVarietyMetricsForMode(1);
        //recommendationService.getCoverageAndVarietyMetricsForMode(2);
        Map<Long, Long> resultCosine =recommendationService.getCoverageAndVarietyMetricsForMode(3);
        //recommendationService.getCoverageAndVarietyMetricsForMode(4);
    }

    @GetMapping("/evaluation")
    public void getEvaluation() {
        System.out.println("Dla 100 użytkowników (1%): " + recommendationService.getEvaluation(0.8, 0.01));
        System.out.println("Dla 1000 użytkowników (10%): " + recommendationService.getEvaluation(0.8, 0.1));
        System.out.println("Dla 5000 użytkowników (50%): " + recommendationService.getEvaluation(0.8, 0.5));
        System.out.println("Dla 10000 użytkowników (100%): " + recommendationService.getEvaluation(0.8, 1.0));
    }
}
