package tu.kielce.walczak.MusicStore.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tu.kielce.walczak.MusicStore.dao.AlbumRepository;
import tu.kielce.walczak.MusicStore.dto.AlbumWrapper;
import tu.kielce.walczak.MusicStore.entity.Album;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService{

    private AlbumRepository albumRepository;

    @Autowired
    public RecommendationServiceImpl(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }


    @Override
    @Transactional
    public List<AlbumWrapper> getTestRecommendations(Long albumId, int size) {
        List<Album> foundAlbums = albumRepository.findAll().stream().limit(size).collect(Collectors.toList());
        foundAlbums.get(0).albumToVector();
        return foundAlbums.stream().map(album -> new AlbumWrapper(album, 1.5)).collect(Collectors.toList());
    }
}
