package tu.kielce.walczak.MusicStore.service;

import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.List;

public interface RecommendationService {
    List<AlbumWrapper> getTestRecommendations(Long albumid, int size);
}
