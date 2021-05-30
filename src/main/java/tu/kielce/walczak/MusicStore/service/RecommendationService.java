package tu.kielce.walczak.MusicStore.service;

import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.List;

public interface RecommendationService {
    List<AlbumWrapper> getTestRecommendations(Long albumid, int size);
    List<AlbumWrapper> getEuclidGenreRecs(Long albumId, int size);
    List<AlbumWrapper> getEuclidSubgenreRecs(Long albumId, int size);
    List<AlbumWrapper> getMixedRecs(Long albumId, int size);
    List<AlbumWrapper> getCosineRecs(Long albumId, int size);
    List<AlbumWrapper> getDummyUserRecs(Long userId, int size);
}
