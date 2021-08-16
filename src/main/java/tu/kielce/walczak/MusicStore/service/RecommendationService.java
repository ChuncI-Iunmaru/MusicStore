package tu.kielce.walczak.MusicStore.service;

import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.List;
import java.util.Map;

public interface RecommendationService {
    enum Mode {
        SpearmanNearest, SpearmanThreshold, PearsonNearest, PearsonThreshold
    }

    List<AlbumWrapper> getTestRecommendations(Long albumid, int size);

    List<AlbumWrapper> getEuclidGenreRecs(Long albumId, int size);

    List<AlbumWrapper> getEuclidSubgenreRecs(Long albumId, int size);

    List<AlbumWrapper> getMixedRecs(Long albumId, int size);

    List<AlbumWrapper> getCosineRecs(Long albumId, int size);

    List<AlbumWrapper> getDummyUserRecs(Long userId, int size);

    List<AlbumWrapper> getUniversalUserRecs(Long userId, int size, Mode mode);

    List<AlbumWrapper> getBestsellers(int size);

    Map<Long, Long> getCoverageAndVarietyMetricsForMode(int mode);

    double getEvaluation(double trainingSplit, double usersSplit);
    void fillDataFromDB();
}
