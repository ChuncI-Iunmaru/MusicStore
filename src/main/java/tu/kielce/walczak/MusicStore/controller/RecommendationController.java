package tu.kielce.walczak.MusicStore.controller;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;
import tu.kielce.walczak.MusicStore.recommenders.ContentBasedEvaluator;
import tu.kielce.walczak.MusicStore.recommenders.ContentBasedMode;
import tu.kielce.walczak.MusicStore.recommenders.UserBasedEvaluator;
import tu.kielce.walczak.MusicStore.recommenders.UserBasedMode;
import tu.kielce.walczak.MusicStore.service.RecommendationService;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return recommendationService.getUniversalUserRecs(userId, size, UserBasedMode.PearsonNearest);
    }

    @GetMapping("/pearsonThreshold")
    public List<AlbumWrapper> getPearsonThresholdRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje pearson threshold on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, UserBasedMode.PearsonThreshold);
    }

    @GetMapping("/spearmanNearest")
    public List<AlbumWrapper> getSpearmanNearestNRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje spearman nearest n on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, UserBasedMode.SpearmanNearest);
    }

    @GetMapping("/spearmanThreshold")
    public List<AlbumWrapper> getSpearmanThresholdRecs(@RequestParam("id") Long userId, @RequestParam("size") int size) {
        System.out.println("Prośba o rekomendacje spearman threshold on user dla userId=" + userId + ", rozmiar listy=" + size);
        return recommendationService.getUniversalUserRecs(userId, size, UserBasedMode.SpearmanThreshold);
    }

    @GetMapping("/recentBestsellers")
    public List<AlbumWrapper> getRecentBestsellers(@RequestParam("size") int size) {
        System.out.println("Prośba o bestsellery" + ", rozmiar listy=" + size);
        return recommendationService.getBestsellers(size);
    }

    @GetMapping("/userBasedEvaluation")
    public void getUserBasedEvaluation() {
        DataModel model = recommendationService.getUserModel();
        System.out.println("Generowanie logów porównywania user-based");
        try {
            String fileName = "user_evaluation_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH_mm"))
                    + ".txt";
            System.out.println(fileName);
            double trainingSplit = 0.8;
            double[] userSplits = {0.1, 0.25, 0.5, 1.0};
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.printf("Załadowanych %d użytkowników \n", model.getNumUsers());
            printWriter.printf("Załadowanych %d items \n", model.getNumItems());
            printWriter.printf("Training Split = %f", trainingSplit);
            UserBasedEvaluator evaluator = new UserBasedEvaluator(model, 0.1, 100);

            for (UserBasedMode mode: UserBasedMode.values()){
                printWriter.println("--------------------------------------------------------------------------------");
                printWriter.printf("Dla algorytmu %s\n", mode.name());
                System.out.println("Dla algorytmu " + mode.name());
                for (double userSplit : userSplits){
                    System.out.printf("Dla %f%% (%f) użytkowników (users split)\n",userSplit*100, userSplit*model.getNumUsers());
                    printWriter.printf("Dla %f%% (%f) użytkowników (users split) = ",userSplit*100, userSplit*model.getNumUsers());
                    printWriter.println(evaluator.getEvaluation(trainingSplit, userSplit, mode));
                    printWriter.println();
                }
                printWriter.println("--------------------------------------------------------------------------------");
                printWriter.println();
            }
            printWriter.close();
        } catch (IOException | TasteException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/contentBasedEvaluation")
    public void getContentBasedEvaluation(){
        DataModel model = recommendationService.getItemModel();
        Map<Long, Album> albums = recommendationService.getFastMapAlbums();
        int recSize = 5;
        System.out.println("Generowanie logów porównywania content-based");
        try {
            String fileName = "content_evaluation_"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH_mm"))
                    + ".txt";
            FileWriter fileWriter = new FileWriter(fileName);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.printf("Załadowanych %d użytkowników \n", model.getNumUsers());
            printWriter.printf("Załadowanych %d items \n", model.getNumItems());
            printWriter.printf("Rozmiar listy rekomendacji = %d \n", recSize);
            ContentBasedEvaluator evaluator = new ContentBasedEvaluator(model,albums);
            List<Long> albumIdsAsc = albums.keySet().stream().sorted().collect(Collectors.toList());

            for (ContentBasedMode mode: ContentBasedMode.values()){
                Map<Long, Integer> albumCount = evaluator.getAlbumRecsCount(mode, recSize);
                Map<Long, Float> maxSimValues = evaluator.getMaxSimilarityValues(mode, recSize);
                Map<Long, Float> minSimValues = evaluator.getMinSimilarityValues(mode, recSize);
                List<Long> unrecommendedAlbums = evaluator.getUnrecommendedAlbums(mode, recSize);
                printWriter.println("--------------------------------------------------------------------------------");
                printWriter.printf("Dla algorytmu %s\n", mode.name());
                printWriter.printf("id;count;minSim;maxSim\n");
                for (Long id: albumIdsAsc){
                    printWriter.printf("%d;%d;%f;%f\n",
                            id,
                            albumCount.get(id),
                            minSimValues.get(id),
                            maxSimValues.get(id));
                }
                printWriter.println();
                printWriter.printf("Nie pojawiło się ani razu %d albumów\n", unrecommendedAlbums.size());
//                printWriter.println(Arrays.toString(unrecommendedAlbums.toArray()));
                printWriter.println("--------------------------------------------------------------------------------");
                printWriter.println();
            }
            printWriter.close();
        } catch (IOException | TasteException e) {
            e.printStackTrace();
        }
    }
}
